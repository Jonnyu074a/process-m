package com.jonnyuun.processm.core.system

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import com.jonnyuun.processm.core.model.AppDetail
import com.jonnyuun.processm.core.model.AppSummary

class AndroidAppsSource(
    private val context: Context,
) {
    fun listApps(): List<AppSummary> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return packages.mapNotNull { appInfo ->
            try {
                val pkgInfo = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_META_DATA)
                AppSummary(
                    packageName = appInfo.packageName,
                    label = appInfo.loadLabel(pm) ?: appInfo.packageName,
                    versionName = pkgInfo.versionName,
                    versionCode = pkgInfo.versionCode,
                    isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    uid = appInfo.uid,
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }.sortedBy { it.label.lowercase() }
    }

    fun appDetail(packageName: String): AppDetail? {
        val pm = context.packageManager
        return try {
            val pkgInfo = pm.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA,
            )
            AppDetail(
                summary = AppSummary(
                    packageName = pkgInfo.packageName,
                    label = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0))
                    } catch (_: Exception) {
                        packageName
                    },
                    versionName = pkgInfo.versionName,
                    versionCode = pkgInfo.versionCode,
                    isSystem = (pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    uid = pkgInfo.applicationInfo.uid,
                ),
                firstInstallTime = pkgInfo.firstInstallTime,
                lastUpdateTime = pkgInfo.lastUpdateTime,
                requestedPermissions = pkgInfo.requestedPermissions?.toList() ?: emptyList(),
                dataSizeBytes = null,
                cacheSizeBytes = null,
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}
