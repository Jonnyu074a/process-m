# Fase 0 — Plan técnico

**Proyecto:** Droidsypm (`com.jonnyuun.processm`)
**Documento:** plan técnico de arranque
**Fecha:** 2026-11-04
**Estado:** validado por el usuario; Spike 1 en ejecución (scaffold + CI + UI base listos).

> Este documento responde al encargo de Fase 0 del prompt maestro: tecnología,
> SDK levels, módulos, viabilidad de MIUIX, matriz de capacidades, navegación,
> riesgos y plan por fases. Nada de lo aquí descrito se da por compilado hasta
> que se ejecute el Spike 0 (§8).

---

## 1. Resumen de decisiones

| Decisión | Elección propuesta | Motivo corto |
|---|---|---|
| Lenguaje | Kotlin 2.4.21 | Requerido por MIUIX 0.9.3 (compilado con Kotlin 2.4) y Compose moderno. |
| UI | Jetpack Compose + MIUIX 0.9.3 | MIUIX es Apache-2.0, tiene artefacto Android (`miuix-ui-android`) y minSdk 23. |
| minSdk | **26** (Android 8.0) | `StorageStatsManager` (tamaño por app), cobertura amplia, base común razonable. |
| compileSdk / targetSdk | **37 (minor 0) / 36** | MIUIX 0.9.x exige `minCompileSdk=37`; targetSdk 36 mantiene el comportamiento de Android 16. El dispositivo de prueba es API 35. |
| Build | AGP 9.3.1 + Gradle 9.5.0 + JDK 17 | AGP 9.1+ es obligatorio para API 37. Kotlin 2.4.21 se mantiene desactivando el Kotlin integrado de AGP 9. |
| CI | **GitHub Actions** (APK debug como artefacto) | No hay SDK local en el teléfono; el build vive en CI. |
| DI | **Koin 4.2.2** | Sin KSP/annotation processing: builds más rápidos y menos piezas frágiles. |
| Persistencia | DataStore (prefs) ahora; Room solo si hace falta | El historial de sesiones vive en memoria; exportación a CSV/JSON. |
| Gráficas | Canvas propio en Compose | Evita dependencias pesadas y permite controlar muestreo y rendimiento. |
| Nivel avanzado | Shizuku 13.1.5 con **UserService** | `Shizuku#newProcess` está en retirada; UserService es la vía soportada. |
| Nivel root | **Incluido en v1** (Fase 4 lectura, Fase 6 acciones) | Decisión del usuario; lista blanca, consentimiento y auditoría. |
| Monitorización en segundo plano | **No en v1** | Evita Foreground Service tipos/políticas y consumo; se pausa al salir. |
| Idioma | Español por defecto, recursos preparados para i18n | `values/` + `values-en/` y sin textos hardcodeados. |
| Nombre / package | **Droidsypm** / `com.jonnyuun.processm` | Identidad del desarrollador en el package. |
| Distribución | Sideload (uso personal/compartir fuera de Play) | Sin restricciones de política de Play para `QUERY_ALL_PACKAGES`. |
| Acento | Configurable (semilla Monet + presets) | Violeta por defecto; cambiable en Ajustes. |

Decisiones validadas por el usuario: §12.

---

## 2. Stack verificado (2026-11-04)

Todo lo siguiente se comprobó contra Maven Central / Google Maven / documentación
oficial, no de memoria.

### 2.1 Toolchain

| Componente | Versión | Verificación |
|---|---|---|
| JDK | 17 (Temurin/OpenJDK del sistema) | AGP 9.3 exige JDK 17 como mínimo. `openjdk-17-jdk-headless` está disponible en apt. |
| Gradle | 9.5.0 | Mínimo de AGP 9.3; KGP 2.4.21 soporta hasta 9.7.0. |
| AGP | 9.3.1 | Necesario para `compileSdk 37`/API 37; soportado por KGP 2.4.21 (máx. 9.3.1). |
| Kotlin (KGP) | 2.4.21 | KGP 2.4.20–2.4.21 soporta Gradle 7.6.3–9.7.0 y AGP 8.5.2–9.3.1. AGP 9 trae Kotlin integrado; se desactiva (`android.builtInKotlin=false`) para conservar KGP 2.4 y consumir MIUIX (kotlin-stdlib 2.4.0). |
| Compose BOM | 2026.06.01 (Compose 1.11.4) | `ui-android:1.11.4` exige `minCompileSdk=35`; compatible con compileSdk 37. |
| Build tools | 36.0.0 | Requerido por AGP 9.x; el platform es `platforms;android-37.0`. |

