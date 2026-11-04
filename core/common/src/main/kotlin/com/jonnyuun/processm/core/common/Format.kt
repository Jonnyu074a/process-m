package com.jonnyuun.processm.core.common

import java.util.Locale

private const val MIB = 1024.0 * 1024.0
private const val GIB = 1024.0 * 1024.0 * 1024.0

/** Formatea bytes en unidades binarias (MiB/GiB), que es lo que reporta Android. */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 MiB"
    return if (bytes.toDouble() >= GIB) {
        String.format(Locale.getDefault(), "%.1f GiB", bytes / GIB)
    } else {
        String.format(Locale.getDefault(), "%.0f MiB", bytes / MIB)
    }
}

/** Fracción 0..1 → porcentaje con cero decimales. */
fun formatFraction(fraction: Double): String =
    String.format(Locale.getDefault(), "%.0f%%", (fraction * 100.0).coerceIn(0.0, 100.0))

/** Porcentaje 0..100 con cero decimales. */
fun formatPercent(percent: Double): String =
    String.format(Locale.getDefault(), "%.0f%%", percent.coerceIn(0.0, 100.0))

/** Carga media con dos decimales. */
fun formatLoad(load: Double): String =
    String.format(Locale.getDefault(), "%.2f", load)
