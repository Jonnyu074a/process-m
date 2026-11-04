package com.jonnyuun.processm

import android.app.Application
import android.content.Context
import com.jonnyuun.processm.core.system.AndroidMemorySource
import com.jonnyuun.processm.core.system.AndroidStorageSource
import com.jonnyuun.processm.core.system.ProcCpuSource
import com.jonnyuun.processm.data.SystemMetricsRepository
import com.jonnyuun.processm.domain.MetricsRepository

class ProcessMApp : Application() {
    val graph: AppGraph by lazy { AppGraph(this) }
}

/**
 * Contenedor manual de dependencias para el Spike 0. Koin entra en la Fase 1,
 * cuando haya más de un repositorio y ViewModels que inyectar.
 */
class AppGraph(context: Context) {

    private val memorySource = AndroidMemorySource(context.applicationContext)
    private val cpuSource = ProcCpuSource()
    private val storageSource = AndroidStorageSource()

    val metricsRepository: MetricsRepository = SystemMetricsRepository(
        memorySource = memorySource,
        cpuSource = cpuSource,
        storageSource = storageSource,
    )
}