### 2.2 Librerías

| Librería | Versión | Notas |
|---|---|---|
| MIUIX (`top.yukonga.miuix.kmp:miuix-ui-android`) | **0.9.3** | Apache-2.0. AAR con `minSdkVersion=23`. Depende de Compose Multiplatform 1.11.1. Experimental. |
| MIUIX icons / preference / nav | 0.9.3 (opcional) | Misma versión que `miuix-ui`; solo si se usan. |
| Shizuku API / provider | 13.1.5 | `dev.rikka.shizuku:api` + `provider`. Requiere Android 6+. |
| Koin | 4.2.2 | `koin-android`, `koin-androidx-compose`, `koin-compose-viewmodel`. |
| Navigation Compose | 2.9.5 | `minCompileSdk=35`; no usar 2.10.x (exige compileSdk 37 + AGP 9.1). |
| Lifecycle | 2.10.0 | 2.11.0 exige compileSdk 37; no usar todavía. |
| Activity Compose | 1.12.4 | 1.13.0 también vale (`minCompileSdk=36`), pero 1.12.4 reduce variables. |
| Core KTX | 1.18.0 | 1.19.x exige compileSdk 37 + AGP 9.1. |
| DataStore Preferences | 1.3.0 | `minCompileSdk=34`. |
| Coroutines | 1.11.0 | Estable. |
| Kotlinx Serialization | 1.12.0 | Para rutas type-safe y export JSON. |
| Room | — | No se añade en el MVP. |

### 2.3 Por qué no lo último de todo

Compose 1.12.x (`ui-android:1.12.1`, `foundation-android:1.12.1`) y MIUIX 0.9.4
exigen **compileSdk 37 + AGP ≥ 9.1**. AGP 9 activa por defecto *built-in Kotlin*
y su nuevo DSL, que es incompatible con el plugin `org.jetbrains.kotlin.android`;
la configuración del compilador de Compose en ese modo no está documentada de
forma inequívoca. Migrar eso junto con el MVP mezcla dos riesgos.

Camino elegido: **stack estable hoy** (Compose 1.11.4 + MIUIX 0.9.3 + AGP 8.13.2)
y una tarea de migración planificada en Fase 7. El coste es quedarse una versión
menor por detrás en UI, sin impacto funcional.

Si más adelante se migra: AGP 9.x + compileSdk 37 + Compose 1.12/1.13 + MIUIX
0.9.4+, resolviendo antes el modo Compose del built-in Kotlin.

---

## 3. SDK levels

### minSdk 26 (Android 8.0)

- `StorageStatsManager` (tamaño real por aplicación) existe desde API 26.
- Compose y MIUIX funcionan desde 23/24; 26 no añade deuda técnica.
- Cobertura de dispositivos muy alta en 2026 y reduce combinaciones frente a 23/24.
- Contrapartida honesta: en Android 8–10, Shizuku sin PC no es posible (la
  depuración inalámbrica llega en Android 11). Esas versiones tendrán experiencia
  Standard completa y Advanced solo con PC. Se documentará en la app.

Alternativa si se prefiere minimizar matriz de pruebas: minSdk 29 (Android 10).
No se recomienda salvo que el objetivo sea solo dispositivos recientes.

### compileSdk 36 / targetSdk 36

- `compileSdk 36` es el máximo que AGP 8.13 soporta con garantías (36.1).
- `targetSdk 36` cumple el requisito de Play vigente en 2026 y evita comportamientos
  heredados de targets antiguos.
- El dispositivo de referencia (Xiaomi 2303ERA42L, HyperOS 2.0.215.0, Android 15,
  API 35) queda por debajo del target, que es el caso normal.

### Consecuencias de targetSdk 36 a tener en cuenta

- Edge-to-edge obligatorio: manejar insets en todas las pantallas.
- Restricciones de Foreground Services: por eso v1 no monitoriza en segundo plano.
- Package visibility (Android 11+): `QUERY_ALL_PACKAGES` o `<queries>`.
- Permisos de almacenamiento: se usa SAF para exportar, no rutas absolutas.

