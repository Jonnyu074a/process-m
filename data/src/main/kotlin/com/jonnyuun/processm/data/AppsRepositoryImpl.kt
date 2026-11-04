package com.jonnyuun.processm.data

import android.content.Context
import com.jonnyuun.processm.core.system.AndroidAppsSource
import com.jonnyuun.processm.domain.AppsRepository

class AppsRepositoryImpl(
    private val context: Context,
) : AppsRepository {

    private val source = AndroidAppsSource(context)

    override suspend fun listApps(): List<com.jonnyuun.processm.core.model.AppSummary> =
        withContext(Dispatchers.Default) {
            source.listApps()
        }

    override suspend fun appDetail(packageName: String): com.jonnyuun.processm.core.model.AppDetail? =
        withContext(Dispatchers.Default) {
            source.appDetail(packageName)
        }
}
EOF
echo done