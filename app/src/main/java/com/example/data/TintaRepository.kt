package com.example.data

import android.content.Context
import android.net.Uri
import com.example.data.model.Chapter
import com.example.data.model.ChapterVersion
import com.example.data.model.CharacterEntity
import com.example.data.model.CodexEntry
import com.example.data.model.MarginNote
import com.example.data.model.Novel
import com.example.data.model.Relationship
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TintaRepository(private val database: AppDatabase) {

    val allNovels: Flow<List<Novel>> = database.novelDao().getAllNovels()
    val featuredNovel: Flow<Novel?> = database.novelDao().getFeaturedNovel()
    val allCharacters: Flow<List<CharacterEntity>> = database.characterDao().getAllCharacters()
    val allCodexEntries: Flow<List<CodexEntry>> = database.codexDao().getAllCodexEntries()
    val allNotes: Flow<List<MarginNote>> = database.marginNoteDao().getAllNotes()

    fun getNovelById(id: String): Flow<Novel?> = database.novelDao().getNovelById(id)

    fun getChaptersForNovel(novelId: String): Flow<List<Chapter>> =
        database.chapterDao().getChaptersByNovel(novelId)

    fun getChapterById(id: String): Flow<Chapter?> = database.chapterDao().getChapterById(id)

    fun getVersionsForChapter(chapterId: String): Flow<List<ChapterVersion>> =
        database.chapterVersionDao().getVersionsForChapter(chapterId)

    suspend fun saveChapterVersion(version: ChapterVersion) {
        database.chapterVersionDao().insertVersion(version)
    }

    fun getNotesForChapter(chapterId: String): Flow<List<MarginNote>> =
        database.marginNoteDao().getNotesForChapter(chapterId)

    fun getNotesForNovel(novelId: String): Flow<List<MarginNote>> =
        database.marginNoteDao().getNotesForNovel(novelId)

    fun getCharactersForNovel(novelId: String): Flow<List<CharacterEntity>> =
        database.characterDao().getCharactersForNovel(novelId)

    fun getRelationshipsForNovel(novelId: String): Flow<List<Relationship>> =
        database.relationshipDao().getRelationshipsForNovel(novelId)

    suspend fun recalculateNovelMetrics(novelId: String) {
        val novel = database.novelDao().getNovelById(novelId).firstOrNull() ?: return
        val chapters = database.chapterDao().getChaptersByNovel(novelId).firstOrNull() ?: emptyList()
        val totalWords = chapters.sumOf { it.wordCount }
        val chapterCount = chapters.size
        val totalTarget = if (novel.totalChapters > 0) maxOf(chapterCount, novel.totalChapters) else maxOf(1, chapterCount)
        val progress = if (totalTarget > 0) {
            ((chapterCount.toFloat() / totalTarget) * 100).toInt().coerceIn(0, 100)
        } else 0

        database.novelDao().updateNovelMetrics(
            id = novelId,
            wordCount = totalWords,
            completedChapters = chapterCount,
            totalChapters = totalTarget,
            progress = progress
        )
    }

    suspend fun saveChapterContent(chapterId: String, content: String, wordCount: Int, novelId: String) {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        database.chapterDao().updateChapterContent(chapterId, content, wordCount, currentTime)
        recalculateNovelMetrics(novelId)
    }

    suspend fun toggleChapterStatus(chapterId: String, isFinished: Boolean) {
        database.chapterDao().updateChapterStatus(chapterId, isFinished)
        val chapter = database.chapterDao().getChapterById(chapterId).firstOrNull()
        chapter?.novelId?.let { novelId ->
            recalculateNovelMetrics(novelId)
        }
    }

    suspend fun addMarginNote(note: MarginNote) {
        database.marginNoteDao().insertNote(note)
    }

    suspend fun deleteMarginNote(noteId: Long) {
        database.marginNoteDao().deleteNoteById(noteId)
    }

    suspend fun insertNovel(novel: Novel) {
        database.novelDao().insertNovel(novel)
    }

    suspend fun insertChapter(chapter: Chapter) {
        database.chapterDao().insertChapter(chapter)
        recalculateNovelMetrics(chapter.novelId)
    }

    suspend fun insertCharacter(character: CharacterEntity) {
        database.characterDao().insertCharacter(character)
    }

    suspend fun insertRelationship(relationship: Relationship) {
        database.relationshipDao().insertRelationship(relationship)
    }

    suspend fun deleteRelationship(id: Long) {
        database.relationshipDao().deleteRelationshipById(id)
    }

    suspend fun toggleNovelRead(novelId: String, isRead: Boolean) {
        database.novelDao().updateReadStatus(novelId, isRead)
    }

    suspend fun updateNovelCover(novelId: String, coverPath: String?) {
        database.novelDao().updateNovelCover(novelId, coverPath)
    }

    suspend fun updateNovelInfo(novelId: String, title: String, genre: String, subtitle: String, synopsis: String) {
        database.novelDao().updateNovelDetails(novelId, title, genre, subtitle, synopsis)
    }

    suspend fun deleteNovel(novelId: String) {
        database.novelDao().deleteNovelById(novelId)
        database.chapterDao().deleteChaptersByNovel(novelId)
        database.chapterVersionDao().deleteVersionsByNovel(novelId)
        database.marginNoteDao().deleteNotesByNovel(novelId)
        database.characterDao().deleteCharactersByNovel(novelId)
        database.relationshipDao().deleteRelationshipsByNovel(novelId)
    }

    suspend fun clearAllData() {
        database.novelDao().deleteAllNovels()
        database.chapterDao().deleteAllChapters()
        database.chapterVersionDao().deleteAllVersions()
        database.marginNoteDao().deleteAllNotes()
        database.characterDao().deleteAllCharacters()
        database.relationshipDao().deleteAllRelationships()
        database.codexDao().deleteAllCodexEntries()
    }

    suspend fun importJsonFromUri(context: Context, uri: Uri): Boolean {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (!jsonString.isNullOrBlank()) {
                importJsonData(jsonString)
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importJsonData(jsonString: String): Boolean {
        try {
            val root = JSONObject(jsonString)
            val booksArray = if (root.has("books")) {
                root.getJSONArray("books")
            } else if (root.has("novels")) {
                root.getJSONArray("novels")
            } else {
                JSONArray().put(root)
            }

            for (i in 0 until booksArray.length()) {
                val bObj = booksArray.getJSONObject(i)
                val novelId = bObj.optString("id", System.currentTimeMillis().toString())
                val title = bObj.optString("title", "Sin título")
                val syn = bObj.optString("syn", bObj.optString("synopsis", ""))
                val target = bObj.optInt("target", 20)
                val summary = bObj.optString("summary", "")
                val genre = bObj.optString("g", bObj.optString("genre", "Ficción"))
                val tropesArray = bObj.optJSONArray("t")
                val tropes = if (tropesArray != null) {
                    val list = mutableListOf<String>()
                    for (t in 0 until tropesArray.length()) list.add(tropesArray.getString(t))
                    list.joinToString(", ")
                } else {
                    bObj.optString("tropes", "")
                }

                var totalWords = 0
                var completedChapters = 0

                // Parse chapters
                if (bObj.has("chapters")) {
                    val chArray = bObj.getJSONArray("chapters")
                    for (c in 0 until chArray.length()) {
                        val cObj = chArray.getJSONObject(c)
                        val chId = cObj.optString("id", "${novelId}_cap_${c + 1}")
                        val chTitle = cObj.optString("t", cObj.optString("title", "Capítulo ${c + 1}"))
                        val chTxt = cObj.optString("txt", cObj.optString("content", ""))
                        val chDone = cObj.optBoolean("done", false)
                        val chWords = if (chTxt.isNotBlank()) chTxt.trim().split("\\s+".toRegex()).size else 0
                        totalWords += chWords
                        if (chDone) completedChapters++

                        val chapter = Chapter(
                            id = chId,
                            novelId = novelId,
                            number = c + 1,
                            title = chTitle,
                            actName = "Capítulo ${c + 1}",
                            content = chTxt,
                            wordCount = chWords,
                            isFinished = chDone,
                            lastSynced = "Sincronizado"
                        )
                        database.chapterDao().insertChapter(chapter)

                        // Notes in chapter
                        if (cObj.has("notes")) {
                            val notesArr = cObj.getJSONArray("notes")
                            for (n in 0 until notesArr.length()) {
                                val nObj = notesArr.getJSONObject(n)
                                val noteTxt = nObj.optString("t", nObj.optString("content", ""))
                                val noteColorIdx = nObj.optInt("c", 0)
                                val colorTag = when (noteColorIdx % 3) {
                                    0 -> "amber"
                                    1 -> "lavender"
                                    else -> "terracotta"
                                }
                                val note = MarginNote(
                                    novelId = novelId,
                                    chapterId = chId,
                                    category = "ANOTACIÓN",
                                    colorTag = colorTag,
                                    content = noteTxt,
                                    scopeTag = chTitle,
                                    timeLabel = "Guardado"
                                )
                                database.marginNoteDao().insertNote(note)
                            }
                        }
                    }
                }

                // Parse characters
                if (bObj.has("chars")) {
                    val charArray = bObj.getJSONArray("chars")
                    for (k in 0 until charArray.length()) {
                        val charObj = charArray.getJSONObject(k)
                        val cId = charObj.optString("id", "${novelId}_char_$k")
                        val cName = charObj.optString("n", charObj.optString("name", "Personaje"))
                        val cRole = charObj.optString("r", charObj.optString("roleTag", "Personaje"))
                        val cAge = charObj.optString("a", "18")
                        val cEco = charObj.optString("e", "Clase media")
                        val cPers = charObj.optString("p", charObj.optString("bio", ""))
                        val cHist = charObj.optString("h", "")
                        val cGoal = charObj.optString("g", "")
                        val isProtagonist = cRole.contains("Protagonista", ignoreCase = true)
                        val isDeut = cRole.contains("Deuterag", ignoreCase = true) || cRole.contains("amoroso", ignoreCase = true)

                        val character = CharacterEntity(
                            id = cId,
                            novelId = novelId,
                            name = cName,
                            roleTag = cRole,
                            epithet = cRole,
                            bio = if (cPers.isNotBlank()) cPers else cHist,
                            age = "$cAge años",
                            species = "Humano",
                            plotRoleOrMotivation = if (cGoal.isNotBlank()) cGoal else cEco,
                            traits = "$cRole, $cEco",
                            specialAbility = if (cGoal.isNotBlank()) cGoal else "Determinación",
                            crucialLink = if (cHist.isNotBlank()) cHist.take(60) else "Vínculo principal",
                            faction = cEco,
                            avatarDrawableName = if (k % 2 == 0) "avatar_kaelen" else "avatar_ren",
                            isProtagonist = isProtagonist,
                            isDeuteragonist = isDeut
                        )
                        database.characterDao().insertCharacter(character)
                    }
                }

                val progress = if (target > 0) ((completedChapters.toFloat() / target) * 100).toInt().coerceIn(0, 100) else 0

                val novel = Novel(
                    id = novelId,
                    title = title,
                    subtitle = if (summary.isNotBlank()) summary.take(60) + "..." else "Manuscrito maestro",
                    genre = genre,
                    tropes = tropes,
                    wordCount = totalWords,
                    targetWords = target * 2500,
                    completedChapters = completedChapters,
                    totalChapters = target,
                    progressPercent = progress,
                    coverDrawableName = "cover_silencio",
                    status = if (progress >= 100) "Completado" else if (progress > 50) "Revisión" else "En progreso",
                    lastEdited = "Recién importado",
                    isFeatured = true,
                    synopsis = syn
                )
                database.novelDao().insertNovel(novel)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun generateBackupJson(novels: List<Novel>, characters: List<CharacterEntity>): String {
        val root = JSONObject()
        root.put("app", "tinta-y-trama")
        root.put("v", 1)
        root.put("fecha", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()))

        val booksArray = JSONArray()
        for (novel in novels) {
            val bObj = JSONObject()
            bObj.put("id", novel.id)
            bObj.put("title", novel.title)
            bObj.put("syn", novel.synopsis)
            bObj.put("target", novel.totalChapters)
            bObj.put("summary", novel.subtitle)
            bObj.put("g", novel.genre)
            bObj.put("t", JSONArray(novel.tropes.split(",").map { it.trim() }))

            // Put characters for this novel
            val charsArray = JSONArray()
            val novelChars = characters.filter { it.novelId == novel.id }
            for (char in novelChars) {
                val cObj = JSONObject()
                cObj.put("id", char.id)
                cObj.put("n", char.name)
                cObj.put("r", char.roleTag)
                cObj.put("a", char.age.replace("[^0-9]".toRegex(), ""))
                cObj.put("e", char.faction)
                cObj.put("p", char.bio)
                cObj.put("g", char.plotRoleOrMotivation)
                charsArray.put(cObj)
            }
            bObj.put("chars", charsArray)
            booksArray.put(bObj)
        }
        root.put("books", booksArray)
        return root.toString(2)
    }
}
