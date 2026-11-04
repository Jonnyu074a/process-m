package com.jonnyuun.processm.core.system

/** Snapshot de un proceso en ejecución con su consumo de memoria. */
data class ProcessSnapshot(
    val pid: Int,
    val processName: String,
    val packageName: String?,
    val importance: Int,
    val ramBytes: Long,
    val isSystem: Boolean,
)
