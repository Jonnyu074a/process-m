package com.jonnyuun.processm.data

import android.content.Context
import com.jonnyuun.processm.core.system.AndroidProcessSource
import com.jonnyuun.processm.core.system.ProcessSnapshot
import com.jonnyuun.processm.domain.ProcessRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProcessRepositoryImpl(
    private val context: Context,
) : ProcessRepository {

    private val source = AndroidProcessSource(context)

    override suspend fun runningProcesses(): List<ProcessSnapshot> =
        withContext(Dispatchers.Default) {
            source.read()
        }
}
EOF
echo done