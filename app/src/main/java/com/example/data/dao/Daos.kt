package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Chapter
import com.example.data.model.ChapterVersion
import com.example.data.model.CharacterEntity
import com.example.data.model.CodexEntry
import com.example.data.model.MarginNote
import com.example.data.model.Novel
import com.example.data.model.Relationship
import kotlinx.coroutines.flow.Flow

@Dao
interface NovelDao {
    @Query("SELECT * FROM novels ORDER BY isRead ASC, isFeatured DESC, title ASC")
    fun getAllNovels(): Flow<List<Novel>>

    @Query("SELECT * FROM novels WHERE id = :id LIMIT 1")
    fun getNovelById(id: String): Flow<Novel?>

    @Query("SELECT * FROM novels WHERE isFeatured = 1 LIMIT 1")
    fun getFeaturedNovel(): Flow<Novel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNovel(novel: Novel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNovels(novels: List<Novel>)

    @Update
    suspend fun updateNovel(novel: Novel)

    @Query("UPDATE novels SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: String, isRead: Boolean)

    @Query("UPDATE novels SET customCoverPath = :coverPath WHERE id = :id")
    suspend fun updateNovelCover(id: String, coverPath: String?)

    @Query("UPDATE novels SET title = :title, genre = :genre, subtitle = :subtitle, synopsis = :synopsis WHERE id = :id")
    suspend fun updateNovelDetails(id: String, title: String, genre: String, subtitle: String, synopsis: String)

    @Query("UPDATE novels SET wordCount = :wordCount, progressPercent = :progress WHERE id = :id")
    suspend fun updateNovelMetrics(id: String, wordCount: Int, progress: Int)

    @Query("DELETE FROM novels WHERE id = :id")
    suspend fun deleteNovelById(id: String)

    @Query("DELETE FROM novels")
    suspend fun deleteAllNovels()
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE novelId = :novelId ORDER BY number ASC")
    fun getChaptersByNovel(novelId: String): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    fun getChapterById(id: String): Flow<Chapter?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<Chapter>)

    @Update
    suspend fun updateChapter(chapter: Chapter)

    @Query("UPDATE chapters SET content = :content, wordCount = :wordCount, lastSynced = :syncTime WHERE id = :id")
    suspend fun updateChapterContent(id: String, content: String, wordCount: Int, syncTime: String)

    @Query("UPDATE chapters SET isFinished = :isFinished WHERE id = :id")
    suspend fun updateChapterStatus(id: String, isFinished: Boolean)

    @Query("DELETE FROM chapters WHERE novelId = :novelId")
    suspend fun deleteChaptersByNovel(novelId: String)

    @Query("DELETE FROM chapters")
    suspend fun deleteAllChapters()
}

@Dao
interface ChapterVersionDao {
    @Query("SELECT * FROM chapter_versions WHERE chapterId = :chapterId ORDER BY timestamp DESC")
    fun getVersionsForChapter(chapterId: String): Flow<List<ChapterVersion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: ChapterVersion)

    @Query("DELETE FROM chapter_versions WHERE chapterId = :chapterId")
    suspend fun deleteVersionsForChapter(chapterId: String)

    @Query("DELETE FROM chapter_versions WHERE novelId = :novelId")
    suspend fun deleteVersionsByNovel(novelId: String)

    @Query("DELETE FROM chapter_versions")
    suspend fun deleteAllVersions()
}

@Dao
interface MarginNoteDao {
    @Query("SELECT * FROM margin_notes WHERE chapterId = :chapterId ORDER BY id DESC")
    fun getNotesForChapter(chapterId: String): Flow<List<MarginNote>>

    @Query("SELECT * FROM margin_notes WHERE novelId = :novelId ORDER BY id DESC")
    fun getNotesForNovel(novelId: String): Flow<List<MarginNote>>

    @Query("SELECT * FROM margin_notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<MarginNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: MarginNote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<MarginNote>)

    @Delete
    suspend fun deleteNote(note: MarginNote)

    @Query("DELETE FROM margin_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM margin_notes WHERE novelId = :novelId")
    suspend fun deleteNotesByNovel(novelId: String)

    @Query("DELETE FROM margin_notes")
    suspend fun deleteAllNotes()
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE novelId = :novelId ORDER BY isProtagonist DESC, isDeuteragonist DESC, name ASC")
    fun getCharactersForNovel(novelId: String): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters ORDER BY name ASC")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters WHERE novelId = :novelId")
    suspend fun deleteCharactersByNovel(novelId: String)

    @Query("DELETE FROM characters")
    suspend fun deleteAllCharacters()
}

@Dao
interface RelationshipDao {
    @Query("SELECT * FROM relationships WHERE novelId = :novelId")
    fun getRelationshipsForNovel(novelId: String): Flow<List<Relationship>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: Relationship)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationships(relationships: List<Relationship>)

    @Query("DELETE FROM relationships WHERE novelId = :novelId")
    suspend fun deleteRelationshipsByNovel(novelId: String)

    @Query("DELETE FROM relationships")
    suspend fun deleteAllRelationships()
}

@Dao
interface CodexDao {
    @Query("SELECT * FROM codex_entries ORDER BY category ASC, title ASC")
    fun getAllCodexEntries(): Flow<List<CodexEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<CodexEntry>)

    @Query("DELETE FROM codex_entries")
    suspend fun deleteAllCodexEntries()
}
