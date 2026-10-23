package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChapterVersion
import com.example.data.model.MarginNote
import com.example.ui.TintaTab
import com.example.ui.TintaViewModel
import com.example.ui.components.MarkdownWysiwygVisualTransformation
import com.example.ui.theme.NewsreaderFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily

// Custom Colors matching Tinta Theme
private val TintaCreamLight = Color(0xFFFBF0D9)
private val TintaCreamDark = Color(0xFF2C2621)
private val TintaPaperLinedLight = Color(0xFFF1F5F9)
private val TintaPaperLinedDark = Color(0xFF1E293B)
private val TintaPaperGridLight = Color(0xFFE2E8F0)
private val TintaPaperGridDark = Color(0xFF334155)

private val TintaAccent = Color(0xFFE06738)
private val TintaAccentDark = Color(0xFFC25327)

@Composable
fun EscribirScreen(
    viewModel: TintaViewModel,
    modifier: Modifier = Modifier
) {
    val selectedNovelId by viewModel.selectedNovelId.collectAsStateWithLifecycle()
    val currentNovel by viewModel.currentNovel.collectAsStateWithLifecycle()
    val activeChapters by viewModel.activeChapters.collectAsStateWithLifecycle()
    val activeChapterId by viewModel.activeChapterId.collectAsStateWithLifecycle()
    val chapterContent by viewModel.chapterContent.collectAsStateWithLifecycle()
    val isFinished by viewModel.isChapterFinished.collectAsStateWithLifecycle()
    val chapterStatus by viewModel.chapterStatus.collectAsStateWithLifecycle()
    val isSavingContent by viewModel.isSavingContent.collectAsStateWithLifecycle()
    val paperTexture by viewModel.paperTexture.collectAsStateWithLifecycle()
    val selectedFont by viewModel.selectedFont.collectAsStateWithLifecycle()
    val marginNotes by viewModel.activeNotes.collectAsStateWithLifecycle()

    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val timerInitialMinutes by viewModel.timerInitialMinutes.collectAsStateWithLifecycle()
    val wordsWrittenInSession by viewModel.wordsWrittenInSession.collectAsStateWithLifecycle()
    val isDarkThemeState by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val isDark = isDarkThemeState

    var tfv by remember(activeChapterId) {
        mutableStateOf(TextFieldValue(text = chapterContent, selection = TextRange(chapterContent.length)))
    }

    LaunchedEffect(chapterContent, activeChapterId) {
        if (tfv.text != chapterContent) {
            tfv = TextFieldValue(
                text = chapterContent,
                selection = TextRange(chapterContent.length)
            )
        }
    }

    var isChapterDropdownExpanded by remember { mutableStateOf(false) }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    val currentChapter = activeChapters.find { it.id == activeChapterId }
    val currentChapterIndex = activeChapters.indexOfFirst { it.id == activeChapterId }
    val hasPreviousChapter = currentChapterIndex > 0
    val hasNextChapter = currentChapterIndex >= 0 && currentChapterIndex < activeChapters.size - 1

    val textPrimary = if (isDark) Color(0xFFF1EDE6) else Color(0xFF1C1917)
    val textMuted = if (isDark) Color(0xFFA8A29E) else Color(0xFF57534E)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedNovelId.isNullOrBlank() || currentNovel == null) {
                // Empty State when no novel is selected
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("escribir_empty_state_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = null,
                            tint = TintaAccent,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No hay ninguna historia seleccionada. Selecciona una historia en la Biblioteca para escribir en el editor.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.selectTab(TintaTab.BIBLIOTECA) },
                            colors = ButtonDefaults.buttonColors(containerColor = TintaAccent),
                            modifier = Modifier.testTag("empty_state_go_to_biblioteca_button")
                        ) {
                            Text("Ir a la Biblioteca")
                        }
                    }
                }
            } else if (activeChapters.isEmpty()) {
                // Empty chapters state for active novel
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("no_chapters_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = TintaAccent,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "La historia '${currentNovel?.title}' aún no tiene ningún capítulo.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.createChapter() },
                            colors = ButtonDefaults.buttonColors(containerColor = TintaAccent),
                            modifier = Modifier.testTag("create_first_chapter_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Crear Primer Capítulo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // 1. Chapter Switcher Pill Bar
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (hasPreviousChapter) {
                                    viewModel.selectChapter(activeChapters[currentChapterIndex - 1].id)
                                }
                            },
                            enabled = hasPreviousChapter,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Capítulo anterior",
                                tint = if (hasPreviousChapter) textMuted else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Chapter Selector Menu Trigger
                        Box(modifier = Modifier.weight(1f)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { isChapterDropdownExpanded = true }
                                    .padding(vertical = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = currentNovel?.title?.uppercase() ?: "MI NOVELA",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TintaAccent,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = currentChapter?.let { "Cap. ${it.number}: ${it.title}" } ?: "Seleccionar Capítulo",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Desplegar capítulos",
                                        tint = textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isChapterDropdownExpanded,
                                onDismissRequest = { isChapterDropdownExpanded = false }
                            ) {
                                activeChapters.forEach { chapter ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Cap. ${chapter.number}: ${chapter.title}",
                                                    fontWeight = if (chapter.id == activeChapterId) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (chapter.id == activeChapterId) TintaAccent else textPrimary
                                                )
                                                if (chapter.isFinished) {
                                                    Text(" ✓", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectChapter(chapter.id)
                                            isChapterDropdownExpanded = false
                                        }
                                    )
                                }

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TintaAccent, modifier = Modifier.size(16.dp))
                                            Text(text = "Nuevo Capítulo", color = TintaAccent, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        isChapterDropdownExpanded = false
                                        viewModel.createChapter()
                                    }
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (hasNextChapter) {
                                    viewModel.selectChapter(activeChapters[currentChapterIndex + 1].id)
                                }
                            },
                            enabled = hasNextChapter,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Capítulo siguiente",
                                tint = if (hasNextChapter) textMuted else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // 2. Compact Pomodoro Bar & Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interactive Pomodoro Widget
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.clip(CircleShape)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TintaAccent)
                            )
                            val minutes = timerSeconds / 60
                            val seconds = timerSeconds % 60
                            val timerFormatted = String.format("%02d:%02d", minutes, seconds)

                            Row(
                                modifier = Modifier.clickable { showTimerDialog = true },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = timerFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Ajustar tiempo",
                                    tint = textMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleTimer() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isTimerRunning) "Pausar" else "Iniciar",
                                    tint = TintaAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            IconButton(
                                onClick = { showTimerDialog = true },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Configurar / Reiniciar temporizador",
                                    tint = textMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    // Chapter Status Dropdown & Dynamic Save Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Chapter Status Selector Dropdown
                        Box {
                            val statusBgColor = when (chapterStatus) {
                                "Terminado" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                "En Revisión" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                            val statusTextColor = when (chapterStatus) {
                                "Terminado" -> Color(0xFF10B981)
                                "En Revisión" -> Color(0xFF3B82F6)
                                else -> textMuted
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusBgColor,
                                border = BorderStroke(1.dp, statusTextColor.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { isStatusDropdownExpanded = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    val statusLabel = when (chapterStatus) {
                                        "Terminado" -> "✓ Terminado"
                                        "En Revisión" -> "🔍 En Revisión"
                                        else -> "📝 Borrador"
                                    }
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = statusTextColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Cambiar estado",
                                        tint = statusTextColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isStatusDropdownExpanded,
                                onDismissRequest = { isStatusDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = textMuted, modifier = Modifier.size(14.dp))
                                            Text("Borrador", color = textPrimary)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setChapterStatus("Borrador")
                                        isStatusDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                                            Text("En Revisión", color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setChapterStatus("En Revisión")
                                        isStatusDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                            Text("Terminado", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setChapterStatus("Terminado")
                                        isStatusDropdownExpanded = false
                                    }
                                )
                            }
                        }

                        // Dynamic Auto-Save State Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            if (isSavingContent) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = TintaAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Guardando...",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TintaAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Guardado en local",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // 3. Manuscript Paper Sheet
                val canvasBg = when (paperTexture) {
                    "parchment" -> if (isDark) TintaCreamDark else TintaCreamLight
                    "lined" -> if (isDark) TintaPaperLinedDark else Color(0xFFFBFBFA)
                    "grid" -> if (isDark) TintaPaperGridDark else Color(0xFFF8FAFC)
                    else -> if (isDark) MaterialTheme.colorScheme.surfaceContainerLow else Color(0xFFFFFFFF)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = canvasBg,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manuscript_canvas")
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Pattern overlay for Lined / Grid
                        if (paperTexture == "lined" || paperTexture == "grid") {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val strokeWidth = 1.2.dp.toPx()
                                val step = 28.dp.toPx()
                                var y = step
                                while (y < size.height) {
                                    drawLine(
                                        color = if (paperTexture == "lined") {
                                            if (isDark) Color(0xFF64748B).copy(alpha = 0.5f) else Color(0xFF60A5FA).copy(alpha = 0.65f)
                                        } else {
                                            if (isDark) Color(0xFF475569).copy(alpha = 0.4f) else Color(0xFF94A3B8).copy(alpha = 0.5f)
                                        },
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                    y += step
                                }
                                if (paperTexture == "lined") {
                                    // Classic ruled notebook vertical red margin line
                                    val marginX = 26.dp.toPx()
                                    drawLine(
                                        color = if (isDark) Color(0xFFEF4444).copy(alpha = 0.35f) else Color(0xFFF87171).copy(alpha = 0.7f),
                                        start = Offset(marginX, 0f),
                                        end = Offset(marginX, size.height),
                                        strokeWidth = 1.2.dp.toPx()
                                    )
                                }
                                if (paperTexture == "grid") {
                                    var x = step
                                    while (x < size.width) {
                                        drawLine(
                                            color = if (isDark) Color(0xFF475569).copy(alpha = 0.4f) else Color(0xFF94A3B8).copy(alpha = 0.5f),
                                            start = Offset(x, 0f),
                                            end = Offset(x, size.height),
                                            strokeWidth = strokeWidth
                                        )
                                        x += step
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Sheet Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "MANUSCRITO ORIGINAL",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TintaAccent,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    )
                                    Text(
                                        text = "Primera edición · Borrador de autor",
                                        style = TextStyle(fontFamily = NewsreaderFontFamily, fontStyle = FontStyle.Italic, fontSize = 11.sp),
                                        color = textMuted
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = currentChapter?.title?.uppercase() ?: "MANUSCRITO",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = textMuted,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Editable Prose Editor (WYSIWYG Markdown Visual Transformation)
                            val editorFontFamily = if (selectedFont == "Newsreader") NewsreaderFontFamily else PlusJakartaSansFontFamily
                            BasicTextField(
                                value = tfv,
                                onValueChange = { newTfv ->
                                    tfv = newTfv
                                    if (newTfv.text != chapterContent) {
                                        viewModel.updateChapterContent(newTfv.text)
                                    }
                                },
                                visualTransformation = MarkdownWysiwygVisualTransformation(editorFontFamily, isDark),
                                textStyle = TextStyle(
                                    fontFamily = editorFontFamily,
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp,
                                    color = textPrimary
                                ),
                                cursorBrush = SolidColor(TintaAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                                    .testTag("editable_manuscript_text")
                            )

                            // Page Watermark
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Text(
                                    text = "PÁG. 1 · TINTA & TRAMA",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = textMuted.copy(alpha = 0.6f),
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }
                    }
                }
            }

            // Margin Notes Section
            SectionMarginNotes(
                notes = marginNotes,
                textPrimary = textPrimary,
                onAddPostIt = { viewModel.openCreateModal("note") },
                onDeleteNote = { noteId -> viewModel.deleteMarginNote(noteId) }
            )

            Spacer(modifier = Modifier.height(130.dp))
        }

        // 4. Ergonomic Writer's Dock Toolbar (Bottom Dock)
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.98f),
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top Row: Paper Textures Pill Tabs
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "PAPEL:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = textMuted,
                        fontWeight = FontWeight.Bold
                    )

                    PaperChip(
                        label = "Pergamino",
                        dotColor = Color(0xFFD97706),
                        isSelected = paperTexture == "parchment",
                        isDark = isDark,
                        onClick = { viewModel.setPaperTexture("parchment") }
                    )
                    PaperChip(
                        label = "Limpio",
                        dotColor = Color(0xFFA8A29E),
                        isSelected = paperTexture == "plain",
                        isDark = isDark,
                        onClick = { viewModel.setPaperTexture("plain") }
                    )
                    PaperChip(
                        label = "Rayado",
                        dotColor = Color(0xFF60A5FA),
                        isSelected = paperTexture == "lined",
                        isDark = isDark,
                        onClick = { viewModel.setPaperTexture("lined") }
                    )
                    PaperChip(
                        label = "Retícula",
                        dotColor = Color(0xFF34D399),
                        isSelected = paperTexture == "grid",
                        isDark = isDark,
                        onClick = { viewModel.setPaperTexture("grid") }
                    )
                }

                // Bottom Row: Formatting Tools & Musas Assistant
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Formatting Tools Group
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FormatToolButton(text = "B", isBold = true, textPrimary = textPrimary, onClick = {
                            viewModel.applyMarkdownFormat("bold", tfv.selection.start, tfv.selection.end)
                        })
                        FormatToolButton(text = "I", isItalic = true, textPrimary = textPrimary, onClick = {
                            viewModel.applyMarkdownFormat("italic", tfv.selection.start, tfv.selection.end)
                        })
                        FormatToolButton(text = "H1", isBold = true, textPrimary = textPrimary, onClick = {
                            viewModel.applyMarkdownFormat("header", tfv.selection.start, tfv.selection.end)
                        })
                        FormatIconButton(icon = Icons.Default.FormatQuote, onClick = {
                            viewModel.applyMarkdownFormat("quote", tfv.selection.start, tfv.selection.end)
                        })
                        FormatIconButton(icon = Icons.Default.FormatListBulleted, onClick = {
                            viewModel.applyMarkdownFormat("list", tfv.selection.start, tfv.selection.end)
                        })
                    }

                    // Divider
                    Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))

                    // Assistant Group
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Musas Creative Craft Assist
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TintaAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, TintaAccent.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.showToast("✨ Musas: 'Prueba cambiar el ritmo de la frase introduciendo una metáfora sensorial'")
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, tint = TintaAccent, modifier = Modifier.size(14.dp))
                                Text("Musas", style = MaterialTheme.typography.labelSmall, color = TintaAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Pomodoro Timer Customization Dialog
        if (showTimerDialog) {
            PomodoroTimerDialog(
                initialMinutes = timerInitialMinutes,
                onDismiss = { showTimerDialog = false },
                onReset = {
                    viewModel.resetTimer()
                    showTimerDialog = false
                },
                onSelectDuration = { minutes ->
                    viewModel.setTimerDuration(minutes)
                    showTimerDialog = false
                }
            )
        }
    }
}

