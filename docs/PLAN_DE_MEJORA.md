# 📋 Plan de Mejora Integral: Tinta & Trama

Este documento presenta una guía paso a paso para **organizar mejor el repositorio** y **desarrollar nuevas funcionalidades** en la aplicación **Tinta & Trama**.

---

## 🏛️ PARTE 1: Plan para Organizar y Estructurar Mejor el Repositorio

Actualmente el repositorio tiene una base muy sólida con **Jetpack Compose**, **Room**, **Coroutines** y **Flow**, pero todo el código está concentrado bajo el paquete genérico `com.example` y en pocos archivos muy extensos.

### 1.1 Reorganización del Paquete Raíz y Naming Conventions
- **Paso actual:** El paquete raíz es `com.example`.
- **Acción recomendada:** Renombrar el paquete raíz a `com.aistudio.tintatrama` (coincidiendo con el `applicationId` definido en `build.gradle.kts`).
- **Estructura propuesta por capas / features:**
  ```text
  com.aistudio.tintatrama/
  ├── core/
  │   ├── database/         # AppDatabase, TypeConverters, DAOs
  │   ├── model/            # Novel, Chapter, CharacterEntity, etc.
  │   ├── designsystem/     # Theme, Type, Color, Componentes base (TintaToast, Buttons)
  │   └── network/          # Módulos de red / Retrofit / Firebase (si aplica)
  ├── data/
  │   └── repository/       # TintaRepository e Interfaces de Repositorio
  └── feature/
      ├── library/          # BibliotecaScreen, BibliotecaViewModel/State
      ├── obra/             # ObraScreen (Estructura de la novela y capítulos)
      ├── editor/           # EscribirScreen (Editor Zen, notas al margen)
      ├── characters/       # PersonajesScreen (Fichas y mapa de vínculos)
      └── codex/            # RecursosScreen (Códice, notas rápidas, worldbuilding)
  ```

### 1.2 Modularización de la UI y ViewModels
- **Problema actual:** `TintaViewModel.kt` centraliza la lógica de las 5 pestañas principales (Biblioteca, Obra, Escribir, Personajes, Recursos), además de la gestión de backups e importación.
- **Acción recomendada:**
  1. Dividir `TintaViewModel` en ViewModels específicos por feature:
     - `LibraryViewModel`: Búsqueda, filtros, importación/exportación JSON.
     - `EditorViewModel`: Edición de capítulos, modo Zen, fuentes/papeles, contador de palabras.
     - `CharactersViewModel`: Fichas de personajes, filtros de facción, grafo de relaciones.
     - `CodexViewModel`: Categorías de notas, worldbuilding e ideas.
  2. Utilizar **Inyección de Dependencias (Hilt o Koin)** para proveer instancias del repositorio y base de datos sin acoplamiento manual.

### 1.3 Calidad de Código, CI/CD y Linters
- **Agregar Ktlint / Spotless:** Integrar plugins de formato automático para garantizar un estilo de código consistente.
- **Git Hooks (Pre-commit / Pre-push):** Ejecutar verificación de compilación y tests automáticos antes de cada commit.
- **Configuración de CI en GitHub Actions:**
  - Job de compilación (`./gradlew assembleDebug`).
  - Job de ejecución de tests unitarios (`./gradlew test`).

---

## 🚀 PARTE 2: Plan de Mejora y Desarrollo del Producto (Roadmap Funcional)

Dividido en 4 fases evolutivas clasificadas por impacto y prioridad:

### 🏆 FASE 1: Experiencia de Escritura e Interactividad (Prioridad Alta)
1. **Editor con Formato Enriquecido (Rich Text / Markdown):**
   - Permitir al autor formatear texto (**Negrita**, *Cursiva*, Encabezados, Citas literarias, Listas).
   - Renderizado dinámico mediante Jetpack Compose `AnnotatedString` o librerías de Rich Text.
2. **Historial de Versiones y Guardado de Borradores:**
   - Crear una tabla `ChapterVersion` para guardar automáticos ("snapshots") cada N minutos o al pausar la escritura.
   - Permitir restaurar versiones previas de un capítulo.
3. **Modo Pomodoro / Temporizador de Escritura:**
   - Contador regresivo/progresivo para sesiones de escritura concentrada (ej. 25 min).
   - Registro de velocidad de escritura (Palabras por minuto / palabras por sesión).

---

### 🎨 FASE 2: Worldbuilding Avanzado y Estructura Narrativa (Prioridad Media-Alta)
1. **Matriz Conceptual de Vínculos Interactiva:**
   - Reemplazar las fichas fijas del "Mapa de relaciones" por un **Canvas interactivo** con nodos arrastables (drag & drop).
   - Conexiones con colores segun la relación (Aliados, Enemigos, Amor, Familia).
2. **Códice Expandido de Worldbuilding:**
   - Crear secciones dedicadas a:
     - **Geografía y Localizaciones** (con mapas interactivos o imágenes anexas).
     - **Sistemas de Magia / Tecnología / Leyes del mundo**.
     - **Líneas del Tiempo (Timelines)** para eventos cronológicos.
3. **Organizador de Capítulos (Drag & Drop Reordering):**
   - Permitir reordenar capítulos mediante arrastrar y soltar en la pantalla `Obra`.

---

### 📤 FASE 3: Exportación Editorial y Productividad (Prioridad Media)
1. **Exportación Multiformato:**
   - **PDF:** Renderizado con maquetación editorial (fuente elegante, numeración de páginas).
   - **EPUB:** Formato estándar para e-readers (Kindle, Kobo).
   - **TXT / Markdown (.md):** Para respaldos limpios.
2. **Panel de Estadísticas del Autor:**
   - Gráficos de barras y líneas con avances diarios/semanales.
   - Cálculo estimado del tiempo de lectura total de la obra.

---

### ☁️ FASE 4: Sincronización y Colaboración (Prioridad Futura)
1. **Sincronización en la Nube (Firebase Cloud Firestore / Google Drive):**
   - Respaldo automático sin necesidad de exportar manual el archivo `.json`.
   - Sincronización entre teléfono y tablet.
2. **Asistente Creativo Local / Sugerencias de Estilo:**
   - Detección de palabras repetidas o muletillas en el texto.
   - Generador local de nombres para personajes o lugares.

---

## 📌 Recomendación de Inicio (¿Por dónde empezar?)

Para avanzar de forma ordenada y sin romper funcionalidad existente:

1. **Paso 1 (Organización):** Crear los paquetes por capas/features en `com/example/ui` y agrupar componentes.
2. **Paso 2 (Desarrollo):** Implementar el **Editor con formato enriquecido (Markdown)** en `EscribirScreen.kt`.
3. **Paso 3 (Desarrollo):** Implementar la **Exportación de Manuscrito a TXT / PDF / EPUB**.
