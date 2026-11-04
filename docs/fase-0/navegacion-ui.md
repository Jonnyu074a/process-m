# Navegación, pantallas y guía visual

## 1. Principios de producto

- La app responde “¿cómo está el dispositivo y qué está pasando?” en menos de 5 s.
- Cada dato dice de dónde viene y qué tan fiable es.
- Lo que no se puede medir se explica; no se rellena con ceros ni ejemplos.
- Look HyperOS/MIUIX reinterpretado: oscuro, limpio, tarjetas, jerarquía fuerte.
- Una acción destructiva nunca está a un toque de distancia.

---

## 2. Mapa de navegación

```
Onboarding (solo primer arranque, saltable)
 ├─ Bienvenida
 ├─ Tema y acento
 ├─ Comprobación de compatibilidad
 ├─ Niveles de acceso (Standard / Advanced / Root)
 ├─ Shizuku (opcional)
 └─ Permisos contextuales

Main (Scaffold + barra inferior)
 ├─ Overview
 │   ├─ Acceso rápido a Processes / Applications / Live
 │   └─ “¿De dónde sale este dato?” → DataSourceSheet
 ├─ Processes
 │   ├─ Búsqueda / filtros / orden
 │   └─ ProcessDetail (sheet o pantalla) → acciones
 ├─ Applications
 │   ├─ Búsqueda / filtros
 │   └─ AppDetail → abrir, ajustes, uso, tamaño, permisos, acciones
 ├─ Live
 │   ├─ Selector de métricas e intervalo
 │   ├─ Sesión en curso / pausada
 │   └─ SessionSummary → exportar
 └─ Device
     ├─ Secciones con fuente por dato
     └─ Estado de acceso (Standard/Shizuku/root)

Ajustes (icono superior)
 ├─ Apariencia (tema, acento, movimiento)
 ├─ Monitorización (intervalo, métricas, ventana)
 ├─ Acceso avanzado (Shizuku, root)
 ├─ Privacidad y datos (exportar, borrar, historial de acciones)
 └─ Acerca de / licencias / fuentes
```

Rutas de Navigation Compose (type-safe con `@Serializable`):

```kotlin
@Serializable data object Overview
@Serializable data object Processes
@Serializable data class ProcessDetail(val pid: Int)
@Serializable data object Applications
@Serializable data class AppDetail(val packageName: String)
@Serializable data object Live
@Serializable data object Device
@Serializable data object Settings
@Serializable data class DataSource(val metricId: String)
```

Reglas:

- La barra inferior no aparece en onboarding ni en detalles a pantalla completa.
- En compacto: barra inferior con 5 destinos.
- En mediano/expandido: `NavigationRail`; en expandido, lista + detalle a dos paneles.
- “Atrás” siempre vuelve a un estado comprensible; no se pierde una sesión activa
  sin confirmación.

---

## 3. Pantallas

### 3.1 Overview

```
┌──────────────────────────────────────────┐
│ System Monitor            [Standard ▾] ⚙ │
│ Xiaomi 2303ERA42L · Android 15           │
├──────────────────────────────────────────┤
│ ┌────────────────┐ ┌───────────────────┐ │
│ │  RAM           │ │  CPU              │ │
│ │  ◯ 62 %        │ │  ／＼＿／＼ 34 %   │ │
│ │  4.8 / 7.7 GB  │ │  carga 2.1        │ │
│ └────────────────┘ └───────────────────┘ │
│ ┌──────────────────────────────────────┐ │
│ │ Almacenamiento  ▓▓▓▓▓▓▓░░░ 78 %      │ │
│ │ 102 GB libres de 228 GB              │ │
│ └──────────────────────────────────────┘ │
│ ┌───────────────┐ ┌────────────────────┐ │
│ │ Batería 71 %  │ │ Térmico: Moderado  │ │
│ │ 32.4 °C       │ │ (sin °C de CPU)    │ │
│ └───────────────┘ └────────────────────┘ │
│ Procesos 214 · Apps 187 · Uptime 3d 4h   │
├──────────────────────────────────────────┤
│  Panel   Procesos   Apps   Live   Equipo │
└──────────────────────────────────────────┘
```

