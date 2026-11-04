# Process M — Android System Monitor & Task Manager

Aplicación Android nativa para observar el estado del dispositivo, sus
aplicaciones y procesos, con tres niveles de acceso explícitos:

- **Standard**: APIs públicas de Android.
- **Advanced**: Shizuku / ADB.
- **Root**: solo con autorización explícita.

Principio rector: **exactitud y transparencia**. Cada métrica indica su fuente,
cómo se calcula y qué limitaciones tiene. Lo que no se puede medir se muestra
como “No disponible”, nunca como un dato inventado.

## Estado

**Fase 0 completada — Spike 0 en curso.** El plan está validado y el proyecto
arranca con un APK mínimo compilado en GitHub Actions (tema MIUIX y métricas
reales de RAM/CPU/almacenamiento). Después, Fase 1 (Overview + Device).

- Package: `com.jonnyuun.processm`
- Distribución: sideload (uso personal, fuera de Google Play)
- Root: incluido en v1, siempre opt-in y con lista blanca.

## Documentación

| Documento | Contenido |
|---|---|
| [Plan técnico](docs/fase-0/plan-tecnico.md) | Stack verificado, SDK levels, módulos, build, fases y decisiones abiertas. |
| [Matriz de capacidades](docs/fase-0/matriz-capacidades.md) | Qué se puede hacer en Standard, Shizuku/ADB y root, con fuentes y límites. |
| [Navegación y UI](docs/fase-0/navegacion-ui.md) | Flujo de pantallas, guía visual, componentes, estados y accesibilidad. |
| [Riesgos](docs/fase-0/riesgos.md) | Riesgos técnicos, límites de Android, seguridad y pruebas. |

## Stack

- Kotlin 2.4.21 · Jetpack Compose (BOM 2026.06.01) · MIUIX 0.9.3
- AGP 8.13.2 · Gradle 8.14.6 · JDK 17 · compileSdk/targetSdk 36 · minSdk 26
- Koin 4.2.2 · Coroutines/Flow · DataStore · Shizuku 13.1.5
- Arquitectura por capas: `app`, `core:model`, `core:common`, `core:designsystem`,
  `core:system`, `domain`, `data`, `feature`

## Compilar

El build oficial vive en GitHub Actions (`.github/workflows/android.yml`): JDK 17,
SDK 36 y Gradle 8.14.6. El APK debug se publica como artefacto `process-m-debug`.

## Próximo paso

Completar el Spike 0: verificar que el APK compila en CI, instalarlo en el
teléfono y medir build, tamaño y rendimiento. Después, Fase 1 (Overview + Device).
