package com.jonnyuun.processm.core.system

import com.jonnyuun.processm.core.model.CpuSnapshot
import com.jonnyuun.processm.core.model.Metric
import com.jonnyuun.processm.core.model.MetricKind
import com.jonnyuun.processm.core.model.UnavailableReason
import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Lee `/proc/stat` y calcula el uso entre dos muestras consecutivas.
 * La primera llamada devuelve [UnavailableReason.COLLECTING]: no hay delta todavía.
 */
class ProcCpuSource : CpuSource {

    private val mutex = Mutex()
    private var previous: List<LongArray>? = null

    override suspend fun read(): Metric<CpuSnapshot> = withContext(Dispatchers.IO) {
        mutex.withLock { readLocked() }
    }

    private fun readLocked(): Metric<CpuSnapshot> {
        val timestamp = Instant.now()
        val current = try {
            readCpuTimes()
        } catch (e: Exception) {
            return unavailable(UnavailableReason.READ_ERROR, e.message, timestamp)
        } ?: return unavailable(
            UnavailableReason.NOT_READABLE,
            "No se pudo interpretar /proc/stat.",
            timestamp,
        )

        val before = previous
        previous = current

        if (before == null || before.size != current.size) {
            return unavailable(
                UnavailableReason.COLLECTING,
                "Se necesita una segunda muestra para calcular el uso.",
                timestamp,
            )
        }

        val aggregate = usageBetween(before[0], current[0])
        val perCore = current.indices.drop(1).mapNotNull { index ->
            usageBetween(before[index], current[index])
        }

        return Metric.Available(
            kind = MetricKind.CALCULATED,
            source = SystemSources.Cpu,
            timestamp = timestamp,
            value = CpuSnapshot(
                usagePercent = aggregate,
                perCorePercent = perCore,
                loadAverage1m = readLoadAverage(),
            ),
        )
    }

    /**
     * Devuelve la lista de contadores [idle, total] tal como aparecen en /proc/stat:
     * índice 0 = agregado, siguientes = un núcleo cada uno.
     */
    private fun readCpuTimes(): List<LongArray>? {
        val lines = File("/proc/stat").readLines()
        val times = ArrayList<LongArray>(lines.size)
        for (line in lines) {
            if (!line.startsWith("cpu")) continue
            val parts = line.trim().split(WHITESPACE)
            val name = parts.firstOrNull() ?: continue
            if (name != "cpu" && !name.drop(3).all(Char::isDigit)) continue

            val values = parts.drop(1).mapNotNull(String::toLongOrNull)
            if (values.size < 5) continue

            val idle = values[3] + values.getOrElse(4) { 0L }
            times += longArrayOf(idle, values.sum())
        }
        return times.takeIf { it.isNotEmpty() }
    }

    private fun usageBetween(before: LongArray, after: LongArray): Double {
        val deltaTotal = after[1] - before[1]
        val deltaIdle = after[0] - before[0]
        if (deltaTotal <= 0L) return 0.0
        return ((deltaTotal - deltaIdle).toDouble() / deltaTotal * 100.0).coerceIn(0.0, 100.0)
    }

    private fun readLoadAverage(): Double? = try {
        File("/proc/loadavg").readText().trim().split(WHITESPACE).firstOrNull()?.toDoubleOrNull()
    } catch (e: Exception) {
        null
    }

    private fun unavailable(
        reason: UnavailableReason,
        detail: String?,
        timestamp: Instant,
    ): Metric.Unavailable = Metric.Unavailable(
        kind = MetricKind.CALCULATED,
        source = SystemSources.Cpu,
        timestamp = timestamp,
        reason = reason,
        detail = detail,
    )

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
