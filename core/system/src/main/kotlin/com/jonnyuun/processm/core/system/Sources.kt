package com.jonnyuun.processm.core.system

import com.jonnyuun.processm.core.model.CpuSnapshot
import com.jonnyuun.processm.core.model.MemorySnapshot
import com.jonnyuun.processm.core.model.Metric
import com.jonnyuun.processm.core.model.StorageSnapshot

interface MemorySource {
    suspend fun read(): Metric<MemorySnapshot>
}

interface CpuSource {
    suspend fun read(): Metric<CpuSnapshot>
}

interface StorageSource {
    suspend fun read(): Metric<StorageSnapshot>
}