---

## 4. Arquitectura y módulos

### 4.1 Árbol Gradle

```
process-m/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── gradle.properties
├── app/                        # Application, MainActivity, NavHost, arranque Koin
├── core/
│   ├── model/                  # Kotlin puro: modelos, métricas, disponibilidad
│   ├── common/                 # Resultados, errores, formato, dispatchers, logging
│   └── designsystem/           # Tema MIUIX, tokens, tarjetas, gráficas, estados
├── domain/                     # Kotlin puro: interfaces de repositorio + casos de uso
├── data/                       # Implementación de repositorios, DataStore, exportación
├── core/system/                # Android APIs, /proc, shell, Shizuku, root, capacidades
└── feature/                    # Pantallas + ViewModels (paquetes por pantalla)
    ├── onboarding/
    ├── overview/
    ├── processes/
    ├── applications/
    ├── live/
    ├── device/
    └── settings/
```

8 módulos. No se crea un módulo por pantalla en la primera etapa: en un teléfono
cada módulo añade tiempo de configuración y de build. `feature` se dividirá en
`:feature:*` solo si el tiempo de compilación lo justifica (Fase 3–5).

### 4.2 Reglas de dependencia

```
app ─────────────► feature ─────► domain ─────► core:model
 │                    │              ▲
 │                    └► core:designsystem
 ├────────────► data ─────────────► domain
 │                 └──────────────► core:system
 └────────────► core:system / core:common
```

- `core:model`, `domain`: **Kotlin/JVM puro**, sin Android → tests rápidos.
- `core:system`: única capa que conoce APIs Android, `/proc`, Shizuku y root.
- `data`: decide qué fuente usar según `AccessLevel` y capacidades; nunca expone
  comandos ni `Process` a la UI.
- `feature`: solo conoce `domain` y `core:designsystem`; no importa `data` ni
  `core:system`.
- `app`: composición e inyección; sin lógica de negocio.

Esto se puede verificar con una tarea de lint arquitectónico propia más adelante
(no en el MVP).

### 4.3 Capa `system`: fuentes enchufables

```kotlin
interface MemorySource { suspend fun read(): Metric<MemorySnapshot> }
interface CpuSource { suspend fun read(): Metric<CpuSnapshot> }
interface ProcessSource { suspend fun list(): Metric<List<ProcessInfo>> }
interface TemperatureSource { suspend fun read(): Metric<TemperatureSnapshot> }
```

`data` construye la cadena según capacidades:

```
StandardSource → ShizukuSource → RootSource
      (siempre)     (si disponible)   (si autorizado)
```

No hay `if (root)` en la UI. Cada fuente declara `AccessLevel` y
`CapabilityStatus`; el repositorio elige la mejor y degrada con motivo explícito.

### 4.4 Inyección de dependencias: Koin

Elegido frente a Hilt por:

- Sin KSP: el build incremental en el teléfono es notablemente más rápido y hay
  menos piezas que puedan romperse con AGP 8/9.
- El grafo es pequeño (8 módulos) y verificable con `checkModules()` en tests.
- Integración directa con Compose (`koinViewModel`) y con servicios/Shizuku.

Coste asumido: los errores de grafo aparecen en runtime, no en compilación; se
mitiga con un test que valide todos los módulos.

### 4.5 Concurrencia y ciclo de vida

- Un `CoroutineScope` de aplicación (`SupervisorJob + Dispatchers.Default`).
- Lecturas de `/proc` y shell en `Dispatchers.IO`.
- Muestreo como `Flow` con `conflate()` y cadencia mínima; la UI consume el último valor.
- Cancelación estructurada al salir de pantalla y al pausar la app.
- Sin polling en `onResume` de varias pantallas a la vez: un solo `MetricsCoordinator`
  con contadores de suscriptores.
- Live Monitor solo en primer plano (sin servicio persistente en v1).

---

## 5. Modelo de datos: disponibilidad explícita

Regla central: **cero, desconocido, no soportado, permiso denegado y error son
cosas distintas**. No se usan nulos ambiguos.