- Cada tarjeta tiene un icono “i” que abre `DataSourceSheet` con fuente, método,
  intervalo y limitaciones.
- Estado de acceso visible y tocable (lleva a Ajustes → Acceso).
- Si una métrica no está disponible: tarjeta atenuada con “No disponible” y el
  motivo en una línea (“Requiere Shizuku” / “El fabricante no expone este dato”).

### 3.2 Processes

- Cabecera con contador visible (“214 procesos · Shizuku”) y aviso si es parcial.
- Buscador fijo; chips de orden: CPU, RAM, PID, Nombre, Estado.
- Fila: nombre + paquete, PID/UID, CPU %, RSS. Barra fina de CPU.
- En Standard: estado vacío explicativo, no una lista vacía sin más.
- Detalle: ficha técnica con fuente por campo, tiempos, hilos, acciones.
- CPU se muestra como “% de un núcleo” con nota; opción “% del sistema”.

### 3.3 Applications

- Buscador + filtros: Usuario / Sistema / Con uso reciente.
- Fila: icono, nombre, package, versión, tamaño si está disponible.
- Detalle: versión, UID, instalación, datos, caché, permisos, uso, botones
  “Abrir” y “Ajustes de la app”.
- Acciones peligrosas separadas visualmente (zona “Avanzado”, texto de advertencia).

### 3.4 Live Monitor

- Controles: Iniciar / Pausar, intervalo (1 s, 2 s, 5 s), métricas visibles.
- Gráficas apiladas con eje temporal y leyenda; ventana de 5 min por defecto.
- Barra de estado de la sesión: duración, muestras, fuente.
- “Detener y resumir” → SessionSummary con picos y cambios.
- Exportar CSV/JSON con SAF (el usuario elige destino).
- Al salir de la app o apagar pantalla: pausa automática y aviso la próxima vez.

### 3.5 Device

Secciones: Identidad, Sistema, Pantalla, Memoria, Almacenamiento, Batería,
Conectividad, Acceso. Cada valor con etiqueta de origen (“Sistema”, “Calculado”,
“Inferido”) cuando aplique.

### 3.6 Settings

- Apariencia: Sistema / Claro / Oscuro / AMOLED; acento; movimiento reducido.
- Monitorización: intervalo por defecto, métricas, tamaño de ventana.
- Acceso: estado de Shizuku, conectar/desconectar, explicación de root.
- Privacidad: exportar todo, borrar historial de acciones, borrar preferencias.
- Acerca de: versión, licencias (MIUIX Apache-2.0, Shizuku Apache-2.0, etc.),
  enlaces a fuentes de datos.

---

## 4. Guía visual

### 4.1 Paleta (tema oscuro AMOLED por defecto)

| Token | Valor | Uso |
|---|---|---|
| `background` | `#080808` | Fondo AMOLED. |
| `surface` | `#121212` | Tarjetas base. |
| `surfaceElevated` | `#1A1A1A` | Tarjetas destacadas, hojas. |
| `surfaceHighest` | `#222222` | Diálogos, menús. |
| `textPrimary` | `#F2F2F2` | Títulos y valores. |
| `textSecondary` | `#A0A0A0` | Etiquetas y unidades. |
| `outline` | `#2A2A2A` | Separadores. |
| `accent` | configurable (semilla) | Gráficas, selección, CTA. |
| `success` | `#4CAF82` | Estado saludable. |
| `warning` | `#E0A33E` | Atención. |
| `danger` | `#E05A5A` | Errores y acciones destructivas. |
| `info` | `#5B9BE0` | Información y enlaces. |

- El acento se genera con `ThemeController(keyColor = …)` de MIUIX (Monet).
- Nunca se usa solo color para comunicar estado: siempre icono + texto.
- Contraste mínimo AA (4.5:1 para texto normal).

### 4.2 Tipografía y espaciado

- Tipografía del tema MIUIX; escala: display 32, title 22, subtitle 17, body 15,
  caption 12, mono 13 para PID/direcciones.
