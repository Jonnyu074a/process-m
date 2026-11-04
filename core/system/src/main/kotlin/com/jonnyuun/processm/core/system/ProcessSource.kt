package com.jonnyuun.processm.core.system

import com.jonnyuun.processm.core.model.ProcessSnapshot

interface ProcessSource {
    suspend fun read(): List<ProcessSnapshot>
}
EOF