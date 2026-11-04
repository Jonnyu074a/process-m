package com.jonnyuun.processm.domain

import com.jonnyuun.processm.core.model.AppDetail
import com.jonnyuun.processm.core.model.AppSummary

/** Contrato de acceso al listado y detalle de aplicaciones instaladas. */
interface AppsRepository {
    /** Todas las aplicaciones instaladas, ordenadas alfabéticamente. */
    suspend fun listApps(): List<AppSummary>

    /** Detalle de una aplicación por nombre de paquete. */
    suspend fun appDetail(packageName: String): AppDetail?
}
