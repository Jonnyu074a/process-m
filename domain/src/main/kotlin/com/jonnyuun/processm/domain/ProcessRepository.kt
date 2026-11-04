package com.jonnyuun.processm.domain

import com.jonnyuun.processm.core.system.ProcessSnapshot

/** Contrato de acceso a los procesos en ejecución. */
interface ProcessRepository {
    suspend fun runningProcesses(): List<ProcessSnapshot>
}
EOF
echo done