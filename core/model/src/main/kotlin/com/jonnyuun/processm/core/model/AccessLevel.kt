package com.jonnyuun.processm.core.model

/** Nivel de acceso con el que se obtuvo un dato. */
enum class AccessLevel {
    /** APIs públicas de Android. */
    STANDARD,

    /** Shizuku / ADB. */
    ADVANCED,

    /** Shell con privilegios de root, siempre con consentimiento. */
    ROOT,
}
