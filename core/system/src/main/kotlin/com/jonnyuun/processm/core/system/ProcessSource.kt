package com.jonnyuun.processm.core.system

interface ProcessSource {
    suspend fun read(): List<ProcessSnapshot>
}