```kotlin
enum class AccessLevel { STANDARD, SHIZUKU_ADB, SHIZUKU_ROOT, ROOT }

enum class MetricKind { MEASURED, CALCULATED, ESTIMATED }

enum class UnavailableReason {
    NOT_SUPPORTED,      // la API/versión no lo permite
    PERMISSION_REQUIRED,
    PERMISSION_DENIED,
    ACCESS_LEVEL_REQUIRED,
    READ_ERROR,
    TIMEOUT,
    STALE,
}

data class MetricSource(
    val id: String,             // "activitymanager.memoryinfo", "/proc/stat", "shizuku:/proc"
    val label: String,          // texto para la UI
    val accessLevel: AccessLevel,
    val description: String,
)

sealed interface Metric<out T> {
    val source: MetricSource
    val kind: MetricKind
    val timestamp: Instant

    data class Available<T>(
        val value: T,
        override val source: MetricSource,
        override val kind: MetricKind,
        override val timestamp: Instant,
    ) : Metric<T>

    data class Unavailable(
        val reason: UnavailableReason,
        val detail: String? = null,
        override val source: MetricSource,
        override val kind: MetricKind,
        override val timestamp: Instant,
    ) : Metric<Nothing>
}
```

Modelos previstos: `DeviceInfo`, `MemorySnapshot`, `CpuSnapshot`,
`TemperatureSnapshot`, `StorageSnapshot`, `ProcessInfo`, `ApplicationInfo`,
`PermissionStatus`, `CapabilityStatus`, `MetricSample`, `MonitoringSession`,
`ActionResult`.

Ejemplo de honestidad en procesos:

```kotlin
data class ProcessInfo(
    val pid: Int,
    val name: String,
    val uid: Int?,
    val packageName: String?,
    val state: ProcessState?,
    val rssBytes: Long?,          // null = no legible en este nivel
    val vszBytes: Long?,
    val cpuPercentOfOneCore: Double?, // calculado por delta
    val startTimeMillis: Long?,
    val source: MetricSource,
    val kind: MetricKind,
)
```

---

## 6. Estrategia de métricas (resumen)

Detalle completo en [`matriz-capacidades.md`](matriz-capacidades.md).

### CPU total

`/proc/stat` expone jiffies acumulados. El porcentaje se calcula por diferencia:

```
uso% = 100 * (Δtotal - Δidle) / Δtotal
Δ = muestra_actual - muestra_anterior
```

- `MEASURED` (jiffies del kernel) + `CALCULATED` (porcentaje).
- Intervalo mínimo 1 s; por defecto 2 s.
- Se muestra la ventana y la fuente; si el intervalo real se desvía, se indica.

### CPU por proceso

```
cpu% (de un núcleo) = 100 * Δ(utime + stime) / Δ(total_jiffies_por_núcleo)
```

- Requiere leer `/proc/<pid>/stat` de otros procesos: **solo Shizuku/root**
  (hidepid). En Standard no se muestra la lista completa.
- Se etiqueta como `CALCULATED`; no es una lectura instantánea de `top`.
- Se puede ofrecer “% del sistema” multiplicando por nº de núcleos; la UI debe
  decir cuál se está mostrando.

### Memoria

- `ActivityManager.MemoryInfo` (`totalMem`, `availMem`, `threshold`, `lowMemory`).
- `/proc/meminfo` como fuente secundaria (MemTotal, MemAvailable, Cached, Swap).
- PSS por app solo bajo demanda (`dumpsys meminfo` o `smaps_rollup` vía Shizuku/root):
  es caro y no se usa para refrescar cada segundo.

### Temperatura

- Temperatura de batería: `ACTION_BATTERY_CHANGED` (`EXTRA_TEMPERATURE`, décimas de °C).
- Estado térmico: `PowerManager.getCurrentThermalStatus()` (API 29+) → etiqueta
  (`NONE`, `LIGHT`, `MODERATE`, …), **no** grados.
- Temperaturas de CPU/GPU/skin: normalmente bloqueadas por SELinux; solo root, y
  variables por fabricante. Si no hay dato: "No disponible" con motivo.
- Nunca se inventa ni se interpola una temperatura.

### Almacenamiento

- `StatFs` para total/libre del volumen de datos.
- `StorageStatsManager` para tamaño por app (requiere acceso de uso).
- Caché vs datos vs código cuando la API lo permita.

---

## 7. Compilación: GitHub Actions

No se instala toolchain Android en el teléfono. El proyecto se compila en GitHub
Actions y el APK se descarga desde el artefacto (o desde una Release).

