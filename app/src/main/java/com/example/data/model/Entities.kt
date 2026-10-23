package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "novels")
data class Novel(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val genre: String,
    val tropes: String,
    val wordCount: Int,
    val targetWords: Int,
    val completedChapters: Int,
    val totalChapters: Int,
    val progressPercent: Int,
    val coverDrawableName: String,
    val customCoverPath: String? = null,
    val status: String,
    val lastEdited: String,
    val isFeatured: Boolean,
    val synopsis: String,
    val isTutorial: Boolean = false,
    val isRead: Boolean = false
)

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey val id: String,
    val novelId: String,
    val number: Int,
    val title: String,
    val actName: String,
    val content: String,
    val wordCount: Int,
    val isFinished: Boolean,
    val lastSynced: String
)

@Entity(tableName = "chapter_versions")
data class ChapterVersion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: String,
    val novelId: String,
    val timestamp: Long,
    val timeFormatted: String,
    val wordCount: Int,
    val content: String,
    val note: String
)

@Entity(tableName = "margin_notes")
data class MarginNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val novelId: String,
    val chapterId: String,
    val category: String,
    val colorTag: String,
    val content: String,
    val scopeTag: String,
    val timeLabel: String
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val novelId: String,
    val name: String,
    val roleTag: String,
    val epithet: String,
    val bio: String,
    val age: String,
    val species: String,
    val plotRoleOrMotivation: String,
    val traits: String,
    val specialAbility: String,
    val crucialLink: String,
    val faction: String,
    val avatarDrawableName: String,
    val isProtagonist: Boolean = false,
    val isDeuteragonist: Boolean = false
)

@Entity(tableName = "relationships")
data class Relationship(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val novelId: String,
    val fromCharacterName: String,
    val toCharacterName: String,
    val type: String,
    val description: String
)

@Entity(tableName = "codex_entries")
data class CodexEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val title: String,
    val excerpt: String,
    val content: String,
    val iconName: String
)
