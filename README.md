# 🖋️ Tinta & Trama

**Atelier literario y estudio editorial móvil para autores.**

Tinta & Trama es una aplicación Android nativa desarrollada con Jetpack Compose, Kotlin Coroutines, Flow y Room Database, diseñada específicamente para escritores y autores que desean planificar, estructurar, escribir y gestionar sus obras literarias desde sus dispositivos móviles.

---

## 🚀 Características Principales

- 📚 **Biblioteca de Manuscritos:** Gestión de obras con metas de palabras, seguimiento de avance e importación/exportación JSON.
- 📖 **Estructura de la Obra (Trama):** Desglose por capítulos, organización temática y vistas resumidas del avance.
- ✍️ **Editor Inmersivo (Escribir):** Espacio de escritura minimalista con modo Zen, estilos de papel/fuentes, notas al margen y contador de palabras en tiempo real.
- 👤 **Elenco & Grafo de Personajes:** Fichas personalizadas de personajes con roles, alineaciones, motivaciones y visualización de relaciones/vínculos.
- 🎨 **Recursos & Códice:** Tablero de notas rápidas, worldbuilding y banco de ideas.
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