Hechos del entorno local (comprobados):

- No hay JDK, Gradle ni Android SDK.
- 8 núcleos, 7.5 GB de RAM (≈2.1 GB disponibles), 140 GB libres.
- El proyecto vive en `/storage/emulated/0/OpenCode/process-m` (FUSE).

### Workflow `android.yml`

- Runner `ubuntu-latest`, JDK 17 (Temurin).
- `android-actions/setup-android` (sin el paquete `tools`, retirado) + `sdkmanager`
  con `platforms;android-37.0` y `build-tools;36.0.0`.
- Gradle 9.5.0 vía `gradle/actions/setup-gradle` (sin wrapper en el repo por ahora).
- Tarea: `gradle :app:assembleDebug`.
- Artefacto: `process-m-debug` con `app/build/outputs/apk/debug/*.apk`.
- Disparadores: push a `main` y `workflow_dispatch`.

### Instalación en el teléfono

1. Abrir el artefacto en el navegador del teléfono (o una Release con el APK).
2. Descargar y permitir “instalar apps desconocidas” para el navegador/archivos.
3. Instalar. El APK de debug usa la clave de debug; sirve para uso personal.

### Release (más adelante)

Para APK firmado y actualizable sin desinstalar: keystore propio en secrets
(`KEYSTORE_BASE64`, `KEY_ALIAS`, `KEYSTORE_PASSWORD`, `KEY_PASSWORD`) y un
workflow por tag. Se decide cuando la app esté estable.

### Coste y límites

- Minutos de Actions: gratuitos en repos públicos; en privados consumen cuota.
- Cada iteración tarda ~2–4 min; es el bucle real de verificación.
- Sin SDK local no se pueden ejecutar pruebas instrumentadas ni emulador.
  Las pruebas unitarias JVM sí se ejecutan en CI.


---

## 8. Flujo de trabajo por etapa

Cada fase termina con: resumen, archivos creados/modificados, decisiones,
compilación ejecutada, pruebas ejecutadas y errores pendientes. Sin afirmar que
algo funciona sin haberlo comprobado.

| Fase | Entregable | Criterio de aceptación |
|---|---|---|
| **0** | Este plan + spike de toolchain en CI | GitHub Actions compila un APK con tema MIUIX y métricas reales; se instala en el teléfono. |
| **1** | MVP visual: Overview + Device con datos reales | RAM, CPU total, almacenamiento y batería reales; estados de error/permiso. |
| **2** | Métricas y gráficas | Repositorios, muestreo configurable, fuente visible por métrica. |
| **3** | Applications | Lista, búsqueda, filtros, detalle, abrir ajustes de app. |
| **4** | Processes + nivel root | Standard (limitado) + Shizuku (UserService) + lectura root con lista blanca y consentimiento. |
| **5** | Live Monitor | Sesiones, pausa, selección de métricas, export CSV/JSON, parada al salir. |
| **6** | Actions | Force-stop con confirmación, resultados, registro local; root solo en lista blanca. |
| **7** | Pulido | Accesibilidad, i18n, rendimiento, release firmado, licencias, migración AGP 9. |

### Spike 0 (antes de la Fase 1)

Objetivo: eliminar las incógnitas que pueden invalidar el plan.

1. Configurar GitHub Actions con JDK 17, SDK 37 y Gradle 9.5.0.
2. Proyecto mínimo multi-módulo: `:app` + `:core:*` + `:domain` + `:data` + `:feature`,
   tema MIUIX, una pantalla y barra inferior.
3. Leer RAM (`ActivityManager`), CPU total (`/proc/stat`) y almacenamiento (`StatFs`).
4. Compilar APK debug en CI y publicarlo como artefacto y Release.
5. Instalar y comprobar en el teléfono; medir arranque, memoria de la app y fluidez.
6. Decidir: ¿MIUIX 0.9.3 es estable y rinde bien en HyperOS? Si no, plan B:
   Material 3 con tokens visuales propios (la arquitectura no cambia).

#### Resultado del spike (2026-11-04)

- Build en GitHub Actions: **correcto** en 1m20s (run `41888983118`).
- APK debug: **10.2 MB**, `applicationId=com.jonnyuun.processm`,
  `minSdk=26`, `targetSdk=36`, `compileSdk=37`.
