package com.jonnyuun.processm.domain

import kotlinx.coroutines.flow.Flow

/** Preferencias de monitorización configurables por el usuario. */
interface SettingsRepository {
    /** Intervalo de muestreo en milisegundos (1000, 2000 o 5000). */
    val samplingIntervalMs: Flow<Long>

    suspend fun setSamplingIntervalMs(value: Long)
}
EOF
echo done