- Retícula de 4 dp; espaciado habitual 8/12/16/24.
- Radios: 16 (chips), 20 (filas), 24–28 (tarjetas y hojas).
- Elevación por superficie, no por sombras fuertes.

### 4.3 Componentes propios (`core:designsystem`)

- `MetricCard`: título, valor, unidad, estado, acción “fuente”.
- `RingGauge`: anillo de progreso con umbral y etiqueta central.
- `Sparkline` y `LineChart`: Canvas, sin librerías externas; máximo 300 puntos.
- `UsageBar`: barra segmentada (usado/caché/libre).
- `StatusChip`: Standard/Advanzado/Root, con icono.
- `StateHost`: carga (skeleton), vacío, error con reintento, permiso denegado.
- `SourceBadge`: “Medido”, “Calculado”, “Estimado”, “No disponible”.
- `ConfirmActionSheet`: qué se va a hacer, a qué app/proceso, consecuencia y
  botón destructivo separado.

MIUIX se usa a través de estos componentes y del tema; las pantallas no deberían
importar decenas de símbolos de MIUIX directamente. Eso permite cambiar de
librería sin reescribir features si el proyecto experimental falla.

### 4.4 Movimiento

- Transiciones de pantalla 200–250 ms; aparición de tarjetas 150 ms con fade+slide
  corto; actualización de valores sin rebote.
- Gráficas: desplazamiento continuo suave, sin animar cada punto.
- Haptics discretos en acciones y confirmaciones (respetando ajustes).
- Respetar “reducir movimiento”: sin desplazamientos largos ni animaciones en bucle.

### 4.5 Estados obligatorios

| Estado | Comportamiento |
|---|---|
| Cargando | Skeleton con la forma del contenido, sin spinner global. |
| Vacío | Explicación + acción sugerida (“Concede acceso de uso”). |
| Error | Mensaje corto, detalle técnico plegable, botón Reintentar. |
| Permiso denegado | Qué permiso, para qué sirve, botón a Ajustes. |
| Nivel insuficiente | Candado + “Requiere Shizuku” + cómo activarlo. |
| Dato no soportado | “No disponible en este dispositivo” + motivo. |
| Dato estimado | Badge “Calculado”/“Estimado” + explicación. |

---

## 5. Accesibilidad

- Objetivos táctiles ≥ 48 dp.
- `contentDescription` en iconos y gráficas (resumen textual del último valor).
- Orden de foco lógico; soporte de TalkBack y switch access.
- Escalado de fuente hasta 200 % sin cortes; layouts que envuelven.
- No depender del color; estados con texto e icono.
- Semántica de gráficas: valor actual + mínimo/máximo de la ventana.

---

## 6. Adaptación

| Ancho | Navegación | Detalle |
|---|---|---|
| Compacto (<600 dp) | Barra inferior | Pantalla completa o sheet. |
| Mediano (600–840 dp) | Rail lateral | Panel lateral. |
| Expandido (>840 dp) | Rail + lista | Dos paneles. |

- Soporte vertical y horizontal; en horizontal, las tarjetas pasan a dos columnas.
- Insets de sistema gestionados en el `Scaffold`; edge-to-edge desde el inicio.

---

## 7. Copys y tono

- Español directo, sin tecnicismos innecesarios; el detalle técnico está a un toque.
- “No disponible” siempre con motivo: “Android no expone esta métrica sin Shizuku”.
- Nunca “matar” en la UI principal: “Forzar detención” y explicación de la consecuencia.
- No usar “optimizar” para acciones que solo cierran apps.

---

## 8. Primer arranque

1. Bienvenida: qué hace y qué no hace (sin promesas falsas).
2. Tema y acento (se puede cambiar luego).
3. Comprobación: versión, fabricante, acceso, permisos ya concedidos.
4. Niveles: Standard / Advanced / Root, con qué desbloquea cada uno.
5. Shizuku: estado y enlace a la app; se puede omitir.
6. Permisos: solo al entrar en la función que los necesita (no todos de golpe).
7. Dashboard.

El onboarding se puede saltar entero y no bloquea funciones Standard.
