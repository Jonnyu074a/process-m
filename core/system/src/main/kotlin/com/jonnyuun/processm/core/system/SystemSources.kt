package com.jonnyuun.processm.core.system

import com.jonnyuun.processm.core.model.AccessLevel
import com.jonnyuun.processm.core.model.MetricSource

/** Fuentes declaradas una sola vez para que la UI pueda mostrarlas. */
object SystemSources {
    val Memory = MetricSource(
        id = "android.activitymanager.memory",
        label = "ActivityManager.MemoryInfo",
        accessLevel = AccessLevel.STANDARD,
        note = "Memoria total y disponible reportada por Android. No incluye swap.",
    )

    val Cpu = MetricSource(
        id = "proc.stat",
        label = "/proc/stat",
        accessLevel = AccessLevel.STANDARD,
        note = "Uso de CPU calculado entre dos muestras; no es una lectura instantánea.",
    )

    val Storage = MetricSource(
        id = "android.statfs.data",
        label = "StatFs(/data)",
        accessLevel = AccessLevel.STANDARD,
        note = "Espacio del volumen de datos. 'Disponible' excluye bloques reservados del sistema.",
    )
}
