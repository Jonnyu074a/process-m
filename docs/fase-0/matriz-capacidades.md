# Matriz de capacidades — Standard / Shizuku-ADB / Root

Leyenda:

- ✅ disponible con implementación directa
- 🟡 parcial, depende de versión/fabricante o requiere un permiso especial
- ❌ no disponible en ese nivel
- 🔒 requiere confirmación explícita del usuario
- ⚠️ costoso (evitar en refresco periódico)

`S` = Standard · `A` = Advanced (Shizuku/ADB) · `R` = Root

---

## 1. Niveles y detección

| Nivel | Cómo se obtiene | Identidad | Notas |
|---|---|---|---|
| Standard | Siempre | uid de la app | APIs públicas y permisos normales/especiales. |
| Advanced (Shizuku ADB) | Shizuku activo y permiso concedido | uid 2000 (`shell`) | Requiere Shizuku/Sui; en Android 11+ se inicia sin PC. |
| Advanced (Shizuku root) | Shizuku iniciado con root | uid 0 | Mismas APIs, más capacidad. |
| Root directo | `su` autorizado por el gestor root | uid 0 | Se detecta y se pide permiso; nunca automático. |

Reglas:

- El nivel se recalcula al volver a primer plano, al morir el binder de Shizuku y
  al cambiar permisos. No se cachea para siempre.
- Una función sin capacidad se muestra **deshabilitada con explicación**, no oculta
  ni simulada.
- Shizuku ADB y Shizuku root se distinguen: `Shizuku.getUid()` devuelve 2000 o 0.

---

## 2. Métricas del dispositivo

| Métrica | S | A | R | Fuente / API | Precisión y límites | Fase |
|---|---|---|---|---|---|---|
| RAM total / disponible | ✅ | ✅ | ✅ | `ActivityManager.MemoryInfo` | `MEASURED`. `availMem` no es “libre” exacta: incluye caché reclamable. | 1 |
| RAM detallada (caché, swap) | 🟡 | ✅ | ✅ | `/proc/meminfo` | Lectura directa; puede variar por ROM. Si falla: no disponible. | 2 |
| RAM por app (PSS) | ❌ | ⚠️✅ | ⚠️✅ | `dumpsys meminfo <pkg>` o `/proc/<pid>/smaps_rollup` | Costoso (100–500 ms). Solo bajo demanda. | 4 |
| CPU total | ✅ | ✅ | ✅ | `/proc/stat` | `MEASURED` en jiffies; % `CALCULATED` por delta. | 1 |
| CPU por núcleo | ✅ | ✅ | ✅ | `/proc/stat` (`cpu0..cpuN`) | Mismo cálculo por línea. | 2 |
| Frecuencias de CPU | 🟡 | ✅ | ✅ | `/sys/devices/system/cpu/cpu*/cpufreq/*` | A menudo legible; si SELinux lo bloquea, no disponible. | 2 |
| Carga media | ✅ | ✅ | ✅ | `/proc/loadavg` | Medida del kernel, no porcentaje. | 2 |
| CPU por proceso | ❌ | ✅ | ✅ | `/proc/<pid>/stat` | `CALCULATED` por delta de utime+stime. Requiere hidepid sorteado. | 4 |
| Nº de procesos | ❌ | ✅ | ✅ | `/proc` o `ps` vía Shizuku | Standard no ve procesos de otras apps. | 4 |
| Temperatura de batería | ✅ | ✅ | ✅ | `ACTION_BATTERY_CHANGED` → `EXTRA_TEMPERATURE` | Décimas de °C; `MEASURED`. | 1 |
| Estado térmico | ✅ | ✅ | ✅ | `PowerManager.getCurrentThermalStatus` (API 29+) | Etiqueta, no grados. En API 26–28: no disponible. | 2 |
| Temperatura de CPU/GPU/skin | ❌ | 🟡 | 🟡 | `/sys/class/thermal/thermal_zone*` | Bloqueado habitualmente; nombres y escala variables por OEM. Solo si se lee de verdad. | 4 |
| Almacenamiento total/libre | ✅ | ✅ | ✅ | `StatFs` | `MEASURED`. | 1 |
| Almacenamiento por app | 🟡 | 🟡 | ✅ | `StorageStatsManager` + acceso de uso | Requiere permiso especial de uso; en algunas ROMs faltan categorías. | 3 |
| Tamaño de instalación por app | 🟡 | 🟡 | ✅ | `StorageStatsManager` | Igual que arriba; puede requerir recalcular. | 3 |
| Batería (nivel, voltaje, salud, enchufe) | ✅ | ✅ | ✅ | `ACTION_BATTERY_CHANGED` | `MEASURED`. | 1 |
| Corriente / potencia | 🟡 | 🟡 | ✅ | `BatteryManager.BATTERY_PROPERTY_CURRENT_NOW` | Signo y unidad varían por OEM; se etiqueta como estimación si no hay certeza. | 2 |
| Tiempo de actividad | ✅ | ✅ | ✅ | `SystemClock.elapsedRealtime`, `/proc/uptime` | `elapsedRealtime` incluye suspensión. | 1 |
| Uso por app (foreground) | 🟡 | 🟡 | 🟡 | `UsageStatsManager` + `PACKAGE_USAGE_STATS` | Permiso especial; no mide CPU. | 3 |
| Red (tipo, estado, IP) | ✅ | ✅ | ✅ | `ConnectivityManager`, `LinkProperties` | No requiere permisos peligrosos para lo básico. | 2 |
| Tráfico por app | 🟡 | 🟡 | 🟡 | `NetworkStatsManager` + acceso de uso | Depende de contadores del sistema; puede haber huecos. | 4 |
| Sensores (luz, proximidad) | 🟡 | 🟡 | 🟡 | `SensorManager` | Solo si el dispositivo los expone; no es prioridad. | — |

