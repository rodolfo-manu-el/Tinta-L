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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    val paperTexture by viewModel.paperTexture.collectAsStateWithLifecycle()
    val selectedFont by viewModel.selectedFont.collectAsStateWithLifecycle()
    val marginNotes by viewModel.activeNotes.collectAsStateWithLifecycle()
    val chapterVersions by viewModel.chapterVersions.collectAsStateWithLifecycle()

    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val wordsWrittenInSession by viewModel.wordsWrittenInSession.collectAsStateWithLifecycle()
    val isDarkThemeState by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val isDark = isDarkThemeState || isSystemInDarkTheme()

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
    var showVersionsDialog by remember { mutableStateOf(false) }

    val currentChapter = activeChapters.find { it.id == activeChapterId }
    val currentChapterIndex = activeChapters.indexOfFirst { it.id == activeChapterId }
    val hasPreviousChapter = currentChapterIndex > 0
    val hasNextChapter = currentChapterIndex >= 0 && currentChapterIndex < activeChapters.size - 1

    val textPrimary = if (isDark) Color(0xFFE2E8F0) else Color(0xFF28221E)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF8D7B68)

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
                    // Pomodoro Widget
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.clip(CircleShape)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                            Text(
                                text = timerFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "· $wordsWrittenInSession palabras hoy",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = textMuted
                            )
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
                        }
                    }

                    // Draft status & Save checkmark
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.clickable { viewModel.toggleChapterFinished() }
                        ) {
                            Text(
                                text = if (isFinished) "✓ Terminado" else "Borrador",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = textMuted,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Guardado",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 3. Manuscript Paper Sheet
                val canvasBg = when (paperTexture) {
                    "parchment" -> if (isDark) TintaCreamDark else TintaCreamLight
                    "lined" -> if (isDark) TintaPaperLinedDark else TintaPaperLinedLight
                    "grid" -> if (isDark) TintaPaperGridDark else TintaPaperGridLight
                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                }

                val lineOverlayColor = when (paperTexture) {
                    "parchment" -> if (isDark) Color(0xFF423B32) else Color(0xFFD9C3A3)
                    "lined" -> if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
                    "grid" -> if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                    else -> Color.Transparent
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
                                val strokeWidth = 1f
                                val step = 28.dp.toPx()
                                var y = step
                                while (y < size.height) {
                                    drawLine(
                                        color = lineOverlayColor.copy(alpha = 0.35f),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                    y += step
                                }
                                if (paperTexture == "grid") {
                                    var x = step
                                    while (x < size.width) {
                                        drawLine(
                                            color = lineOverlayColor.copy(alpha = 0.35f),
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

                    // Assistant / History Group
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

                        // History Versions Button
                        IconButton(
                            onClick = { showVersionsDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(imageVector = Icons.Default.History, contentDescription = "Historial", tint = textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Version History Dialog
        if (showVersionsDialog) {
            VersionHistoryDialog(
                versions = chapterVersions,
                onDismiss = { showVersionsDialog = false },
                onRestore = { version ->
                    viewModel.restoreVersion(version)
                    showVersionsDialog = false
                }
            )
        }
    }
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
private fun VersionHistoryDialog(
    versions: List<ChapterVersion>,
    onDismiss: () -> Unit,
    onRestore: (ChapterVersion) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = TintaAccent)
                Text(text = "Historial de Versiones")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (versions.isEmpty()) {
                    Text(
                        text = "Aún no hay versiones o borradores guardados para este capítulo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    versions.forEach { version ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⏱️ ${version.timeFormatted}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TintaAccent
                                    )
                                    Text(
                                        text = "${version.wordCount} palabras",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = version.content.take(80) + if (version.content.length > 80) "..." else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2
                                )
                                OutlinedButton(
                                    onClick = { onRestore(version) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Restaurar")
                                }
                            }
                        }
                    }
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
