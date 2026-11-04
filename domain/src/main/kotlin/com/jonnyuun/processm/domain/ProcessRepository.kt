package com.jonnyuun.processm.domain

import com.jonnyuun.processm.core.model.ProcessSnapshot

/** Contrato de acceso a los procesos en ejecución. */
interface ProcessRepository {
    suspend fun runningProcesses(): List<ProcessSnapshot>
}
