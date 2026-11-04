# Riesgos y límites reales

## 1. Riesgos técnicos

| # | Riesgo | Prob. | Impacto | Mitigación |
|---|---|---|---|---|
| R1 | MIUIX 0.9.x es experimental y cambia APIs | Alta | Medio | Fijar 0.9.3; encapsular en `core:designsystem`; no importar MIUIX fuera de esa capa. Plan B: Material 3 + tokens propios. |
| R2 | MIUIX 0.9.3 arrastra Compose Multiplatform 1.11.1 y puede chocar con AndroidX Compose | Media | Alto | BOM 2026.06.01 (Compose 1.11.4); verificar resolución y render en el Spike 0. |
| R3 | AGP 8.13.2 + Kotlin 2.4.21: R8 puede no reconocer metadata 2.4 en release | Media | Medio | Debug primero; release sin minify hasta validar; si falla, subir a AGP 9.x (KGP 2.4 soporta hasta AGP 9.3.1). |
| R4 | Toolchain en el teléfono: poca RAM y builds lentos | Alta | Medio | Limitar workers/heap, configuration cache, build dir en f2fs; alternativa CI/PC. |
| R5 | `/proc` con hidepid bloquea procesos en Standard | Alta | Alto | No prometer procesos en Standard; Shizuku UserService como fuente real. |
| R6 | HyperOS mata la app o limita segundo plano | Alta | Medio | Monitorización solo en primer plano; pausa al salir; pedir exclusión de batería solo si el usuario lo decide. |
| R7 | Diferencias de `ps`/`top`/`dumpsys` entre ROMs | Alta | Alto | Preferir lectura directa de `/proc`; parsers tolerantes con tests de salidas reales; probar capacidades antes de usar. |
| R8 | Shizuku se detiene al reiniciar y requiere ajustes en MIUI | Alta | Medio | Detección de binder y reconexión; guía paso a paso; "USB debugging (Security options)" documentado. |
| R9 | Sin temperatura de CPU en la mayoría de casos | Alta | Bajo | Mostrar solo batería + estado térmico; no inventar. |
| R10 | `QUERY_ALL_PACKAGES` y `PACKAGE_USAGE_STATS` tienen política de Play | Media | Alto (si se publica) | Sideload primero; si va a Play, justificar o usar `<queries>` y funciones opcionales. |
| R11 | Foreground Service con tipo obligatorio en API 34+ | Media | Medio | No usar servicio persistente en v1. |
| R12 | Coste de batería por polling/shell | Media | Alto | Un solo coordinador, intervalos ≥1 s, batching, pausa en background, modo bajo consumo. |
| R13 | Expectativas de exactitud del usuario | Media | Medio | Etiquetas Medido/Calculado/Estimado y ficha de fuente por métrica. |
| R14 | Root mal usado (matar procesos críticos, bootloop) | Baja | Muy alto | Root incluido en v1 pero acotado: lista blanca, consentimiento, confirmaciones, sin shell libre y auditoría local. |
| R15 | Fuga de datos sensibles en exports o logs | Baja | Alto | Export solo por SAF, sin telemetría, logs locales acotados y borrables. |
| R16 | Koin resuelve mal una dependencia en runtime | Media | Medio | Test `checkModules()` y pruebas de navegación. |
| R17 | Compose pesado en gama baja | Media | Medio | Sin librerías de gráficas, listas con `key`, estados inmutables, perfilado en el teléfono. |
| R18 | Accesibilidad/i18n relegadas | Media | Medio | Strings en recursos desde el día 1; revisión con fuente grande y TalkBack en cada fase. |

---

## 2. Límites impuestos por Android (no se intentan eludir)

- **Procesos ajenos:** `hidepid` y APIs restringidas. Sin Shizuku/root no hay lista
  completa ni CPU por proceso.
- **Memoria por app:** PSS exacto requiere `dumpsys`/`smaps`; es caro y no sirve
  para refresco continuo.
- **Sensores térmicos:** SELinux y HALs privadas. Sin root, normalmente solo
  temperatura de batería y estado térmico.
- **Segundo plano:** Doze, App Standby y los OEM limitan alarmas, servicios y red.
- **Package visibility (11+):** la lista de apps requiere declaración explícita.
- **Scoped storage:** no se accede a datos de otras apps; exportar es vía SAF.
- **SELinux:** no se desactiva ni se evade. Si un archivo no es legible, se informa.
- **Root:** se usa solo si el usuario lo autoriza; nunca se asume.

---

## 3. Realidad por fabricante

| Fabricante / ROM | Particularidad | Efecto |
|---|---|---|
| Xiaomi / HyperOS (dispositivo de prueba) | Gestión agresiva de batería; "USB debugging (Security options)" separado | Shizuku puede requerir ajustes; la app puede morir en segundo plano. |
| Samsung One UI | Sensores y `dumpsys` recortados en algunas versiones | Más campos “No disponible”. |
| OPPO/OnePlus ColorOS | “Permission monitoring” interfiere con ADB | Shizuku necesita desactivarlo. |
| Huawei EMUI | Restricciones de ADB y de segundo plano | Advanced limitado. |
| AOSP / Pixel | Comportamiento de referencia | Menos sorpresas; útil para comparar. |

Estrategia: detectar capacidad y mostrar el motivo, no mantener una lista de
“trucos” por marca. Las guías por OEM solo para Shizuku, y siempre opcionales.

---

## 4. Seguridad y privacidad

- Principio de mínimo privilegio; permisos contextuales.
- Sin red por defecto: la app no envía datos. Exportaciones locales.
- Nada de datos de otras apps persistidos; el historial de acciones es local y
  borrable.
- Argumentos validados; nunca se concatena texto del usuario en comandos.
- Las operaciones privilegiadas son comandos concretos con lista blanca, no un
  intérprete.
- Root: separar lectura de escritura; las de escritura requieren confirmación y
  quedan auditadas.
- No se ejecutan scripts externos ni se aceptan rutas arbitrarias.

---

## 5. Estrategia de pruebas

| Nivel | Qué | Herramienta |
|---|---|---|
| Unitarias JVM | Fórmulas de CPU/RAM, parsers de `/proc`, mappers, disponibilidad | JUnit + kotlin.test |
| Parsers | Salidas reales de varios Android/OEM (fixtures) | Tests de tabla |
| Repositorios | Degradación de nivel, errores, timeouts | Dobles de fuentes |
| Koin | Grafo completo | `checkModules()` |
| UI | Navegación, estados vacío/error/permiso | Compose UI tests (Robolectric o instrumentados) |
| Manual | Instalación y uso en el Xiaomi; Shizuku on/off; rotación; fuente grande | Checklist por fase |
| Rendimiento | Arranque, jank, RAM de la app, batería en sesión de 10 min | Macrobenchmark si el entorno lo permite; si no, medición manual |

No se afirmará que una prueba pasa si no se ejecutó.

---

## 6. Criterios para parar y replantear

- Si MIUIX no compila o rompe el render en el Spike 0 → Material 3 con sistema
  visual propio; la arquitectura no cambia.
- Si Shizuku UserService no puede leer `/proc` en HyperOS → Processes queda como
  “solo root” y se comunica; no se simula.
- Si el build local no cabe en memoria → CI o PC; no se degrada la calidad del
  código para acomodar el entorno.
- Si una métrica no es fiable → se elimina de la UI antes que mostrarla mal.
