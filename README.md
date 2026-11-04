# Droidsypm — Android System Monitor & Task Manager

Aplicación Android nativa para observar el estado del dispositivo, sus
aplicaciones y procesos, con tres niveles de acceso explícitos:

- **Standard**: APIs públicas de Android.
- **Advanced**: Shizuku / ADB.
- **Root**: solo con autorización explícita.

Principio rector: **exactitud y transparencia**. Cada métrica indica su fuente,
cómo se calcula y qué limitaciones tiene. Lo que no se puede medir se muestra
como “No disponible”, nunca como un dato inventado.

## Estado

**Fase 1 en curso — primera entrega publicada.** Droidsypm ya usa Koin, muestra
batería y tiene pantalla Device con métricas reales. Siguientes pasos de la
Fase 1: estados de permiso y pulido de Overview/Device.

- Package: `com.jonnyuun.processm`
- Distribución: sideload (uso personal, fuera de Google Play)
- Root: incluido en v1, siempre opt-in y con lista blanca.
- APK de prueba: [v0.2.2-phase1](https://github.com/Jonnyu074a/process-m/releases/tag/v0.2.2-phase1)
  (anteriores: [v0.2.1-phase1](https://github.com/Jonnyu074a/process-m/releases/tag/v0.2.1-phase1), ...)

## Documentación

| Documento | Contenido |
|---|---|
| [Plan técnico](docs/fase-0/plan-tecnico.md) | Stack verificado, SDK levels, módulos, build, fases y decisiones abiertas. |
| [Matriz de capacidades](docs/fase-0/matriz-capacidades.md) | Qué se puede hacer en Standard, Shizuku/ADB y root, con fuentes y límites. |
| [Navegación y UI](docs/fase-0/navegacion-ui.md) | Flujo de pantallas, guía visual, componentes, estados y accesibilidad. |
| [Riesgos](docs/fase-0/riesgos.md) | Riesgos técnicos, límites de Android, seguridad y pruebas. |

## Stack

- Kotlin 2.4.21 · Jetpack Compose (BOM 2026.06.01) · MIUIX 0.9.3
- AGP 9.3.1 · Gradle 9.5.0 · JDK 17 · compileSdk 37 (minor 0) · targetSdk 36 · minSdk 26
- Koin 4.2.2 · Coroutines/Flow · DataStore · Shizuku 13.1.5
- Arquitectura por capas: `app`, `core:model`, `core:common`, `core:designsystem`,
  `core:system`, `domain`, `data`, `feature`

> MIUIX 0.9.x exige `compileSdk 37`, y API 37 requiere AGP 9.1+. Para conservar
> Kotlin 2.4.21 (MIUIX depende de kotlin-stdlib 2.4) se desactiva el Kotlin
> integrado de AGP 9 (`android.builtInKotlin=false`) y el nuevo DSL.

## Compilar

El build oficial vive en GitHub Actions (`.github/workflows/android.yml`): JDK 17,
SDK 37 (`platforms;android-37.0`), build-tools 36.0.0 y Gradle 9.5.0. El APK debug
se publica como artefacto `process-m-debug` y como Release.

## Próximo paso

Instalar `v0.1.2-spike2` y validar las barras de progreso y sparklines, además de
los márgenes y la navegación con iconos. Después, Fase 1 (Overview + Device).
