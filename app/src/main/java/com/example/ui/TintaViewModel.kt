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

    private val _chapterStatus = MutableStateFlow("Borrador") // "Borrador", "En Revisión", "Terminado"
    val chapterStatus: StateFlow<String> = _chapterStatus.asStateFlow()

    private val _isSavingContent = MutableStateFlow(false)
    val isSavingContent: StateFlow<Boolean> = _isSavingContent.asStateFlow()

    private val _paperTexture = MutableStateFlow("plain")
    val paperTexture: StateFlow<String> = _paperTexture.asStateFlow()

    private val _selectedFont = MutableStateFlow("Newsreader")
    val selectedFont: StateFlow<String> = _selectedFont.asStateFlow()

    private val _isZenMode = MutableStateFlow(false)
    val isZenMode: StateFlow<Boolean> = _isZenMode.asStateFlow()

    // Pomodoro / Writing Session Timer State
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerInitialMinutes = MutableStateFlow(25)
    val timerInitialMinutes: StateFlow<Int> = _timerInitialMinutes.asStateFlow()

    private val _timerSeconds = MutableStateFlow(25 * 60) // Default 25 minutes
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _wordsWrittenInSession = MutableStateFlow(0)
    val wordsWrittenInSession: StateFlow<Int> = _wordsWrittenInSession.asStateFlow()

    private var initialSessionWords = -1
    private var timerJob: Job? = null
    private var saveContentJob: Job? = null
    private var lastSnapshotTime: Long = 0L

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
        val previousChapterId = _activeChapterId.value
        val previousContent = _chapterContent.value
        val novelId = _selectedNovelId.value ?: _activeNovelId.value

        // Cancel pending debounce save for previous chapter and execute immediate snapshot if needed
        saveContentJob?.cancel()
        if (previousChapterId.isNotBlank() && previousContent.isNotBlank() && novelId.isNotBlank()) {
            viewModelScope.launch {
                val words = previousContent.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                repository.saveChapterContent(previousChapterId, previousContent, words, novelId)
            }
        }

        _activeChapterId.value = chapterId
        viewModelScope.launch {
            val chapter = repository.getChapterById(chapterId).firstOrNull()
            chapter?.let {
                _chapterContent.value = it.content
                _chapterWordCount.value = it.wordCount
                _isChapterFinished.value = it.isFinished
                _chapterStatus.value = if (it.isFinished) "Terminado" else "Borrador"
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
            } else if (_activeChapterId.value.isNotBlank()) {
                val current = repository.getChapterById(_activeChapterId.value).firstOrNull()
                if (current != null && current.novelId == novelId) current else repository.getChaptersForNovel(novelId).firstOrNull()?.firstOrNull()
            } else {
                repository.getChaptersForNovel(novelId).firstOrNull()?.firstOrNull()
            }
            targetChapter?.let {
                selectChapter(it.id)
            }
        }
    }

    /**
     * Actualiza el borrador del capítulo en tiempo real con guardado diferido (debounced auto-save)
     * y genera respaldos/snapshots automáticos de versión a intervalos regulares.
     */
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
            saveContentJob?.cancel()
            saveContentJob = viewModelScope.launch {
                _isSavingContent.value = true
                delay(500L) // Debounce de 500ms para evitar escrituras excesivas en base de datos
                repository.saveChapterContent(chapterId, newContent, words, novelId)
                delay(200L)
                _isSavingContent.value = false

                // Snapshot automático de versión cada 5 minutos de escritura continua
                val now = System.currentTimeMillis()
                if (now - lastSnapshotTime > 5 * 60 * 1000L && newContent.isNotBlank()) {
                    lastSnapshotTime = now
                    val timeFormatted = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date(now))
                    val autoVersion = ChapterVersion(
                        chapterId = chapterId,
                        novelId = novelId,
                        timestamp = now,
                        timeFormatted = timeFormatted,
                        wordCount = words,
                        content = newContent,
                        note = "Respaldo automático"
                    )
                    repository.saveChapterVersion(autoVersion)
                }
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

    fun setTimerDuration(minutes: Int) {
        val validMinutes = minutes.coerceIn(1, 180)
        _timerInitialMinutes.value = validMinutes
        resetTimer(validMinutes)
    }

    fun resetTimer(minutes: Int = _timerInitialMinutes.value) {
        pauseTimer()
        _timerSeconds.value = minutes * 60
        initialSessionWords = _chapterWordCount.value
        _wordsWrittenInSession.value = 0
    }

    fun setChapterStatus(status: String) {
        _chapterStatus.value = status
        val isDone = (status == "Terminado")
        _isChapterFinished.value = isDone
        val chapterId = _activeChapterId.value
        if (chapterId.isNotBlank()) {
            viewModelScope.launch {
                repository.toggleChapterStatus(chapterId, isDone)
                showToast("Estado actualizado a $status")
            }
        }
    }

    fun toggleChapterFinished() {
        val chapterId = _activeChapterId.value
        if (chapterId.isBlank()) return
        val newState = !_isChapterFinished.value
        _isChapterFinished.value = newState
        _chapterStatus.value = if (newState) "Terminado" else "Borrador"
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
     * Escala y recorta con formato estándar para libros (proporción 2:3) y
     * guarda la ruta del archivo local en la base de datos para garantizar persistencia con Scoped Storage.
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
                        // Proporción editorial estándar de libros: 2:3 (600 x 900 px)
                        val targetWidth = 600
                        val targetHeight = 900

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
                            showToast("Portada añadida en formato estándar (2:3)")
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

    /**
     * Alterna el estado de leído/pendiente de una historia.
     * Las historias leídas se desplazan a segundo plano / debajo de las no leídas.
     */
    fun toggleNovelRead(novelId: String) {
        viewModelScope.launch {
            val novel = repository.getNovelById(novelId).firstOrNull() ?: return@launch
            val newReadState = !novel.isRead
            val updatedNovel = novel.copy(isRead = newReadState)
            repository.insertNovel(updatedNovel)
            if (newReadState) {
                showToast("\"${novel.title}\" marcada como leída (en segundo plano)")
            } else {
                showToast("\"${novel.title}\" marcada como pendiente de lectura")
            }
        }
    }

    /**
     * Actualiza el nombre, categoría (género), subtítulo y sinopsis de una historia.
     */
    fun updateNovelDetails(novelId: String, title: String, genre: String, subtitle: String, synopsis: String) {
        viewModelScope.launch {
            val novel = repository.getNovelById(novelId).firstOrNull() ?: return@launch
            val updatedNovel = novel.copy(
                title = title.trim().ifBlank { novel.title },
                genre = genre.trim().ifBlank { novel.genre },
                subtitle = subtitle.trim().ifBlank { novel.subtitle },
                synopsis = synopsis.trim().ifBlank { novel.synopsis }
            )
            repository.insertNovel(updatedNovel)
            showToast("Información de \"${updatedNovel.title}\" actualizada")
        }
    }

    /**
     * Elimina permanentemente una historia y todos sus capítulos, notas y personajes asociados.
     */
    fun deleteNovel(novelId: String) {
        viewModelScope.launch {
            val novel = repository.getNovelById(novelId).firstOrNull()
            val novelTitle = novel?.title ?: "Historia"
            repository.deleteNovel(novelId)

            if (_selectedNovelId.value == novelId) {
                val remaining = repository.allNovels.firstOrNull()?.filter { it.id != novelId } ?: emptyList()
                if (remaining.isNotEmpty()) {
                    val next = remaining.first()
                    _selectedNovelId.value = next.id
                    _activeNovelId.value = next.id
                    val chapters = repository.getChaptersForNovel(next.id).firstOrNull() ?: emptyList()
                    if (chapters.isNotEmpty()) {
                        selectChapter(chapters.first().id)
                    }
                } else {
                    _selectedNovelId.value = null
                    _activeNovelId.value = ""
                    _activeChapterId.value = ""
                    _chapterContent.value = ""
                    _chapterWordCount.value = 0
                }
            }
            showToast("\"$novelTitle\" eliminada de la biblioteca")
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
     * Importa un documento local (.md, .docx, .epub) usando Storage Access Framework (SAF).
     * Analiza el archivo para extraer el título, dar formato enriquecido en Markdown y dividir capítulos según los encabezados.
     */
    fun importDocument(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val fileName = getFileNameFromUri(uri, contentResolver) ?: "documento_importado"
                val isEpub = fileName.endsWith(".epub", ignoreCase = true)
                val isDocx = fileName.endsWith(".docx", ignoreCase = true)

                val rawText = when {
                    isEpub -> extractContentFromEpub(uri, contentResolver)
                    isDocx -> extractRichTextFromDocx(uri, contentResolver)
                    else -> contentResolver.openInputStream(uri)?.use { stream ->
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

    /**
     * Extrae texto con formato enriquecido (Markdown) desde un archivo Word (.docx).
     * Mantiene negritas (**), cursivas (*), y niveles de encabezado (# / ##).
     */
    private fun extractRichTextFromDocx(uri: Uri, resolver: android.content.ContentResolver): String {
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
                            val p = paragraphs.item(i) as org.w3c.dom.Element
                            val pStyleNodes = p.getElementsByTagName("w:pStyle")
                            var isHeading1 = false
                            var isHeading2 = false

                            if (pStyleNodes.length > 0) {
                                val styleVal = (pStyleNodes.item(0) as org.w3c.dom.Element).getAttribute("w:val")
                                if (styleVal.contains("Heading1", ignoreCase = true) || styleVal == "1" || styleVal.contains("Título1", ignoreCase = true)) {
                                    isHeading1 = true
                                } else if (styleVal.contains("Heading2", ignoreCase = true) || styleVal == "2" || styleVal.contains("Título2", ignoreCase = true)) {
                                    isHeading2 = true
                                }
                            }

                            val pTextBuilder = StringBuilder()
                            if (isHeading1) pTextBuilder.append("# ")
                            else if (isHeading2) pTextBuilder.append("## ")

                            val runs = p.getElementsByTagName("w:r")
                            for (rIndex in 0 until runs.length) {
                                val r = runs.item(rIndex) as org.w3c.dom.Element
                                val rPr = r.getElementsByTagName("w:rPr")

                                var isBold = false
                                var isItalic = false

                                if (rPr.length > 0) {
                                    val rPrElem = rPr.item(0) as org.w3c.dom.Element
                                    val bNodes = rPrElem.getElementsByTagName("w:b")
                                    if (bNodes.length > 0) {
                                        val valAttr = (bNodes.item(0) as org.w3c.dom.Element).getAttribute("w:val")
                                        isBold = valAttr != "false" && valAttr != "0"
                                    }
                                    val iNodes = rPrElem.getElementsByTagName("w:i")
                                    if (iNodes.length > 0) {
                                        val valAttr = (iNodes.item(0) as org.w3c.dom.Element).getAttribute("w:val")
                                        isItalic = valAttr != "false" && valAttr != "0"
                                    }
                                }

                                val tNodes = r.getElementsByTagName("w:t")
                                val runTextBuilder = StringBuilder()
                                for (tIndex in 0 until tNodes.length) {
                                    runTextBuilder.append(tNodes.item(tIndex).textContent)
                                }
                                val rawRunText = runTextBuilder.toString()
                                if (rawRunText.isNotBlank()) {
                                    val formattedRun = when {
                                        isBold && isItalic -> "***$rawRunText***"
                                        isBold -> "**$rawRunText**"
                                        isItalic -> "*$rawRunText*"
                                        else -> rawRunText
                                    }
                                    pTextBuilder.append(formattedRun)
                                }
                            }

                            val fullPText = pTextBuilder.toString().trim()
                            if (fullPText.isNotBlank()) {
                                textBuilder.append(fullPText).append("\n\n")
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
     * Extrae el contenido completo y estructurado en Markdown desde un libro en formato EPUB (.epub).
     */
    private fun extractContentFromEpub(uri: Uri, resolver: android.content.ContentResolver): String {
        val textBuilder = StringBuilder()
        try {
            val htmlEntries = mutableMapOf<String, String>()
            resolver.openInputStream(uri)?.use { inputStream ->
                java.util.zip.ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        val name = entry.name.lowercase(Locale.ROOT)
                        if (name.endsWith(".xhtml") || name.endsWith(".html") || name.endsWith(".htm")) {
                            val content = zipStream.bufferedReader().readText()
                            htmlEntries[entry.name] = content
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                }
            }

            // Ordenar las entradas para mantener secuencia lógica de capítulos
            val sortedKeys = htmlEntries.keys.sortedWith(Comparator { k1, k2 ->
                val num1 = k1.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                val num2 = k2.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                if (num1 != num2) num1.compareTo(num2) else k1.compareTo(k2)
            })

            for (key in sortedKeys) {
                val rawHtml = htmlEntries[key] ?: continue
                val markdown = convertHtmlToMarkdown(rawHtml)
                if (markdown.isNotBlank()) {
                    textBuilder.append(markdown).append("\n\n")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return textBuilder.toString()
    }

    /**
     * Convierte fragmentos de HTML / XHTML de EPUB a Markdown estructurado para el editor.
     */
    private fun convertHtmlToMarkdown(html: String): String {
        var clean = html.replace("(?i)<script[\\s\\S]*?>[\\s\\S]*?</script>".toRegex(), "")
            .replace("(?i)<style[\\s\\S]*?>[\\s\\S]*?</style>".toRegex(), "")

        // Títulos
        clean = clean.replace("(?i)<h1[^>]*>(.*?)</h1>".toRegex(), "\n# $1\n")
        clean = clean.replace("(?i)<h2[^>]*>(.*?)</h2>".toRegex(), "\n## $1\n")
        clean = clean.replace("(?i)<h3[^>]*>(.*?)</h3>".toRegex(), "\n### $1\n")

        // Formato de texto
        clean = clean.replace("(?i)<(b|strong)[^>]*>(.*?)</\\1>".toRegex(), "**$2**")
        clean = clean.replace("(?i)<(i|em)[^>]*>(.*?)</\\1>".toRegex(), "*$2*")

        // Párrafos y saltos de línea
        clean = clean.replace("(?i)<p[^>]*>(.*?)</p>".toRegex(), "$1\n\n")
        clean = clean.replace("(?i)<br\\s*/?>".toRegex(), "\n")

        // Eliminar resto de etiquetas HTML
        clean = clean.replace("<[^>]+>".toRegex(), "")

        // Decodificar entidades HTML básicas
        clean = clean.replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")

        // Limpiar líneas vacías excesivas
        return clean.lines().map { it.trim() }.joinToString("\n").replace("\n{3,}".toRegex(), "\n\n").trim()
    }

    /**
     * Exporta el manuscrito en PDF maquetado, EPUB (.epub), Word (.docx) o Markdown (.md).
     */
    fun exportManuscript(format: String, context: Context) {
        val novel = currentNovel.value ?: return
        val chapters = activeChapters.value
        if (chapters.isEmpty()) {
            showToast("No hay capítulos que exportar")
            return
        }

        val fileName = "${novel.title.lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "_")}_manuscrito"

        try {
            val exportDir = java.io.File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val extension = format.lowercase(Locale.ROOT)
            val exportFile = java.io.File(exportDir, "$fileName.$extension")

            when (extension) {
                "pdf" -> generatePdfExport(novel, chapters, exportFile)
                "epub" -> generateEpubExport(novel, chapters, exportFile)
                "docx" -> generateDocxExport(novel, chapters, exportFile)
                else -> generateMarkdownExport(novel, chapters, exportFile)
            }

            val mimeType = when (extension) {
                "pdf" -> "application/pdf"
                "epub" -> "application/epub+zip"
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
                putExtra(android.content.Intent.EXTRA_SUBJECT, "${novel.title} - Manuscrito (${format.uppercase(Locale.ROOT)})")
                putExtra(android.content.Intent.EXTRA_TEXT, "Manuscrito de '${novel.title}' exportado en formato ${format.uppercase(Locale.ROOT)} desde Tinta & Trama.")
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = android.content.Intent.createChooser(shareIntent, "Exportar manuscrito (${format.uppercase(Locale.ROOT)})")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            showToast("📄 Exportando manuscrito en ${format.uppercase(Locale.ROOT)}")
        } catch (e: Exception) {
            showToast("Error al exportar: ${e.message}")
        }
    }

    /**
     * Guarda directamente el archivo exportado en una ubicación específica elegida por el usuario con SAF.
     */
    fun saveExportedFileDirectly(format: String, targetUri: Uri, context: Context) {
        val novel = currentNovel.value ?: return
        val chapters = activeChapters.value
        if (chapters.isEmpty()) {
            showToast("No hay capítulos que exportar")
            return
        }

        try {
            val extension = format.lowercase(Locale.ROOT)
            val tempFile = java.io.File(context.cacheDir, "temp_export_${System.currentTimeMillis()}.$extension")

            when (extension) {
                "pdf" -> generatePdfExport(novel, chapters, tempFile)
                "epub" -> generateEpubExport(novel, chapters, tempFile)
                "docx" -> generateDocxExport(novel, chapters, tempFile)
                else -> generateMarkdownExport(novel, chapters, tempFile)
            }

            context.contentResolver.openOutputStream(targetUri)?.use { output ->
                tempFile.inputStream().use { input -> input.copyTo(output) }
            }
            tempFile.delete()
            showToast("💾 Manuscrito guardado exitosamente en el almacenamiento elegido")
        } catch (e: Exception) {
            showToast("Error al guardar archivo: ${e.message}")
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

    /**
     * Genera un libro electrónico estándar en formato EPUB (.epub).
     */
    private fun generateEpubExport(novel: Novel, chapters: List<Chapter>, outputFile: java.io.File) {
        java.io.FileOutputStream(outputFile).use { fos ->
            java.util.zip.ZipOutputStream(fos).use { zos ->
                // 1. mimetype (Sin compresión STORED como especifica el estándar EPUB)
                val mimetypeBytes = "application/epub+zip".toByteArray(Charsets.US_ASCII)
                val mimeEntry = java.util.zip.ZipEntry("mimetype").apply {
                    method = java.util.zip.ZipEntry.STORED
                    size = mimetypeBytes.size.toLong()
                    compressedSize = mimetypeBytes.size.toLong()
                    crc = java.util.zip.CRC32().apply { update(mimetypeBytes) }.value
                }
                zos.putNextEntry(mimeEntry)
                zos.write(mimetypeBytes)
                zos.closeEntry()

                // 2. META-INF/container.xml
                zos.putNextEntry(java.util.zip.ZipEntry("META-INF/container.xml"))
                val containerXml = "<?xml version=\"1.0\"?>\n<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n  <rootfiles>\n    <rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n  </rootfiles>\n</container>"
                zos.write(containerXml.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 3. OEBPS/style.css
                zos.putNextEntry(java.util.zip.ZipEntry("OEBPS/style.css"))
                val styleCss = "body { font-family: serif; margin: 5%; line-height: 1.6; } h1, h2 { text-align: center; font-weight: bold; } p { text-indent: 1.5em; margin-bottom: 0.5em; }"
                zos.write(styleCss.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 4. OEBPS/title.xhtml
                zos.putNextEntry(java.util.zip.ZipEntry("OEBPS/title.xhtml"))
                val titleXhtml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head><title>${escapeXml(novel.title)}</title><link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/></head>\n<body>\n  <div style=\"text-align: center; margin-top: 30%;\">\n    <h1>${escapeXml(novel.title)}</h1>\n    <h2>${escapeXml(novel.subtitle)}</h2>\n    <p><em>${escapeXml(novel.genre)}</em></p>\n    <p style=\"margin-top: 20%; font-size: 0.9em;\">Tinta &amp; Trama Atelier Literario</p>\n  </div>\n</body>\n</html>"
                zos.write(titleXhtml.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 5. Capítulos XHTML
                chapters.forEachIndexed { idx, ch ->
                    zos.putNextEntry(java.util.zip.ZipEntry("OEBPS/chapter_${idx + 1}.xhtml"))
                    val chHtml = StringBuilder()
                    chHtml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head><title>${escapeXml(ch.title)}</title><link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/></head>\n<body>\n")
                    chHtml.append("<h2>Capítulo ").append(ch.number).append(": ").append(escapeXml(ch.title)).append("</h2>\n")

                    val paragraphs = ch.content.split("\n")
                    for (p in paragraphs) {
                        if (p.isNotBlank()) {
                            var formattedP = escapeXml(p)
                            formattedP = formattedP.replace("\\*\\*([^*]+)\\*\\*".toRegex(), "<strong>$1</strong>")
                            formattedP = formattedP.replace("\\*([^*]+)\\*".toRegex(), "<em>$1</em>")
                            chHtml.append("<p>").append(formattedP).append("</p>\n")
                        }
                    }
                    chHtml.append("</body>\n</html>")
                    zos.write(chHtml.toString().toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }

                // 6. OEBPS/toc.ncx
                zos.putNextEntry(java.util.zip.ZipEntry("OEBPS/toc.ncx"))
                val ncx = StringBuilder()
                ncx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n<head><meta name=\"dtb:uid\" content=\"urn:uuid:${novel.id}\"/></head>\n<docTitle><text>${escapeXml(novel.title)}</text></docTitle>\n<navMap>\n")
                ncx.append("<navPoint id=\"title\" playOrder=\"1\"><navLabel><text>Portada</text></navLabel><content src=\"title.xhtml\"/></navPoint>\n")
                chapters.forEachIndexed { idx, ch ->
                    ncx.append("<navPoint id=\"nav_${idx + 1}\" playOrder=\"${idx + 2}\"><navLabel><text>Capítulo ${ch.number}: ${escapeXml(ch.title)}</text></navLabel><content src=\"chapter_${idx + 1}.xhtml\"/></navPoint>\n")
                }
                ncx.append("</navMap>\n</ncx>")
                zos.write(ncx.toString().toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 7. OEBPS/content.opf
                zos.putNextEntry(java.util.zip.ZipEntry("OEBPS/content.opf"))
                val opf = StringBuilder()
                opf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookId\" version=\"2.0\">\n<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n")
                opf.append("<dc:title>").append(escapeXml(novel.title)).append("</dc:title>\n")
                opf.append("<dc:creator>Tinta &amp; Trama</dc:creator>\n")
                opf.append("<dc:language>es</dc:language>\n")
                opf.append("<dc:subject>").append(escapeXml(novel.genre)).append("</dc:subject>\n")
                opf.append("</metadata>\n<manifest>\n")
                opf.append("<item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n")
                opf.append("<item id=\"style\" href=\"style.css\" media-type=\"text/css\"/>\n")
                opf.append("<item id=\"title\" href=\"title.xhtml\" media-type=\"application/xhtml+xml\"/>\n")
                chapters.forEachIndexed { idx, _ ->
                    opf.append("<item id=\"chapter_${idx + 1}\" href=\"chapter_${idx + 1}.xhtml\" media-type=\"application/xhtml+xml\"/>\n")
                }
                opf.append("</manifest>\n<spine toc=\"ncx\">\n")
                opf.append("<itemref idref=\"title\"/>\n")
                chapters.forEachIndexed { idx, _ ->
                    opf.append("<itemref idref=\"chapter_${idx + 1}\"/>\n")
                }
                opf.append("</spine>\n</package>")
                zos.write(opf.toString().toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }
    }

    /**
     * Genera un PDF maquetado con portada personalizada y numeración de páginas ("Página X de Y").
     */
    private fun generatePdfExport(novel: Novel, chapters: List<Chapter>, outputFile: java.io.File) {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageWidth = 595 // Estándar A4 en puntos
        val pageHeight = 842

        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 28f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val subtitlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 16f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 10f
        }
        val chapterHeaderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }
        val bodyPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
        }
        val footerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 10f
            textAlign = android.graphics.Paint.Align.CENTER
        }

        var pageNumber = 1

        // --- PÁGINA 1: PORTADA MAQUETADA ---
        var pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Intentar dibujar imagen de portada si está disponible
        val customCoverFile = novel.customCoverPath?.let { java.io.File(it) }
        var drawImageSuccess = false
        if (customCoverFile != null && customCoverFile.exists()) {
            try {
                val coverBitmap = android.graphics.BitmapFactory.decodeFile(customCoverFile.absolutePath)
                if (coverBitmap != null) {
                    val coverWidth = 300
                    val coverHeight = 450
                    val coverLeft = (pageWidth - coverWidth) / 2f
                    val coverTop = 120f
                    val scaledCover = android.graphics.Bitmap.createScaledBitmap(coverBitmap, coverWidth, coverHeight, true)
                    canvas.drawBitmap(scaledCover, coverLeft, coverTop, null)
                    drawImageSuccess = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val titleY = if (drawImageSuccess) 610f else 300f
        canvas.drawText(novel.title, pageWidth / 2f, titleY, titlePaint)
        canvas.drawText(novel.subtitle.ifBlank { novel.genre }, pageWidth / 2f, titleY + 35f, subtitlePaint)
        canvas.drawText("TINTA & TRAMA — ATELIER LITERARIO", pageWidth / 2f, pageHeight - 80f, footerPaint)
        pdfDocument.finishPage(page)

        // --- PÁGINAS DE CONTENIDO CON CABECERA Y NUMERACIÓN "PÁGINA X" ---
        pageNumber++
        pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas

        var y = 80f
        val margin = 50f
        val lineSpacing = 18f

        fun drawHeaderAndFooter(c: android.graphics.Canvas, pNum: Int, currentChTitle: String) {
            c.drawText(novel.title.uppercase(Locale.ROOT) + " — " + currentChTitle, margin, 40f, headerPaint)
            c.drawLine(margin, 46f, pageWidth - margin, 46f, headerPaint)
            c.drawText("Página $pNum", pageWidth / 2f, pageHeight - 40f, footerPaint)
        }

        for (ch in chapters) {
            drawHeaderAndFooter(canvas, pageNumber, "Capítulo ${ch.number}")

            if (y > pageHeight - 120f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 80f
                drawHeaderAndFooter(canvas, pageNumber, "Capítulo ${ch.number}")
            }

            canvas.drawText("Capítulo ${ch.number}: ${ch.title}", margin, y, chapterHeaderPaint)
            y += 32f

            val lines = ch.content.lines()
            for (line in lines) {
                if (line.isBlank()) {
                    y += 12f
                    continue
                }
                val words = line.split(" ")
                var currentLine = ""
                for (word in words) {
                    if (bodyPaint.measureText("$currentLine $word") < pageWidth - (margin * 2)) {
                        currentLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    } else {
                        if (y > pageHeight - 70f) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            y = 80f
                            drawHeaderAndFooter(canvas, pageNumber, "Capítulo ${ch.number}")
                        }
                        canvas.drawText(currentLine, margin, y, bodyPaint)
                        y += lineSpacing
                        currentLine = word
                    }
                }
                if (currentLine.isNotEmpty()) {
                    if (y > pageHeight - 70f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 80f
                        drawHeaderAndFooter(canvas, pageNumber, "Capítulo ${ch.number}")
                    }
                    canvas.drawText(currentLine, margin, y, bodyPaint)
                    y += lineSpacing
                }
            }
            y += 24f
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
