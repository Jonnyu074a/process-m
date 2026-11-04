package com.jonnyuun.processm.core.model

/** Memoria reportada por [android.app.ActivityManager.MemoryInfo]. */
data class MemorySnapshot(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
) {
    val usedFraction: Double
        get() = if (totalBytes <= 0L) 0.0 else usedBytes.toDouble() / totalBytes
}

/** Uso de CPU calculado entre dos muestras de `/proc/stat`. */
data class CpuSnapshot(
    val usagePercent: Double,
    val perCorePercent: List<Double>,
    val loadAverage1m: Double?,
)

/** Batería reportada por [android.os.BatteryManager]. */
data class BatterySnapshot(
    val levelPercent: Int,
    val isCharging: Boolean,
)
data class StorageSnapshot(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
) {
    val usedFraction: Double
        get() = if (totalBytes <= 0L) 0.0 else usedBytes.toDouble() / totalBytes
}

/** Snapshot de un proceso en ejecución con su consumo de memoria. */
data class ProcessSnapshot(
    val pid: Int,
    val processName: String,
    val packageName: String?,
    val importance: Int,
    val ramBytes: Long,
    val isSystem: Boolean,
)