@Composable
private fun PomodoroTimerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onSelectDuration: (Int) -> Unit
) {
    var customMinutesInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = TintaAccent)
                Text(text = "Ajustes de Cronómetro")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Selecciona un tiempo predefinido o reinicia el cronómetro para tu sesión de escritura:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 25, 45, 60).forEach { mins ->
                        val isSelected = initialMinutes == mins
                        OutlinedButton(
                            onClick = { onSelectDuration(mins) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSelected) TintaAccent else MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) TintaAccent.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${mins}m",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TintaAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Custom duration input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customMinutesInput,
                        onValueChange = { customMinutesInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Personalizado (min)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val mins = customMinutesInput.toIntOrNull()
                            if (mins != null && mins in 1..180) {
                                onSelectDuration(mins)
                            }
                        },
                        enabled = customMinutesInput.isNotBlank() && (customMinutesInput.toIntOrNull() ?: 0) in 1..180,
                        colors = ButtonDefaults.buttonColors(containerColor = TintaAccent)
                    ) {
                        Text("Aplicar")
                    }
                }

                // Reset button
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reiniciar Cronómetro (${initialMinutes}m)", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun PaperChip(
    label: String,
    dotColor: Color,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, if (isSelected) TintaAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun FormatToolButton(
    text: String,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    textPrimary: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                color = textPrimary
            )
        }
    }
}

@Composable
private fun FormatIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}


@Composable
private fun SectionMarginNotes(
    notes: List<MarginNote>,
    textPrimary: Color,
    onAddPostIt: () -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = TintaAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Notas al Margen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Button(
                onClick = onAddPostIt,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TintaAccent,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nuevo Post-it", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }

        notes.forEachIndexed { index, note ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = TintaAccent,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { onDeleteNote(note.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar nota",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary
                    )
                }
            }
        }
    }
}