- Artefacto: `process-m-debug`.
  Release: https://github.com/Jonnyu074a/process-m/releases/tag/v0.1.0-spike0
- Incidencias resueltas: MIUIX 0.9.x exige `compileSdk 37` (no 36), API 37 solo
  está en `platforms;android-37.0`, y AGP 9 necesita desactivar Kotlin integrado
  para usar KGP 2.4.21.
- Pendiente: instalar en el teléfono y anotar arranque/memoria/fluidez; con eso
  se cierra la decisión sobre MIUIX.

#### Seguimiento del spike: Spike 1 (2026-11-04)

- Primera prueba real en el teléfono: el contenido se percibía pegado a los
  márgenes y la barra inferior era solo texto, sin iconos por apartado.
- Corrección: `NavigationBar`/`NavigationBarItem` nativos de MIUIX, con icono y
  etiqueta, inset de barra de navegación y divisor; cinco vectores propios.
- Overview: 20 dp de margen horizontal, `consumeWindowInsets(padding)` en el
  contenido y `Scaffold` con la barra MIUIX como `bottomBar`.
- Build correcto en 1m32s (run `41892851356`); Release
  https://github.com/Jonnyu074a/process-m/releases/tag/v0.1.1-spike1
- Pendiente: revalidar en el teléfono la barra con iconos y los márgenes.

---

## 9. Riesgos principales (detalle en `riesgos.md`)

1. MIUIX es experimental (0.9.x): APIs pueden cambiar; se aísla en `core:designsystem`.
2. Acoplamiento MIUIX ↔ Compose 1.11 y compileSdk 37: se fija BOM y se valida en el spike;
   cualquier subida de MIUIX exige revisar AGP/Gradle/Kotlin.
3. `/proc` con hidepid: procesos y CPU por proceso solo con Shizuku/root.
4. HyperOS mata procesos en segundo plano: no se promete monitorización persistente.
5. Shizuku se detiene al reiniciar y en MIUI requiere "USB debugging (Security options)".
6. Sensores térmicos casi siempre inaccesibles sin root; la app debe decirlo.
7. Dependencia de CI para compilar: sin SDK local no hay build offline; mitigación:
   workflow reproducible y, más adelante, toolchain local opcional.
8. Permisos sensibles (`QUERY_ALL_PACKAGES`, `PACKAGE_USAGE_STATS`): al ser sideload
   no aplican políticas de Play, pero se declaran y explican en la app.

---

## 10. Estructura de la documentación

```
docs/fase-0/
├── plan-tecnico.md          # este documento
├── matriz-capacidades.md    # qué se puede hacer en cada nivel
├── navegacion-ui.md         # flujo, pantallas, tokens y estados
└── riesgos.md               # riesgos, límites de Android y mitigaciones
```

---

## 11. Qué NO se hará en la primera versión

- Matar procesos en masa o arbitrarios.
- Modificar gobernadores, límites térmicos, LMK o servicios del sistema.
- Consola shell libre para el usuario.
- Monitorización en segundo plano con servicio persistente.
- Datos simulados o “de ejemplo” sin etiqueta visible.
- Telemetría o envío de datos a servidores.
- Root obligatorio para funciones básicas (el nivel root es opcional).

---

## 12. Decisiones validadas

| Tema | Decisión |
|---|---|
| Build | GitHub Actions; APK debug como artefacto. Sin toolchain local. |
| minSdk | 26 (Android 8.0). |
| DI | Koin 4.2.2. |
| Root | Incluido en v1 (lectura en Fase 4, acciones en Fase 6), opt-in. |
| Nombre | Droidsypm. |
| Package | `com.jonnyuun.processm`. |
| Distribución | Sideload para uso personal/compartir fuera de Google Play. |
| Acento | Violeta por defecto; configurable en Ajustes. |

Pendiente operativo (no de producto): crear el repositorio de GitHub y autorizar
el push desde este entorno. Detalle al final de este documento.

### Cómo se conecta este entorno con GitHub

1. Instalar `gh` y autenticar con device flow (código de un solo uso).
2. Crear el repo (público o privado) y hacer el primer push.
3. El workflow compila y publica el artefacto; se descarga desde el teléfono.
4. Cada cambio se entrega con push; el APK se actualiza solo.
