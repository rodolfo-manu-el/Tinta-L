package com.example.ui.screens

// Tinta Obra Screen - Updated for streamlined editorial view
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.TintaViewModel

@Composable
fun ObraScreen(
    viewModel: TintaViewModel,
    modifier: Modifier = Modifier
) {
    val selectedNovelId by viewModel.selectedNovelId.collectAsStateWithLifecycle()
    val currentNovelState by viewModel.currentNovel.collectAsStateWithLifecycle()
    val activeChapters by viewModel.activeChapters.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormatForSaf by remember { mutableStateOf<String?>(null) }

    val safSaveLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("*/*")
    ) { uri: android.net.Uri? ->
        val format = exportFormatForSaf
        if (uri != null && format != null) {
            viewModel.saveExportedFileDirectly(format, uri, context)
        }
        exportFormatForSaf = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (selectedNovelId.isNullOrBlank() || currentNovelState == null) {
            // Native Empty State when no novel is selected or loaded
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("obra_empty_state_card")
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "No hay ninguna historia seleccionada. Selecciona una historia en la Biblioteca para ver sus detalles.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectTab(com.example.ui.TintaTab.BIBLIOTECA) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("empty_state_go_to_biblioteca_button")
                        ) {
                            Text("Ir a la Biblioteca")
                        }
                        OutlinedButton(
                            onClick = { viewModel.openCreateModal("novel") },
                            modifier = Modifier.testTag("empty_state_create_novel_button")
                        ) {
                            Text("Crear historia")
                        }
                    }
                }
            }
        } else {
            val currentNovel = currentNovelState!!
            // Obra Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // SAF Cover Picker Launcher
                        val coverPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                        ) { uri: android.net.Uri? ->
                            if (uri != null) {
                                viewModel.updateNovelCover(currentNovel.id, uri, context)
                            }
                        }

                        // Mini Cover with SAF Picker Click
                        val customCoverFile = currentNovel.customCoverPath?.let { java.io.File(it) }
                        Box(
                            modifier = Modifier
                                .width(82.dp)
                                .height(122.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .clickable { coverPickerLauncher.launch("image/*") }
                        ) {
                            if (customCoverFile != null && customCoverFile.exists()) {
                                coil.compose.AsyncImage(
                                    model = customCoverFile,
                                    contentDescription = "Portada",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                val coverRes = when (currentNovel.coverDrawableName) {
                                    "cover_bruma" -> R.drawable.cover_bruma
                                    "cover_helio" -> R.drawable.cover_helio
                                    else -> R.drawable.cover_silencio
                                }
                                Image(
                                    painter = painterResource(id = coverRes),
                                    contentDescription = "Portada",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = currentNovel.status.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Metadata
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (currentNovel.genre.isNotBlank()) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = currentNovel.genre,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = currentNovel.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = currentNovel.subtitle.ifBlank { "Manuscrito maestro" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    // Export Action Button
                    OutlinedButton(
                        onClick = { showExportDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Exportar",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Metrics & Progress
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Progreso del Borrador",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${currentNovel.progressPercent}%",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { currentNovel.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )

                    val selectedNovelWords = if (activeChapters.isNotEmpty()) activeChapters.sumOf { it.wordCount } else currentNovel.wordCount

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBox(
                            value = "${currentNovel.completedChapters} / ${currentNovel.totalChapters}",
                            label = "CAPÍTULOS",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            value = "${String.format("%,d", selectedNovelWords).replace(',', '.')}",
                            label = "PALABRAS",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Capítulos reales del Manuscrito
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Capítulos de la Obra",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(
                            onClick = { viewModel.createChapter() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Añadir capítulo", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (activeChapters.isEmpty()) {
                        Text(
                            text = "Aún no hay capítulos creados para este manuscrito.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeChapters.sortedBy { it.number }.forEach { chapter ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectChapter(chapter.id)
                                            viewModel.openWritingStudio(chapter.id)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (chapter.isFinished) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${chapter.number}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (chapter.isFinished) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = chapter.title.ifBlank { "Capítulo ${chapter.number}" },
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (chapter.isFinished) "Terminado" else "Borrador",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar capítulo",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Multiformat Export Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Exportación Editorial")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Selecciona el formato para exportar el manuscrito completo con sus ${activeChapters.size} capítulos:")

                        ExportFormatOption(
                            title = "📄 Documento PDF",
                            subtitle = "Maquetación con portada y numeración de páginas",
                            onShare = {
                                viewModel.exportManuscript("pdf", context)
                                showExportDialog = false
                            },
                            onSaveSaf = {
                                exportFormatForSaf = "pdf"
                                safSaveLauncher.launch("${currentNovelState?.title?.lowercase()?.replace("[^a-z0-9]".toRegex(), "_") ?: "manuscrito"}.pdf")
                                showExportDialog = false
                            }
                        )
                        ExportFormatOption(
                            title = "📖 Libro Electrónico EPUB",
                            subtitle = "Compatible con e-readers (Kindle, Kobo, Apple Books)",
                            onShare = {
                                viewModel.exportManuscript("epub", context)
                                showExportDialog = false
                            },
                            onSaveSaf = {
                                exportFormatForSaf = "epub"
                                safSaveLauncher.launch("${currentNovelState?.title?.lowercase()?.replace("[^a-z0-9]".toRegex(), "_") ?: "manuscrito"}.epub")
                                showExportDialog = false
                            }
                        )
                        ExportFormatOption(
                            title = "📝 Texto Markdown (.md)",
                            subtitle = "Formato plano para respaldos y procesadores externos",
                            onShare = {
                                viewModel.exportManuscript("md", context)
                                showExportDialog = false
                            },
                            onSaveSaf = {
                                exportFormatForSaf = "md"
                                safSaveLauncher.launch("${currentNovelState?.title?.lowercase()?.replace("[^a-z0-9]".toRegex(), "_") ?: "manuscrito"}.md")
                                showExportDialog = false
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ExportFormatOption(
    title: String,
    subtitle: String,
    onShare: () -> Unit,
    onSaveSaf: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Compartir", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onSaveSaf,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar en...", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}



@Composable
private fun MetricBox(
    value: String,
    label: String,
    isSecondary: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = if (isSecondary) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

