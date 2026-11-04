package com.jonnyuun.processm.core.model

/** Resumen de una aplicación instalada. */
data class AppSummary(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val versionCode: Long,
    val isSystem: Boolean,
    val uid: Int,
)

/** Detalle de una aplicación. */
data class AppDetail(
    val summary: AppSummary,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val requestedPermissions: List<String>,
    val dataSizeBytes: Long?,
    val cacheSizeBytes: Long?,
)
EOF
echo done