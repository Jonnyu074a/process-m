package com.jonnyuun.processm.core.system

import android.app.ActivityManager
import android.content.Context
import com.jonnyuun.processm.core.model.MemorySnapshot
import com.jonnyuun.processm.core.model.Metric
import com.jonnyuun.processm.core.model.MetricKind
import com.jonnyuun.processm.core.model.UnavailableReason
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidMemorySource(context: Context) : MemorySource {

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    override suspend fun read(): Metric<MemorySnapshot> = withContext(Dispatchers.IO) {
        try {
            val info = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(info)

            val total = info.totalMem
            val available = info.availMem
            if (total <= 0L) {
                return@withContext Metric.Unavailable(
                    kind = MetricKind.MEASURED,
                    source = SystemSources.Memory,
                    timestamp = Instant.now(),
                    reason = UnavailableReason.NOT_SUPPORTED,
                    detail = "El sistema no reporta memoria total.",
                )
            }

            Metric.Available(
                kind = MetricKind.MEASURED,
                source = SystemSources.Memory,
                timestamp = Instant.now(),
                value = MemorySnapshot(
                    totalBytes = total,
                    availableBytes = available,
                    usedBytes = (total - available).coerceAtLeast(0L),
                ),
            )
        } catch (e: Exception) {
            Metric.Unavailable(
                kind = MetricKind.MEASURED,
                source = SystemSources.Memory,
                timestamp = Instant.now(),
                reason = UnavailableReason.READ_ERROR,
                detail = e.message,
            )
        }
    }
}
