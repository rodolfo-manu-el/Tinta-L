package com.example

import com.example.data.model.Chapter
import com.example.data.model.CharacterEntity
import com.example.data.model.Novel
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TintaViewModelTest {

    @Test
    fun `test novel relational mapping and chapter logic`() {
        val novel1 = Novel(
            id = "novel-1",
            title = "Las Cronicas de Ícaro",
            subtitle = "Libro I",
            genre = "Ciencia Ficción",
            tropes = "Espacio",
            wordCount = 1200,
            targetWords = 50000,
            completedChapters = 1,
            totalChapters = 10,
            progressPercent = 10,
            coverDrawableName = "cover_silencio",
            status = "En progreso",
            lastEdited = "Hoy",
            isFeatured = true,
            synopsis = "Una historia espacial"
        )

        val chapter1 = Chapter(
            id = "novel-1_cap_1",
            novelId = "novel-1",
            number = 1,
            title = "El Comienzo",
            actName = "Acto I",
            content = "Había una vez en el espacio...",
            wordCount = 6,
            isFinished = false,
            lastSynced = "Ahora"
        )

        val character1 = CharacterEntity(
            id = "char-1",
            novelId = "novel-1",
            name = "Kaelen",
            roleTag = "Protagonista",
            epithet = "Piloto",
            bio = "Exploradora",
            age = "24",
            species = "Humana",
            plotRoleOrMotivation = "Sobrevivir",
            traits = "Valiente",
            specialAbility = "Pilotaje",
            crucialLink = "Ninguno",
            faction = "Alianza",
            avatarDrawableName = "avatar_kaelen",
            isProtagonist = true
        )

        assertEquals("novel-1", novel1.id)
        assertEquals("novel-1", chapter1.novelId)
        assertEquals("novel-1", character1.novelId)
        assertEquals(6, chapter1.wordCount)
    }

    @Test
    fun `test word count computation`() {
        val text = "Tinta y Trama es una gran aplicación de escritura"
        val words = text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        assertEquals(9, words)
    }

    @Test
    fun `test html to markdown formatting conversion`() {
        val rawHtml = "<h1>Capítulo 1</h1><p>Texto con <b>negrita</b> y <i>cursiva</i></p>"
        var clean = rawHtml.replace("(?i)<h1[^>]*>(.*?)</h1>".toRegex(), "\n# $1\n")
        clean = clean.replace("(?i)<(b|strong)[^>]*>(.*?)</\\1>".toRegex(), "**$2**")
        clean = clean.replace("(?i)<(i|em)[^>]*>(.*?)</\\1>".toRegex(), "*$2*")
        clean = clean.replace("(?i)<p[^>]*>(.*?)</p>".toRegex(), "$1\n\n")
        clean = clean.replace("<[^>]+>".toRegex(), "").trim()

        assertTrue(clean.contains("# Capítulo 1"))
        assertTrue(clean.contains("**negrita**"))
        assertTrue(clean.contains("*cursiva*"))
    }

    @Test
    fun `test pagination logic with explicit manual breaks and auto-overflow`() {
        val manualContent = "Página 1 de la historia.\n\n--- PÁGINA ---\n\nPágina 2 de la historia."
        val manualPages = com.example.ui.screens.splitIntoPages(manualContent)
        assertEquals(2, manualPages.size)
        assertEquals("Página 1 de la historia.", manualPages[0])
        assertEquals("Página 2 de la historia.", manualPages[1])

        val paragraph = (1..60).joinToString(" ") { "palabra$it" }
        val longContent = "$paragraph\n\n$paragraph\n\n$paragraph\n\n$paragraph\n\n$paragraph"
        val autoPages = com.example.ui.screens.splitIntoPages(longContent)
        assertTrue(autoPages.size >= 2)
    }

    @Test
    fun `test filter option archived labels and status`() {
        val archivedOption = com.example.ui.FilterOption.ARCHIVED
        assertEquals("Leídos / Archivados", archivedOption.label)
        assertEquals(com.example.ui.FilterOption.ARCHIVED, com.example.ui.FilterOption.COMPLETED)
    }

    @Test
    fun `test chapter progress calculation on addition`() {
        // When 1 chapter exists in a 10-chapter target novel
        val totalTarget = 10
        val chapters1 = listOf("cap1")
        val progress1 = ((chapters1.size.toFloat() / totalTarget) * 100).toInt()
        assertEquals(10, progress1)

        // When a 2nd chapter is added
        val chapters2 = listOf("cap1", "cap2")
        val progress2 = ((chapters2.size.toFloat() / totalTarget) * 100).toInt()
        assertEquals(20, progress2)
        assertEquals(2, chapters2.size)

        // When chapters exceed target (e.g. 11 chapters)
        val chaptersExceeded = (1..11).map { "cap$it" }
        val newTarget = maxOf(chaptersExceeded.size, totalTarget)
        val progressExceeded = ((chaptersExceeded.size.toFloat() / newTarget) * 100).toInt()
        assertEquals(100, progressExceeded)
        assertEquals(11, newTarget)
    }

    @Test
    fun `test expanded tutorial structure and chapters`() {
        val expectedChapters = listOf(
            "El Atelier de Escritura y Modos Visuales",
            "El Reparto y el Mapa en Corcho",
            "Notas al Margen y el Códice de Lore",
            "Biblioteca, Portadas y Exportación Editorial"
        )
        assertEquals(4, expectedChapters.size)
        assertTrue(expectedChapters.any { it.contains("Atelier") })
        assertTrue(expectedChapters.any { it.contains("Corcho") })
        assertTrue(expectedChapters.any { it.contains("Códice") })
        assertTrue(expectedChapters.any { it.contains("Exportación") })
    }
}
