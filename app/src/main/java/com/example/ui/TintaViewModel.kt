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
        checkAndCreateTutorialOnFirstRun()
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

    /**
     * Detección y creación automática de la historia tutorial "Bienvenido a Tinta",
     * junto con sus capítulos de guía, fichas de personajes completas y red del mapa en corcho.
     */
    private fun checkAndCreateTutorialOnFirstRun() {
        val tutorialId = "bienvenido-a-tinta"
        val context = getApplication<Application>().applicationContext
        val prefs = context.getSharedPreferences("tinta_prefs", Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)

        viewModelScope.launch {
            val existingNovel = repository.getNovelById(tutorialId).firstOrNull()
            if (isFirstRun || existingNovel == null) {
                val tutorialNovel = Novel(
                    id = tutorialId,
                    title = "Bienvenido a Tinta",
                    subtitle = "Tutorial interactivo para escritores",
                    genre = "Guía & Onboarding",
                    tropes = "✨ Tutorial interactivo",
                    wordCount = 450,
                    targetWords = 500,
                    completedChapters = 2,
                    totalChapters = 2,
                    progressPercent = 100,
                    coverDrawableName = "cover_silencio",
                    status = "Tutorial",
                    lastEdited = "Ahora",
                    isFeatured = true,
                    synopsis = "¡Hola escritor! Esta historia interactiva te guiará paso a paso por todas las herramientas de Tinta & Trama.",
                    isTutorial = true
                )

                val cap1 = Chapter(
                    id = "${tutorialId}_cap_1",
                    novelId = tutorialId,
                    number = 1,
                    title = "El Atelier de Escritura",
                    actName = "Capítulo 1",
                    content = "# Bienvenido a Tinta & Trama\n\nEste es tu **nuevo espacio de creación literaria**. Aquí podrás redactar tus manuscritos con formato enriquecido fluido, organizar capítulos y consultar notas al margen.\n\n### ✍️ ¿Cómo usar la barra de herramientas?\n- Usa los botones **B** y *I* para dar estilo a tus palabras.\n- Pulsa **H1** para añadir títulos a tus escenas.\n- Selecciona diferentes **estilos de papel** (Pergamino, Rayado, Retícula o Limpio) en la barra inferior.\n\n*Continúa leyendo en el Capítulo 2 para descubrir cómo gestionar tus portadas y exportar tus historias.*",
                    wordCount = 100,
                    isFinished = true,
                    lastSynced = "Ahora"
                )

                val cap2 = Chapter(
                    id = "${tutorialId}_cap_2",
                    novelId = tutorialId,
                    number = 2,
                    title = "Carátulas y Exportación SAF",
                    actName = "Capítulo 2",
                    content = "# Gestión de Obras y Archivos\n\n### 🎨 Personaliza tus Portadas\nEn la pantalla **Obra**, simplemente toca la imagen de la portada para abrir la galería de tu teléfono con el Storage Access Framework (SAF), seleccionar tu imagen favorita y guardarla de forma segura.\n\n### 📄 Importación y Exportación\n- Puedes importar cualquier documento `.md` o `.docx` directamente desde tu dispositivo.\n- Al terminar tu historia, expórtala en formato **PDF**, **Word (.docx)** o **Markdown (.md)** de forma nativa.\n\n¡Disfruta escribiendo tu próximo gran manuscrito!",
                    wordCount = 110,
                    isFinished = true,
                    lastSynced = "Ahora"
                )

                repository.insertNovel(tutorialNovel)
                repository.insertChapter(cap1)
                repository.insertChapter(cap2)
                prefs.edit().putBoolean("is_first_run", false).apply()
            }

            // Verificar si el tutorial ya tiene fichas de personajes y vínculos de corcho.
            // Si están vacíos, se pueblan automáticamente para que la pestaña Personajes y Mapa en Corcho estén activos.
            val existingChars = repository.getCharactersForNovel(tutorialId).firstOrNull() ?: emptyList()
            if (existingChars.isEmpty()) {
                seedTutorialCharactersAndRelationships(tutorialId)
            }

            if (isFirstRun) {
                selectNovel(tutorialId)
            }
        }
    }

    /**
     * Inserta las fichas de personajes del tutorial y sus relaciones en el mapa de corcho.
     */
    private suspend fun seedTutorialCharactersAndRelationships(tutorialId: String) {
        val char1 = CharacterEntity(
            id = "${tutorialId}_char_1",
            novelId = tutorialId,
            name = "Aria Thorne",
            roleTag = "Protagonista",
            epithet = "Escriba del Viento",
            bio = "Joven calígrafa del Gremio de Creadores. Busca descifrar los manuscritos olvidados para revitalizar la inspiración en el reino de las palabras.",
            age = "24 años",
            species = "Humana / Escriba",
            plotRoleOrMotivation = "Proteger el Gran Manuscrito y enseñar el arte de narrar a las nuevas generaciones.",
            traits = "Observadora, tenaz, elocuente y soñadora.",
            specialAbility = "Lectura veloz y memoria mimética de textos antiguos.",
            crucialLink = "Conocedora del secreto de la Tinta Primigenia.",
            faction = "Gremio de Escribas",
            avatarDrawableName = "avatar_kaelen",
            isProtagonist = true,
            isDeuteragonist = false
        )

        val char2 = CharacterEntity(
            id = "${tutorialId}_char_2",
            novelId = tutorialId,
            name = "Mateo Corvus",
            roleTag = "Deuteragonista",
            epithet = "El Cartógrafo Rebelde",
            bio = "Explorador y dibujante de mapas narrativos. Conoce cada rincón de la trama y guía a los personajes a través de giros argumentales insospechados.",
            age = "27 años",
            species = "Humano / Navegante",
            plotRoleOrMotivation = "Trazar el mapa definitivo que una todos los mundos de ficción.",
            traits = "Audaz, ingenioso, leal y pragmático.",
            specialAbility = "Orientación intuitiva en laberintos y líneas temporales.",
            crucialLink = "Guarda el único mapa que revela el taller del autor.",
            faction = "Cartógrafos Libres",
            avatarDrawableName = "avatar_ren",
            isProtagonist = false,
            isDeuteragonist = true
        )

        val char3 = CharacterEntity(
            id = "${tutorialId}_char_3",
            novelId = tutorialId,
            name = "Lord Valerius",
            roleTag = "Antagonista",
            epithet = "El Censor del Olvido",
            bio = "Antiguo archivero que cree que los borradores imperfectos deben ser purgados con fuego. Intenta sellar la imaginación para imponer un orden rígido.",
            age = "52 años",
            species = "Humano / Inquisidor",
            plotRoleOrMotivation = "Eliminar las historias no canónicas y censurar las voces discordantes.",
            traits = "Meticuloso, severo, persuasivo y analítico.",
            specialAbility = "Retórica paralizante y acceso a los archivos censurados.",
            crucialLink = "Fue el antiguo maestro de Aria antes de su deserción.",
            faction = "Inquisición Silenciosa",
            avatarDrawableName = "cover_bruma",
            isProtagonist = false,
            isDeuteragonist = false
        )

        val char4 = CharacterEntity(
            id = "${tutorialId}_char_4",
            novelId = tutorialId,
            name = "Célere",
            roleTag = "Compañera Alada",
            epithet = "Halcón de Papel",
            bio = "Criatura mística forjada en papiro encantado. Lleva mensajes urgentes entre capítulos y avisa de incoherencias en la continuidad del relato.",
            age = "Atemporal",
            species = "Constructo de Papel & Tinta",
            plotRoleOrMotivation = "Custodiar las cartas de navegación y advertir a Aria de los peligros.",
            traits = "Veloz, vigilante y fiel hasta el último párrafo.",
            specialAbility = "Vuelo transnarrativo entre escenas y notas al margen.",
            crucialLink = "Vínculo anímico con la pluma de Aria.",
            faction = "Compañeros del Atelier",
            avatarDrawableName = "cover_helio",
            isProtagonist = false,
            isDeuteragonist = false
        )

        repository.insertCharacter(char1)
        repository.insertCharacter(char2)
        repository.insertCharacter(char3)
        repository.insertCharacter(char4)

        val rel1 = Relationship(
            novelId = tutorialId,
            fromCharacterName = "Aria Thorne",
            toCharacterName = "Mateo Corvus",
            type = "Alianza Estratégica",
            description = "Confianza mutua y compañerismo en la búsqueda del manuscrito sagrado."
        )
        val rel2 = Relationship(
            novelId = tutorialId,
            fromCharacterName = "Lord Valerius",
            toCharacterName = "Aria Thorne",
            type = "Persecución & Rivalidad",
            description = "Valerius persigue los cuadernos de Aria para borrar su historia y sellar su pluma."
        )
        val rel3 = Relationship(
            novelId = tutorialId,
            fromCharacterName = "Mateo Corvus",
            toCharacterName = "Lord Valerius",
            type = "Antigua Deuda",
            description = "Valerius encarceló a los cartógrafos rebeldes; Mateo busca liberar a sus hermanos de tinta."
        )
        val rel4 = Relationship(
            novelId = tutorialId,
            fromCharacterName = "Aria Thorne",
            toCharacterName = "Célere",
            type = "Vínculo Fiel",
            description = "Célere entrega notas secretas al margen y detecta emboscadas narrativas antes de que ocurran."
        )

        repository.insertRelationship(rel1)
        repository.insertRelationship(rel2)
        repository.insertRelationship(rel3)
        repository.insertRelationship(rel4)
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

    /**
     * Actualiza la carátula o portada personalizada de la historia seleccionada.
     * Copia la imagen a través de SAF/URI al almacenamiento privado interno de la app
     * y guarda la ruta del archivo local en la base de datos para garantizar persistencia con Scoped Storage.
     */
    fun updateNovelCover(novelId: String, uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val coversDir = java.io.File(context.filesDir, "covers")
                if (!coversDir.exists()) coversDir.mkdirs()

                val destFile = java.io.File(coversDir, "cover_${novelId}_${System.currentTimeMillis()}.jpg")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                    if (bitmap != null) {
                        // Center-crop y escalado a proporciones de carátula editorial (3:4)
                        val targetWidth = 600
                        val targetHeight = 800

                        val srcWidth = bitmap.width
                        val srcHeight = bitmap.height

                        val targetAspect = targetWidth.toFloat() / targetHeight.toFloat()
                        val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()

                        val cropWidth: Int
                        val cropHeight: Int

                        if (srcAspect > targetAspect) {
                            cropHeight = srcHeight
                            cropWidth = (srcHeight * targetAspect).toInt()
                        } else {
                            cropWidth = srcWidth
                            cropHeight = (srcWidth / targetAspect).toInt()
                        }

                        val cropX = (srcWidth - cropWidth) / 2
                        val cropY = (srcHeight - cropHeight) / 2

                        val croppedBitmap = android.graphics.Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight)
                        val finalCoverBitmap = android.graphics.Bitmap.createScaledBitmap(croppedBitmap, targetWidth, targetHeight, true)

                        java.io.FileOutputStream(destFile).use { out ->
                            finalCoverBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                        }

                        val novel = repository.getNovelById(novelId).firstOrNull()
                        if (novel != null) {
                            val updatedNovel = novel.copy(customCoverPath = destFile.absolutePath)
                            repository.insertNovel(updatedNovel)
                            showToast("Portada de la historia actualizada")
                        }
                    } else {
                        showToast("No se pudo procesar el formato de la imagen")
                    }
                }
            } catch (e: Exception) {
                showToast("Error al guardar portada: ${e.message}")
            }
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


    /**
     * Importa un documento local (.md o .docx) usando Storage Access Framework (SAF).
     * Analiza el archivo para extraer el título y dividir capítulos según los encabezados.
     */
    fun importDocument(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val fileName = getFileNameFromUri(uri, contentResolver) ?: "documento_importado"
                val rawText = if (fileName.endsWith(".docx", ignoreCase = true)) {
                    extractTextFromDocx(uri, contentResolver)
                } else {
                    contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().use { it.readText() }
                    } ?: ""
                }

                if (rawText.isBlank()) {
                    showToast("El archivo seleccionado está vacío o no se pudo leer.")
                    return@launch
                }

                // Parse document title and chapters
                val title = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

                val novelId = "imported_${System.currentTimeMillis()}"

                // Parse chapters from headings (# or Heading)
                val lines = rawText.lines()
                val chapterBlocks = mutableListOf<Pair<String, String>>() // Title to Content
                var currentCapTitle = "Capítulo 1"
                val currentCapText = StringBuilder()

                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("# ") || trimmed.startsWith("## ") || trimmed.startsWith("Capítulo", ignoreCase = true)) {
                        if (currentCapText.isNotBlank()) {
                            chapterBlocks.add(Pair(currentCapTitle, currentCapText.toString().trim()))
                            currentCapText.clear()
                        }
                        currentCapTitle = trimmed.removePrefix("# ").removePrefix("## ").trim()
                    } else {
                        currentCapText.append(line).append("\n")
                    }
                }
                if (currentCapText.isNotBlank()) {
                    chapterBlocks.add(Pair(currentCapTitle, currentCapText.toString().trim()))
                }

                val totalWordCount = chapterBlocks.sumOf { block ->
                    block.second.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                }

                val novel = Novel(
                    id = novelId,
                    title = title,
                    subtitle = "Importado desde $fileName",
                    genre = "Manuscrito Importado",
                    tropes = "📄 Documento importado",
                    wordCount = totalWordCount,
                    targetWords = (totalWordCount * 1.2).toInt().coerceAtLeast(10000),
                    completedChapters = chapterBlocks.size,
                    totalChapters = chapterBlocks.size,
                    progressPercent = 100,
                    coverDrawableName = "cover_silencio",
                    status = "Importado",
                    lastEdited = "Ahora",
                    isFeatured = false,
                    synopsis = "Manuscrito importado a Tinta & Trama desde el archivo '$fileName'."
                )
                repository.insertNovel(novel)

                chapterBlocks.forEachIndexed { index, (capTitle, capContent) ->
                    val numWords = capContent.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                    val ch = Chapter(
                        id = "${novelId}_cap_${index + 1}",
                        novelId = novelId,
                        number = index + 1,
                        title = capTitle,
                        actName = "Capítulo ${index + 1}",
                        content = capContent,
                        wordCount = numWords,
                        isFinished = true,
                        lastSynced = "Ahora"
                    )
                    repository.insertChapter(ch)
                }

                selectNovel(novelId)
                showToast("📥 Obra '$title' importada exitosamente (${chapterBlocks.size} capítulos)")
            } catch (e: Exception) {
                showToast("Error al importar documento: ${e.message}")
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri, resolver: android.content.ContentResolver): String? {
        var name: String? = null
        val cursor = resolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name ?: uri.path?.substringAfterLast('/')
    }

    private fun extractTextFromDocx(uri: Uri, resolver: android.content.ContentResolver): String {
        val textBuilder = StringBuilder()
        resolver.openInputStream(uri)?.use { inputStream ->
            java.util.zip.ZipInputStream(inputStream).use { zipStream ->
                var entry = zipStream.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                        val builder = factory.newDocumentBuilder()
                        val doc = builder.parse(zipStream)
                        val paragraphs = doc.getElementsByTagName("w:p")
                        for (i in 0 until paragraphs.length) {
                            val p = paragraphs.item(i)
                            // Extract paragraph text content
                            val pText = p.textContent
                            if (pText.isNotBlank()) {
                                textBuilder.append(pText).append("\n\n")
                            }
                        }
                        break
                    }
                    zipStream.closeEntry()
                    entry = zipStream.nextEntry
                }
            }
        }
        return textBuilder.toString()
    }

    /**
     * Exporta el manuscrito en PDF, Word (.docx) o Markdown (.md).
     */
    fun exportManuscript(format: String, context: Context) {
        val novel = currentNovel.value ?: return
        val chapters = activeChapters.value
        if (chapters.isEmpty()) {
            showToast("No hay capítulos que exportar")
            return
        }

        val fileName = "${novel.title.lowercase().replace("[^a-z0-9]".toRegex(), "_")}_manuscrito"

        try {
            val exportDir = java.io.File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val extension = format.lowercase()
            val exportFile = java.io.File(exportDir, "$fileName.$extension")

            when (extension) {
                "pdf" -> generatePdfExport(novel, chapters, exportFile)
                "docx" -> generateDocxExport(novel, chapters, exportFile)
                else -> generateMarkdownExport(novel, chapters, exportFile)
            }

            val mimeType = when (extension) {
                "pdf" -> "application/pdf"
                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                else -> "text/markdown"
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
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

    private fun generateMarkdownExport(novel: Novel, chapters: List<Chapter>, outputFile: java.io.File) {
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

        outputFile.writeText(contentBuilder.toString())
    }

    private fun generatePdfExport(novel: Novel, chapters: List<Chapter>, outputFile: java.io.File) {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageWidth = 595 // A4 standard width
        val pageHeight = 842 // A4 standard height

        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }
        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 16f
            isFakeBoldText = true
        }
        val bodyPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
        }

        var pageNumber = 1
        var pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var y = 60f
        val margin = 50f
        val lineSpacing = 18f

        // Title
        canvas.drawText(novel.title, margin, y, titlePaint)
        y += 30f
        canvas.drawText(novel.subtitle, margin, y, headerPaint)
        y += 40f

        for (ch in chapters) {
            if (y > pageHeight - 100f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }

            canvas.drawText("Capítulo ${ch.number}: ${ch.title}", margin, y, headerPaint)
            y += 26f

            val lines = ch.content.lines()
            for (line in lines) {
                if (line.isBlank()) {
                    y += 10f
                    continue
                }
                val words = line.split(" ")
                var currentLine = ""
                for (word in words) {
                    if (bodyPaint.measureText("$currentLine $word") < pageWidth - (margin * 2)) {
                        currentLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    } else {
                        if (y > pageHeight - 60f) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            y = 60f
                        }
                        canvas.drawText(currentLine, margin, y, bodyPaint)
                        y += lineSpacing
                        currentLine = word
                    }
                }
                if (currentLine.isNotEmpty()) {
                    if (y > pageHeight - 60f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 60f
                    }
                    canvas.drawText(currentLine, margin, y, bodyPaint)
                    y += lineSpacing
                }
            }
            y += 20f
        }

        pdfDocument.finishPage(page)
        java.io.FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
    }

    private fun generateDocxExport(novel: Novel, chapters: List<Chapter>, outputFile: java.io.File) {
        val documentXml = StringBuilder()
        documentXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        documentXml.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n")
        documentXml.append("<w:body>\n")

        documentXml.append("<w:p><w:r><w:rPr><w:b/><w:sz w:val=\"36\"/></w:rPr><w:t>")
        documentXml.append(escapeXml(novel.title))
        documentXml.append("</w:t></w:r></w:p>\n")

        documentXml.append("<w:p><w:r><w:rPr><w:i/><w:sz w:val=\"24\"/></w:rPr><w:t>")
        documentXml.append(escapeXml(novel.subtitle))
        documentXml.append("</w:t></w:r></w:p>\n")

        for (ch in chapters) {
            documentXml.append("<w:p><w:r><w:rPr><w:b/><w:sz w:val=\"28\"/></w:rPr><w:t>")
            documentXml.append("Capítulo ").append(ch.number).append(": ").append(escapeXml(ch.title))
            documentXml.append("</w:t></w:r></w:p>\n")

            val paragraphs = ch.content.split("\n")
            for (p in paragraphs) {
                if (p.isNotBlank()) {
                    documentXml.append("<w:p><w:r><w:t>")
                    documentXml.append(escapeXml(p))
                    documentXml.append("</w:t></w:r></w:p>\n")
                }
            }
        }

        documentXml.append("</w:body></w:document>")

        java.io.FileOutputStream(outputFile).use { fos ->
            java.util.zip.ZipOutputStream(fos).use { zos ->
                // [Content_Types].xml
                zos.putNextEntry(java.util.zip.ZipEntry("[Content_Types].xml"))
                val contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>"
                zos.write(contentTypes.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // _rels/.rels
                zos.putNextEntry(java.util.zip.ZipEntry("_rels/.rels"))
                val rels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>"
                zos.write(rels.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // word/document.xml
                zos.putNextEntry(java.util.zip.ZipEntry("word/document.xml"))
                zos.write(documentXml.toString().toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }


    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun dismissToast() {
        _toastMessage.value = null
    }
}
