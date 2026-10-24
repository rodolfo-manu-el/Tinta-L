# 🖋️ Tinta & Trama

**Atelier literario y estudio editorial móvil para autores.**

Tinta & Trama es una aplicación Android nativa desarrollada con Jetpack Compose, Kotlin Coroutines, Flow y Room Database, diseñada específicamente para escritores y autores que desean planificar, estructurar, escribir y gestionar sus obras literarias desde sus dispositivos móviles.

---

## 🚀 Características Principales

- 📚 **Biblioteca de Manuscritos:** Selector de vista (Lista Limpia y Cuadrícula de Libros), portadas personalizadas (2:3), filtrado inteligente y gestión limpia de acciones contextuales.
- 📥 **Importación Multiformato:** Importa directamente archivos `.epub` (convirtiendo XHTML a Markdown), `.docx` (preservando negritas, cursivas y encabezados) y `.md` mediante Storage Access Framework (SAF).
- 📤 **Exportación Editorial:** Exportación nativa y guardado directo con SAF a PDF maquetado (con portada y numeración de página), `.epub`, `.docx` y `.md`.
- ✍️ **Atelier de Escritura & Editor WYSIWYG:** Guardado automático optimizado con debouncing (500ms), respaldos/snapshots de versión cada 5 minutos, estilos de papel (Pergamino, Rayado, Retícula, Blanco) y tipografías literarias.
- 📖 **Modo Lectura 3D Flip-Book:** Lector a pantalla completa con efecto táctil 3D de curvatura y vuelta de página.
- ⏱️ **Cronómetro Pomodoro Zen:** Temporizador de sesiones de escritura con indicador de palabras redactadas en tiempo real y sonido armónico de finalización (RingtoneManager/ToneGenerator).
- 📌 **Mapa de Corcho Interactivo:** Tablero con chinchetas móviles para personajes e hilos de relación coloreados por tipo de vínculo.
- 🗝️ **Códice & Worldbuilding:** Enciclopedia del universo con categorías (Lugares, Artefactos, Magia, Facciones) e inspiraciones de worldbuilding.
- 🌙 **Diseño Adaptativo:** Modos Pergamino Claro y Noche Inmersiva con estética editorial.

---

## 🛠️ Arquitectura y Tecnologías

El proyecto utiliza las prácticas modernas de desarrollo de Android recomendadas por Google:

- **Lenguaje:** Kotlin 2.2+
- **UI:** Jetpack Compose + Material 3 Design
- **Persistencia Local:** Room Database
- **Gestión de Estado & Concurrencia:** ViewModel, StateFlow, Kotlin Coroutines
- **Inyección / Configuración:** Modern Gradle Setup (Catalog `libs.versions.toml`)

---

## 📂 Estructura del Proyecto

```text
.
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/             # Capa de Datos (Entities, DAOs, Database, Repository)
│   │   │   │   ├── ui/               # Capa de UI (Screens, Components, ViewModels, Theme)
│   │   │   │   └── MainActivity.kt    # Punto de entrada principal
│   │   │   ├── assets/               # Plantillas y datos iniciales (ej. summer_love.json)
│   │   │   └── res/                  # Recursos gráficos, fuentes (Newsreader, Plus Jakarta Sans), strings
│   │   └── test/                     # Pruebas unitarias
│   └── build.gradle.kts
├── docs/                             # Documentación extendida y planes de mejora
│   └── PLAN_DE_MEJORA.md
├── gradle/                           # Version Catalog y Gradle Wrapper
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Cómo Empezar

1. Clonar el repositorio.
2. Abrir el proyecto en **Android Studio (Ladybug / Studio 2024.2+)**.
3. Asegurarse de tener instalado JDK 17 o superior.
4. Sincronizar Gradle e iniciar el proyecto en un emulador o dispositivo físico.

---

## 🗺️ Roadmap de Desarrollo

Para consultar la hoja de ruta detallada e hitos proyectados, consulta [ROADMAP.md](./ROADMAP.md) y [docs/PLAN_DE_MEJORA.md](./docs/PLAN_DE_MEJORA.md).