---

## 3. Procesos

| Dato / operación | S | A | R | Fuente | Notas |
|---|---|---|---|---|---|
| Lista de procesos | ❌ | ✅ | ✅ | `/proc` vía UserService de Shizuku / `ps` | Standard solo ve su propio proceso. |
| PID, nombre, cmdline | ❌ | ✅ | ✅ | `/proc/<pid>/cmdline`, `stat` | Nombres truncados en algunos kernels. |
| UID y usuario | 🟡 | ✅ | ✅ | `/proc/<pid>/status` | El UID propio sí; el resto no. |
| Paquete asociado | 🟡 | ✅ | ✅ | `PackageManager.getPackagesForUid` | Varios paquetes pueden compartir UID. |
| Estado (R/S/D/Z/T) | ❌ | ✅ | ✅ | `/proc/<pid>/stat` | Mapear a etiqueta legible. |
| RSS / VSZ | ❌ | ✅ | ✅ | `/proc/<pid>/status` | RSS no es PSS; se dice claramente. |
| Tiempo de CPU | ❌ | ✅ | ✅ | utime + stime | `CALCULATED`. |
| Hilos | ❌ | ✅ | ✅ | `/proc/<pid>/task` | Solo detalle; no en lista. |
| Prioridad / nice | ❌ | ✅ | ✅ | `stat` | Solo detalle. |
| Forzar detención | ❌ | 🔒✅ | 🔒✅ | `am force-stop <pkg>` | Solo por paquete, con confirmación. Mata la app entera, no un proceso suelto. |
| `kill` por PID | ❌ | 🟡 | 🔒🟡 | señal al PID | No se ofrece en v1: efectos impredecibles y procesos críticos. |
| Suspender app (freezer) | ❌ | ❌ | 🟡 | cgroup freezer (Android 11+) | Fuera de v1. |

**Por qué Standard no puede listar procesos:** desde Android 7/9 el montaje de
`/proc` usa `hidepid`, y `ActivityManager.getRunningAppProcesses()` devuelve solo
los procesos propios para apps normales. Prometer lo contrario sería deshonesto.

---

## 4. Aplicaciones

| Dato / operación | S | A | R | Fuente | Notas |
|---|---|---|---|---|---|
| Lista de apps instaladas | ✅ | ✅ | ✅ | `PackageManager` | Android 11+ requiere `QUERY_ALL_PACKAGES` o `<queries>`. |
| Nombre e icono | ✅ | ✅ | ✅ | `PackageManager` | Carga de iconos en IO + caché. |
| Package, versión, UID | ✅ | ✅ | ✅ | `PackageInfo` / `ApplicationInfo` | — |
| App de sistema / usuario / actualizada | ✅ | ✅ | ✅ | flags de `ApplicationInfo` | — |
| Permisos declarados y concedidos | ✅ | ✅ | ✅ | `PackageInfo.requestedPermissions` | `/proc` no interviene. |
| Tamaño de datos/caché/código | 🟡 | 🟡 | ✅ | `StorageStatsManager` | Permiso de uso. |
| Uso y última vez | 🟡 | 🟡 | 🟡 | `UsageStatsManager` | Permiso especial. |
| Abrir app | ✅ | ✅ | ✅ | `Intent` de lanzamiento | Puede fallar si no tiene actividad de launcher. |
| Abrir ajustes de la app | ✅ | ✅ | ✅ | `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` | — |
| Desinstalar | 🔒✅ | 🔒✅ | 🔒✅ | Intent de desinstalación | Siempre con confirmación del sistema. |
| Detener | ❌ | 🔒✅ | 🔒✅ | `am force-stop` | Con confirmación y sin procesos críticos. |
| Congelar / deshabilitar | ❌ | ❌ | 🟡 | `pm disable-user` | Fuera de v1 por riesgo. |
| “En segundo plano” / “activa” | ❌ | 🟡 | 🟡 | Estado de proceso / uso | No se etiqueta sin definición y fuente fiables. |

