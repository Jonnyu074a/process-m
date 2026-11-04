package com.jonnyuun.processm.data

import com.jonnyuun.processm.core.model.CpuSnapshot
import com.jonnyuun.processm.core.model.MemorySnapshot
import com.jonnyuun.processm.core.model.Metric
import com.jonnyuun.processm.core.model.StorageSnapshot
import com.jonnyuun.processm.core.system.CpuSource
import com.jonnyuun.processm.core.system.MemorySource
import com.jonnyuun.processm.core.system.StorageSource
import com.jonnyuun.processm.domain.MetricsRepository

class SystemMetricsRepository(
    private val memorySource: MemorySource,
    private val cpuSource: CpuSource,
    private val storageSource: StorageSource,
) : MetricsRepository {

    override suspend fun memory(): Metric<MemorySnapshot> = memorySource.read()

    override suspend fun cpu(): Metric<CpuSnapshot> = cpuSource.read()

    override suspend fun storage(): Metric<StorageSnapshot> = storageSource.read()
}
