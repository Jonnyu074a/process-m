package com.jonnyuun.processm.core.system

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager

class AndroidProcessSource(
    private val context: Context,
) : ProcessSource {
    override suspend fun read(): List<ProcessSnapshot> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = context.packageManager

        val running = am.runningAppProcesses ?: return emptyList()
        val pids = running.map { it.pid }.toIntArray()

        val memInfos = if (pids.isNotEmpty()) {
            am.getProcessMemoryInfo(pids)
        } else {
            emptyArray()
        }

        // getProcessMemoryInfo devuelve un array en el mismo orden que pids
        val memInfoMap = pids.indices.associate { idx ->
            pids[idx] to memInfos[idx]
        }.toMap()

        return running.map { proc ->
            val mi = memInfoMap[proc.pid]
            val ramBytes = mi?.let {
                // dalvikPrivateDirty + nativePrivateDirty en KB → bytes
                (it.dalvikPrivateDirty + it.nativePrivateDirty) * 1024L
            } ?: 0L

            val packageName = proc.pkgList.firstOrNull()
            val isSystem = packageName?.let { pkg ->
                try {
                    val ai = pm.getApplicationInfo(pkg, 0)
                    (ai.flags and android.app.ApplicationInfo.FLAG_SYSTEM) != 0
                } catch (_: Exception) {
                    false
                }
            } ?: false

            ProcessSnapshot(
                pid = proc.pid,
                processName = proc.processName ?: "",
                packageName = packageName,
                importance = proc.importance,
                ramBytes = ramBytes,
                isSystem = isSystem,
            )
        }.sortedByDescending { it.ramBytes }
    }
}