---

## 5. Acciones y administración

| Acción | Nivel mínimo | Confirmación | Registro | v1 |
|---|---|---|---|---|
| Abrir app | Standard | No | Sí | ✅ |
| Abrir ajustes de app | Standard | No | Sí | ✅ |
| Exportar sesión | Standard | Sí (SAF) | Sí | ✅ (Fase 5) |
| Borrar datos locales | Standard | Sí | Sí | ✅ |
| Force-stop de una app | Shizuku/root | Sí, con package y nombre | Sí | Fase 6 |
| Desinstalar | Standard | Diálogo del sistema | Sí | Fase 6 |
| Matar proceso por PID | Root | — | — | ❌ |
| Matar todos los procesos | — | — | — | ❌ (nunca) |
| Cambiar gobernador / frecuencias | Root | — | — | ❌ |
| Modificar LMK / límites térmicos | Root | — | — | ❌ |
| Deshabilitar apps del sistema | Root | — | — | ❌ |
| Shell libre | — | — | — | ❌ |

Toda acción privilegiada:

1. Verifica nivel y capacidad justo antes de ejecutar.
2. Muestra exactamente el paquete/proceso afectado.
3. Pide confirmación explícita.
4. Ejecuta con timeout.
5. Devuelve `ActionResult` (éxito, código, mensaje) y lo registra localmente.
6. Nunca concatena texto del usuario en un comando.

---

## 6. Fuentes y fórmulas

### `/proc/stat`

```
cpu  user nice system idle iowait irq softirq steal guest guest_nice
```

- `total = suma de todos los campos`.
- `idle = idle + iowait`.
- `uso% = 100 * (Δtotal − Δidle) / Δtotal`.
- Si `Δtotal == 0`, se devuelve no disponible (no 0 %).

### `/proc/<pid>/stat`

- Campos 14 y 15 (1-based) son `utime` y `stime` en jiffies.
- `cpu%` de un núcleo = `100 * Δ(utime+stime) / Δ(total_por_núcleo)`.
- Se necesitan dos muestras; la primera solo devuelve memoria y estado.
- El resultado se etiqueta `CALCULATED` y se explica en la ficha de la métrica.

### `/proc/meminfo`

- `MemTotal`, `MemAvailable`, `MemFree`, `Buffers`, `Cached`, `SwapTotal`, `SwapFree`.
- “Usada” se define como `MemTotal − MemAvailable` (más útil que `MemFree`).

### `/proc/<pid>/status`

- `VmRSS` (residente), `VmSize` (virtual), `Uid`, `Name`, `State`, `Threads`.
- No es PSS: la suma de RSS de procesos puede superar la RAM real por memoria compartida.

### `/proc/loadavg`

- Carga media de 1/5/15 min y procesos en ejecución/total.
- No es un porcentaje de CPU; se presenta como “carga”.

---

## 7. Compatibilidad por versión (minSdk 26 → 36)

| Función | 26–28 | 29 | 30–32 | 33–35 | 36 |
|---|---|---|---|---|---|
| RAM/almacenamiento/batería | ✅ | ✅ | ✅ | ✅ | ✅ |
| CPU total (`/proc/stat`) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Estado térmico | ❌ | ✅ | ✅ | ✅ | ✅ |
| Tamaño por app | 🟡 | 🟡 | 🟡 | 🟡 | 🟡 |
| Lista de apps | ✅ | ✅ | 🟡* | 🟡* | 🟡* |
| Usage stats | 🟡 | 🟡 | 🟡 | 🟡 | 🟡 |
| Shizuku inalámbrico | ❌ | ❌ | ❌ (11+ = 30+) | ✅ | ✅ |
| Shizuku con PC | ✅ | ✅ | ✅ | ✅ | ✅ |
| Procesos (Shizuku) | ✅ | ✅ | ✅ | ✅ | ✅** |
| `/proc` detallado (root) | ✅ | ✅ | ✅ | ✅ | ✅** |

\* Package visibility de Android 11+.
\** A validar en el spike y en el dispositivo real: las ROMs pueden endurecer
`/proc` y SELinux. Si falla, la app lo reporta; no se asume.

---

## 8. Lo que no se podrá ofrecer (ni con Shizuku)

- PSS exacto de todas las apps a 1 Hz (no existe una API barata).
- Temperatura de CPU en °C en la mayoría de dispositivos sin root.
- “App en primer plano” en tiempo real sin UsageStats o sin eventos de accesibilidad.
- Garantía de que un proceso siga vivo o muerto: Android y el OEM deciden.
- Lecturas instantáneas exactas de CPU por proceso (siempre son deltas).
- Monitorización continua fiable en segundo plano en HyperOS.
