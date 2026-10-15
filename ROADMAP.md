# 🗺️ Hoja de Ruta (Roadmap) - Tinta & Trama

Este documento define la hoja de ruta de desarrollo técnico y funcional para la evolución del proyecto **Tinta & Trama**.

---

## 🎯 Fase 1: Reorganización y Refactorización del Repositorio (Corto Plazo)
- [ ] Renombrar el paquete raíz de `com.example` a `com.aistudio.tintatrama`.
- [ ] Modularizar el código por características (`feature/library`, `feature/editor`, `feature/characters`, `feature/codex`).
- [ ] Implementar inyección de dependencias con Hilt o Koin para desacoplar `TintaViewModel` y `TintaRepository`.
- [ ] Unificar el manejo de errores y estados de UI utilizando sealed interfaces (`UiState<T>`).
- [ ] Aumentar la cobertura de pruebas unitarias (`TintaViewModelTest`, `TintaRepositoryTest`, Room DAOs).

---

## 🎯 Fase 2: Experiencia de Escribir y Producción Editorial (Mediano Plazo)
- [ ] **Editor Enriquecido (Rich Text):** Soporte de negritas, cursivas, listas y títulos mediante Markdown/AnnotatedStrings.
- [ ] **Exportación Profesional:** Exportación del manuscrito completo en formatos **PDF, EPUB y Markdown (.md)**.
- [ ] **Cronómetro de Escritura y Modo Pomodoro:** Sesiones de escritura con metas diarias de palabras y estadísticas.
- [ ] **Historial de Revisiones (Versionado):** Guardado automático de versiones históricas por capítulo.

---

## 🎯 Fase 3: Worldbuilding y Herramientas Creativas (Mediano/Largo Plazo)
- [ ] **Matriz de Vínculos Avanzada:** Mapa conceptual interactivo con Canvas para conectar personajes y tramas.
- [ ] **Códice Literario Extendido:** Organización por mapas, regiones, objetos mágicos, líneas temporales y árboles genealógicos.
- [ ] **Asistente Literario Local/AI:** Integración opcional de sugerencias estéticas, corrección de estilo y brainstorming.

---

## 🎯 Fase 4: Sincronización y Multiplataforma (Largo Plazo)
- [ ] **Sincronización en la Nube:** Backup automático y sincronización multiplataforma mediante Firebase / Cloud Storage.
- [ ] **Soporte Tablet y Escritorio:** Layouts adaptativos para pantallas grandes (KMP / Compose Multiplatform).
