package com.jonnyuun.processm.core.system

import android.app.ActivityManager
import android.app.ApplicationInfo
import android.content.Context
import android.content.pm.PackageManager
import com.jonnyuun.processm.core.model.MetricKind
import java.time.Instant

class AndroidProcessSource(
    private val context: Context,
) : ProcessSource {
    override suspend fun read(): List<ProcessSnapshot> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = context.packageManager

        val running = am.runningAppProcesses ?: return emptyList()
        val pids = running.map { it.pid }.toIntArray()

        val memInfo = if (pids.isNotEmpty()) {
            am.getProcessMemoryInfo(pids).associate { it.pid to it }
        } else {
            emptyMap()
        }

        return running.map { proc ->
            val ramBytes = memInfo[proc.pid]?.let { mi ->
                // dalvikPrivateDirty + nativePrivateDirty en KB → bytes
                (mi.dalvikPrivateDirty + mi.nativePrivateDirty) * 1024L
            } ?: 0L

            val packageName = proc.pkgList.firstOrNull()
            val isSystem = packageName?.let { pkg ->
                try {
                    val ai = pm.getApplicationInfo(pkg, 0)
                    (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                } catch (_: Exception) {
                    false
                }
            } ?: false

            ProcessSnapshot(
                pid = proc.pid,
                processName = proc.processName,
                packageName = packageName,
                importance = proc.importance,
                ramBytes = ramBytes,
                isSystem = isSystem,
            )
        }.sortedByDescending { it.ramBytes }
    }
}
EOF
echo done