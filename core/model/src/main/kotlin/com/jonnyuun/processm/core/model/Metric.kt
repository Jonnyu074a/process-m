package com.jonnyuun.processm.core.model

import java.time.Instant

/** Naturaleza del dato: medido, calculado a partir de medidas o estimado. */
enum class MetricKind {
    MEASURED,
    CALCULATED,
    ESTIMATED,
}

/**
 * Motivo por el que una métrica no está disponible. La UI lo traduce a texto;
 * nunca se sustituye por un valor inventado.
 */
enum class UnavailableReason {
    /** La plataforma o el OEM no exponen el dato. */
    NOT_SUPPORTED,

    /** Hace falta un permiso que el usuario aún no ha concedido. */
    PERMISSION_REQUIRED,

    /** Hace falta Shizuku/ADB o root. */
    ACCESS_LEVEL_REQUIRED,

    /** El dato existe pero no es legible en este momento. */
    NOT_READABLE,

    /** Aún no hay muestras suficientes. */
    COLLECTING,

    /** La lectura falló. */
    READ_ERROR,
}

/** Procedencia de una métrica, para mostrarla en la UI. */
data class MetricSource(
    val id: String,
    val label: String,
    val accessLevel: AccessLevel,
    val note: String,
)

/**
 * Envoltorio que obliga a declarar la procedencia y el estado de cada métrica.
 * [Unavailable] no lleva valor: no se puede "inventar" un dato ausente.
 */
sealed interface Metric<out T> {
    val kind: MetricKind
    val source: MetricSource
    val timestamp: Instant

    data class Available<T>(
        override val kind: MetricKind,
        override val source: MetricSource,
        override val timestamp: Instant,
        val value: T,
    ) : Metric<T>

    data class Unavailable(
        override val kind: MetricKind,
        override val source: MetricSource,
        override val timestamp: Instant,
        val reason: UnavailableReason,
        val detail: String? = null,
    ) : Metric<Nothing>
}
