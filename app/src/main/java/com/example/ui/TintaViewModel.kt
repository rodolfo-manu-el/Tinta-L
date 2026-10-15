package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TintaRepository
import com.example.data.model.Chapter
import com.example.data.model.ChapterVersion
import com.example.data.model.CharacterEntity
import com.example.data.model.CodexEntry
import com.example.data.model.MarginNote
import com.example.data.model.Novel
import com.example.data.model.Relationship
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class TintaTab(val label: String, val iconName: String) {
    BIBLIOTECA("Biblioteca", "local_library"),
    OBRA("Obra", "view_timeline"),
    ESCRIBIR("Escribir", "history_edu"),
    PERSONAJES("Personajes", "group"),
    RECURSOS("Recursos", "auto_awesome")
}

/** Opciones de filtro disponibles en la Biblioteca. */
enum class FilterOption(val label: String) {
    ALL("Todos"),
    IN_PROGRESS("En progreso"),
    COMPLETED("Terminadas")
}

class TintaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TintaRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = TintaRepository(db)
    }

    val allNovels: StateFlow<List<Novel>> = repository.allNovels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredNovel: StateFlow<Novel?> = repository.featuredNovel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCharacters: StateFlow<List<CharacterEntity>> = repository.allCharacters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCodexEntries: StateFlow<List<CodexEntry>> = repository.allCodexEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(TintaTab.BIBLIOTECA)
    val currentTab: StateFlow<TintaTab> = _currentTab.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenreFilter = MutableStateFlow("Todos")
    val selectedGenreFilter: StateFlow<String> = _selectedGenreFilter.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterOption.ALL)
    val selectedFilter: StateFlow<FilterOption> = _selectedFilter.asStateFlow()

    /**
     * Lista de novelas filtrada reactivamente.
     * Combina el flujo de Room con [searchQuery] y [selectedFilter] para
     * actualizar la UI en tiempo real sin consultas adicionales a la BD.
     */
    val filteredNovels: StateFlow<List<Novel>> = combine(
        repository.allNovels,
        _searchQuery,
        _selectedFilter
    ) { novels, query, filter ->
        novels.filter { novel ->
            val matchesQuery = query.isBlank() ||
                novel.title.contains(query, ignoreCase = true) ||
                novel.synopsis.contains(query, ignoreCase = true) ||
                novel.genre.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                FilterOption.ALL -> true
                FilterOption.COMPLETED -> novel.status.equals("Terminada", ignoreCase = true) ||
                    novel.status.equals("Revisión", ignoreCase = true)
                FilterOption.IN_PROGRESS -> !novel.status.equals("Terminada", ignoreCase = true) &&
                    !novel.status.equals("Revisión", ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // selectedNovelId tracks the active novel currently being edited/viewed in Room
    private val _selectedNovelId = MutableStateFlow<String?>(null)
    val selectedNovelId: StateFlow<String?> = _selectedNovelId.asStateFlow()

    // activeNovelId alias maintained for backwards compatibility
    private val _activeNovelId = MutableStateFlow("")
    val activeNovelId: StateFlow<String> = _activeNovelId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentNovel: StateFlow<Novel?> = _selectedNovelId
        .flatMapLatest { id ->
            if (!id.isNullOrBlank()) {
                repository.getNovelById(id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeChapters: StateFlow<List<Chapter>> = _selectedNovelId
        .flatMapLatest { id ->
            if (!id.isNullOrBlank()) {
                repository.getChaptersForNovel(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeCharacters: StateFlow<List<CharacterEntity>> = _selectedNovelId
        .flatMapLatest { id ->
            if (!id.isNullOrBlank()) {
                repository.getCharactersForNovel(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeRelationships: StateFlow<List<Relationship>> = _selectedNovelId
        .flatMapLatest { id ->
            if (!id.isNullOrBlank()) {
                repository.getRelationshipsForNovel(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeChapterId = MutableStateFlow("")
    val activeChapterId: StateFlow<String> = _activeChapterId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeNotes: StateFlow<List<MarginNote>> = combine(_selectedNovelId, _activeChapterId) { novelId, chapterId ->
        Pair(novelId, chapterId)
    }.flatMapLatest { (novelId, chapterId) ->
        if (!chapterId.isNullOrBlank()) {
            repository.getNotesForChapter(chapterId)
        } else if (!novelId.isNullOrBlank()) {
            repository.getNotesForNovel(novelId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val chapterVersions: StateFlow<List<ChapterVersion>> = _activeChapterId
        .flatMapLatest { chapterId ->
            if (chapterId.isNotBlank()) {
                repository.getVersionsForChapter(chapterId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chapter State
    private val _chapterContent = MutableStateFlow("")
    val chapterContent: StateFlow<String> = _chapterContent.asStateFlow()

    private val _chapterWordCount = MutableStateFlow(0)
    val chapterWordCount: StateFlow<Int> = _chapterWordCount.asStateFlow()

    private val _isChapterFinished = MutableStateFlow(false)
    val isChapterFinished: StateFlow<Boolean> = _isChapterFinished.asStateFlow()

    private val _paperTexture = MutableStateFlow("plain")
    val paperTexture: StateFlow<String> = _paperTexture.asStateFlow()

    private val _selectedFont = MutableStateFlow("Newsreader")
    val selectedFont: StateFlow<String> = _selectedFont.asStateFlow()

    private val _isZenMode = MutableStateFlow(false)
    val isZenMode: StateFlow<Boolean> = _isZenMode.asStateFlow()

    // Pomodoro / Writing Session Timer State
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerSeconds = MutableStateFlow(25 * 60) // Default 25 minutes
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _wordsWrittenInSession = MutableStateFlow(0)
    val wordsWrittenInSession: StateFlow<Int> = _wordsWrittenInSession.asStateFlow()

    private var initialSessionWords = -1
    private var timerJob: Job? = null

    private val _personajesTab = MutableStateFlow("fichas") // "fichas" or "mapa"
    val personajesTab: StateFlow<String> = _personajesTab.asStateFlow()

    private val _characterFactionFilter = MutableStateFlow<String?>(null)
    val characterFactionFilter: StateFlow<String?> = _characterFactionFilter.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isSynopsisExpanded = MutableStateFlow(false)
    val isSynopsisExpanded: StateFlow<Boolean> = _isSynopsisExpanded.asStateFlow()

    private val _showCreateModal = MutableStateFlow(false)
    val showCreateModal: StateFlow<Boolean> = _showCreateModal.asStateFlow()

    private val _createType = MutableStateFlow("novel") // "novel", "chapter", "character", "note"
    val createType: StateFlow<String> = _createType.asStateFlow()

    private val _showBackupDialog = MutableStateFlow(false)
    val showBackupDialog: StateFlow<Boolean> = _showBackupDialog.asStateFlow()


    private val _backupJsonString = MutableStateFlow<String?>(null)
    val backupJsonString: StateFlow<String?> = _backupJsonString.asStateFlow()

    init {
        viewModelScope.launch {
            allNovels.collect { novels ->
                if (_selectedNovelId.value.isNullOrBlank() && novels.isNotEmpty()) {
                    val defaultNovel = novels.firstOrNull { it.isFeatured } ?: novels.first()
                    _selectedNovelId.value = defaultNovel.id
                    _activeNovelId.value = defaultNovel.id
                    
                    // Auto-select initial chapter if available
                    val chapters = repository.getChaptersForNovel(defaultNovel.id).firstOrNull() ?: emptyList()
                    if (chapters.isNotEmpty()) {
                        selectChapter(chapters.first().id)
                    }
                }
            }
        }
    }

    fun selectTab(tab: TintaTab) {
        _currentTab.value = tab
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        showToast(if (_isDarkTheme.value) "Modo Noche Activado" else "Modo Pergamino Claro Activado")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGenreFilter(genre: String) {
        _selectedGenreFilter.value = genre
    }

    // --- Métodos de evento para búsqueda y filtros reactivos ---

    /** Actualiza la consulta de búsqueda; dispara el re-filtrado de [filteredNovels]. */
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    /** Selecciona una opción de filtro; dispara el re-filtrado de [filteredNovels]. */
    fun onFilterSelected(filter: FilterOption) {
        _selectedFilter.value = filter
    }

    /** Limpia el campo de búsqueda; restaura la lista sin filtro de texto. */
    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun selectNovel(novelId: String) {
        _selectedNovelId.value = novelId
        _activeNovelId.value = novelId
        _currentTab.value = TintaTab.OBRA

        viewModelScope.launch {
            val chapters = repository.getChaptersForNovel(novelId).firstOrNull() ?: emptyList()
            if (chapters.isNotEmpty()) {
                val existing = chapters.find { it.id == _activeChapterId.value }
                if (existing != null) {
                    selectChapter(existing.id)
                } else {
                    selectChapter(chapters.first().id)
                }
            } else {
                _activeChapterId.value = ""
                _chapterContent.value = ""
                _chapterWordCount.value = 0
                _isChapterFinished.value = false
            }
        }
    }

    fun setActiveNovel(novelId: String) {
        selectNovel(novelId)
    }

    fun selectChapter(chapterId: String) {
        _activeChapterId.value = chapterId
        viewModelScope.launch {
            val chapter = repository.getChapterById(chapterId).firstOrNull()
            chapter?.let {
                _chapterContent.value = it.content
                _chapterWordCount.value = it.wordCount
                _isChapterFinished.value = it.isFinished
            }
        }
    }

    fun createChapter(title: String = "") {
        val novelId = _selectedNovelId.value
        if (novelId.isNullOrBlank()) {
            showToast("Primero selecciona una historia")
            return
        }
        viewModelScope.launch {
            val currentChapters = repository.getChaptersForNovel(novelId).firstOrNull() ?: emptyList()
            val nextNum = currentChapters.size + 1
            val chId = "${novelId}_cap_${System.currentTimeMillis()}"
            val chTitle = title.ifBlank { "Capítulo $nextNum" }
            val newChapter = Chapter(
                id = chId,
                novelId = novelId,
                number = nextNum,
                title = chTitle,
                actName = "Capítulo $nextNum",
                content = "",
                wordCount = 0,
                isFinished = false,
                lastSynced = "Ahora"
            )
            repository.insertChapter(newChapter)
            selectChapter(chId)
            showToast("Capítulo '$chTitle' creado")
        }
    }

    fun openWritingStudio(chapterId: String = "") {
        _currentTab.value = TintaTab.ESCRIBIR
        val novelId = _selectedNovelId.value ?: return
        viewModelScope.launch {
            val targetChapter = if (chapterId.isNotBlank()) {
                repository.getChapterById(chapterId).firstOrNull()
            } else {
                repository.getChaptersForNovel(novelId).firstOrNull()?.firstOrNull()
            }
            targetChapter?.let {
                selectChapter(it.id)
            }
        }
    }

    fun updateChapterContent(newContent: String) {
        _chapterContent.value = newContent
        val words = newContent.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        _chapterWordCount.value = words

        if (initialSessionWords < 0) {
            initialSessionWords = words
        }
        _wordsWrittenInSession.value = (words - initialSessionWords).coerceAtLeast(0)

        val chapterId = _activeChapterId.value
        val novelId = _selectedNovelId.value ?: _activeNovelId.value
        if (chapterId.isNotBlank() && novelId.isNotBlank()) {
            viewModelScope.launch {
                repository.saveChapterContent(chapterId, newContent, words, novelId)
            }
        }
    }

    fun applyMarkdownFormat(formatType: String, selectionStart: Int = -1, selectionEnd: Int = -1) {
        val currentText = _chapterContent.value
        val start = if (selectionStart >= 0) selectionStart.coerceIn(0, currentText.length) else currentText.length
        val end = if (selectionEnd >= 0) selectionEnd.coerceIn(0, currentText.length) else currentText.length

        val selectedText = if (start < end) currentText.substring(start, end) else ""

        val (prefix, suffix) = when (formatType) {
            "bold" -> Pair("**", "**")
            "italic" -> Pair("*", "*")
            "quote" -> Pair("\n> ", "\n")
            "header" -> Pair("\n# ", "\n")
            "list" -> Pair("\n- ", "\n")
            else -> Pair("", "")
        }

        val newText = if (selectedText.isNotBlank()) {
            currentText.substring(0, start) + prefix + selectedText + suffix + currentText.substring(end)
        } else {
            val defaultInsert = when (formatType) {
                "bold" -> "texto en negrita"
                "italic" -> "texto en cursiva"
                "quote" -> "Cita literaria"
                "header" -> "Encabezado principal"
                "list" -> "Elemento de lista"
                else -> ""
            }
            currentText.substring(0, start) + prefix + defaultInsert + suffix + currentText.substring(start)
        }

        updateChapterContent(newText)
        val formatName = when (formatType) {
            "bold" -> "Negrita"
            "italic" -> "Cursiva"
            "quote" -> "Cita"
            "header" -> "Encabezado"
            "list" -> "Lista"
            else -> "Formato"
        }
        showToast("Formato '$formatName' aplicado")
    }

    fun saveCurrentVersion(note: String = "Versión manual") {
        val chapterId = _activeChapterId.value
        val novelId = _selectedNovelId.value ?: _activeNovelId.value
        val content = _chapterContent.value
        val words = _chapterWordCount.value

        if (chapterId.isBlank() || content.isBlank()) {
            showToast("No hay contenido suficiente para guardar versión")
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val timeFormatted = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date(now))
            val version = ChapterVersion(
                chapterId = chapterId,
                novelId = novelId,
                timestamp = now,
                timeFormatted = timeFormatted,
                wordCount = words,
                content = content,
                note = note
            )
            repository.saveChapterVersion(version)
            showToast("📸 Versión guardada ($words palabras)")
        }
    }

    fun restoreVersion(version: ChapterVersion) {
        updateChapterContent(version.content)
        showToast("Borrador restaurado a versión de las ${version.timeFormatted}")
    }

    // Pomodoro Timer Controls
    fun toggleTimer() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _timerSeconds.value > 0) {
                delay(1000L)
                _timerSeconds.value -= 1
            }
            if (_timerSeconds.value <= 0) {
                _isTimerRunning.value = false
                showToast("⏰ ¡Sesión Pomodoro completada! Excelente avance.")
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer(minutes: Int = 25) {
        pauseTimer()
        _timerSeconds.value = minutes * 60
        initialSessionWords = _chapterWordCount.value
        _wordsWrittenInSession.value = 0
    }

    fun toggleChapterFinished() {
        val chapterId = _activeChapterId.value
        if (chapterId.isBlank()) return
        val newState = !_isChapterFinished.value
        _isChapterFinished.value = newState
        viewModelScope.launch {
            repository.toggleChapterStatus(chapterId, newState)
            showToast(if (newState) "✓ Capítulo marcado como Terminado" else "Borrador Vivo reanudado")
        }
    }

    fun setPaperTexture(texture: String) {
        _paperTexture.value = texture
    }

    fun toggleFont() {
        _selectedFont.value = if (_selectedFont.value == "Newsreader") "Jakarta Sans" else "Newsreader"
    }

    fun toggleZenMode() {
        _isZenMode.value = !_isZenMode.value
        if (_isZenMode.value) {
            showToast("Modo Inmersivo Zen Activado")
        }
    }

    fun setPersonajesTab(tab: String) {
        _personajesTab.value = tab
    }

    fun setFactionFilter(faction: String?) {
        _characterFactionFilter.value = faction
    }

    fun toggleSynopsis() {
        _isSynopsisExpanded.value = !_isSynopsisExpanded.value
    }

    fun addMarginNote(category: String, colorTag: String, content: String, scopeTag: String) {
        val novelId = _selectedNovelId.value ?: _activeNovelId.value
        if (novelId.isBlank()) return
        viewModelScope.launch {
            val note = MarginNote(
                novelId = novelId,
                chapterId = _activeChapterId.value,
                category = category,
                colorTag = colorTag,
                content = content,
                scopeTag = scopeTag,
                timeLabel = "Ahora mismo"
            )
            repository.addMarginNote(note)
            showToast("Nota al margen fijada")
        }
    }

    fun deleteMarginNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteMarginNote(noteId)
            showToast("Nota eliminada del margen")
        }
    }

    fun openCreateModal(type: String = "novel") {
        _createType.value = type
        _showCreateModal.value = true
    }

    fun closeCreateModal() {
        _showCreateModal.value = false
    }

    fun createNovel(title: String, genre: String, targetWords: Int, synopsis: String) {
        viewModelScope.launch {
            val id = title.lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "-")
            val novel = Novel(
                id = id,
                title = title,
                subtitle = "Manuscrito original",
                genre = genre,
                tropes = "✨ Nuevo manuscrito",
                wordCount = 0,
                targetWords = targetWords,
                completedChapters = 0,
                totalChapters = 10,
                progressPercent = 0,
                coverDrawableName = "cover_silencio",
                status = "Borrador inicial",
                lastEdited = "Ahora",
                isFeatured = false,
                synopsis = synopsis
            )
            repository.insertNovel(novel)
            
            // Create initial chapter for new novel
            val initialChapter = Chapter(
                id = "${id}_cap_1",
                novelId = id,
                number = 1,
                title = "Capítulo 1",
                actName = "Capítulo 1",
                content = "",
                wordCount = 0,
                isFinished = false,
                lastSynced = "Ahora"
            )
            repository.insertChapter(initialChapter)
            selectNovel(id)
            _showCreateModal.value = false
            showToast("Novela '$title' creada en biblioteca")
        }
    }

    fun createRelationship(fromChar: String, toChar: String, relType: String, desc: String) {
        val novelId = _selectedNovelId.value ?: _activeNovelId.value
        if (novelId.isBlank()) return
        viewModelScope.launch {
            val rel = Relationship(
                novelId = novelId,
                fromCharacterName = fromChar,
                toCharacterName = toChar,
                type = relType,
                description = desc
            )
            repository.insertRelationship(rel)
            showToast("Vínculo entre $fromChar y $toChar registrado")
        }
    }

    fun updateCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            repository.insertCharacter(character)
            showToast("Personaje '${character.name}' actualizado")
        }
    }

    fun createCharacter(name: String, role: String, species: String, bio: String, ability: String) {
        val novelId = _selectedNovelId.value ?: _activeNovelId.value
        if (novelId.isBlank()) return
        viewModelScope.launch {
            val id = name.lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "-")
            val character = CharacterEntity(
                id = id,
                novelId = novelId,
                name = name,
                roleTag = role,
                epithet = role,
                bio = bio,
                age = "Indefinida",
                species = species,
                plotRoleOrMotivation = "En desarrollo",
                traits = "Nuevo personaje,Intriga",
                specialAbility = ability,
                crucialLink = "Pendiente de vincular",
                faction = "Gremio",
                avatarDrawableName = "avatar_kaelen"
            )
            repository.insertCharacter(character)
            _showCreateModal.value = false
            showToast("Personaje '$name' añadido al elenco")
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            val novels = allNovels.value
            val characters = allCharacters.value
            val json = repository.generateBackupJson(novels, characters)
            _backupJsonString.value = json
            _showBackupDialog.value = true
            showToast("Bóveda: Copia generada exitosamente")
        }
    }

    fun closeBackupDialog() {
        _showBackupDialog.value = false
    }


    fun exportManuscript(format: String, context: Context) {
        val novel = currentNovel.value ?: return
        val chapters = activeChapters.value
        if (chapters.isEmpty()) {
            showToast("No hay capítulos que exportar")
            return
        }

        val fileName = "${novel.title.lowercase().replace("[^a-z0-9]".toRegex(), "_")}_manuscrito"
        val contentBuilder = StringBuilder()
        contentBuilder.append("# ${novel.title}\n")
        contentBuilder.append("## ${novel.subtitle}\n\n")
        contentBuilder.append("Género: ${novel.genre}\n")
        contentBuilder.append("Sinopsis: ${novel.synopsis}\n\n")
        contentBuilder.append("=========================================\n\n")

        for (ch in chapters) {
            contentBuilder.append("### Capítulo ${ch.number}: ${ch.title}\n\n")
            contentBuilder.append("${ch.content}\n\n")
            contentBuilder.append("-----------------------------------------\n\n")
        }

        try {
            val extension = when (format.lowercase()) {
                "pdf" -> "pdf"
                "epub" -> "epub"
                else -> "md"
            }
            val mimeType = when (format.lowercase()) {
                "pdf" -> "application/pdf"
                "epub" -> "application/epub+zip"
                else -> "text/markdown"
            }

            val exportDir = java.io.File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = java.io.File(exportDir, "$fileName.$extension")
            file.writeText(contentBuilder.toString())

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(android.content.Intent.EXTRA_SUBJECT, "${novel.title} - Manuscrito (${format.uppercase()})")
                putExtra(android.content.Intent.EXTRA_TEXT, "Manuscrito de '${novel.title}' exportado en formato ${format.uppercase()} desde Tinta & Trama.")
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = android.content.Intent.createChooser(shareIntent, "Exportar manuscrito (${format.uppercase()})")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            showToast("📄 Exportando manuscrito en ${format.uppercase()}")
        } catch (e: Exception) {
            showToast("Error al exportar: ${e.message}")
        }
    }


    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun dismissToast() {
        _toastMessage.value = null
    }
}
