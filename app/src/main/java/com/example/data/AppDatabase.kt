package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ChapterDao
import com.example.data.dao.ChapterVersionDao
import com.example.data.dao.CharacterDao
import com.example.data.dao.CodexDao
import com.example.data.dao.MarginNoteDao
import com.example.data.dao.NovelDao
import com.example.data.dao.RelationshipDao
import com.example.data.model.Chapter
import com.example.data.model.ChapterVersion
import com.example.data.model.CharacterEntity
import com.example.data.model.CodexEntry
import com.example.data.model.MarginNote
import com.example.data.model.Novel
import com.example.data.model.Relationship
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Novel::class,
        Chapter::class,
        ChapterVersion::class,
        MarginNote::class,
        CharacterEntity::class,
        Relationship::class,
        CodexEntry::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun novelDao(): NovelDao
    abstract fun chapterDao(): ChapterDao
    abstract fun chapterVersionDao(): ChapterVersionDao
    abstract fun marginNoteDao(): MarginNoteDao
    abstract fun characterDao(): CharacterDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun codexDao(): CodexDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tinta_trama_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
