package com.jonnyuun.processm.domain

import com.jonnyuun.processm.core.model.BatterySnapshot
import com.jonnyuun.processm.core.model.CpuSnapshot
import com.jonnyuun.processm.core.model.MemorySnapshot
import com.jonnyuun.processm.core.model.Metric
import com.jonnyuun.processm.core.model.StorageSnapshot

/** Contrato de acceso a métricas del sistema. */
interface MetricsRepository {
    suspend fun memory(): Metric<MemorySnapshot>
    suspend fun cpu(): Metric<CpuSnapshot>
    suspend fun storage(): Metric<StorageSnapshot>
    suspend fun battery(): Metric<BatterySnapshot>
}
