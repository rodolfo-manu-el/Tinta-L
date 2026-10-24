package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CharacterEntity
import com.example.data.model.Relationship
import com.example.ui.TintaViewModel

@Composable
fun PersonajesScreen(
    viewModel: TintaViewModel,
    modifier: Modifier = Modifier
) {
    val selectedNovelId by viewModel.selectedNovelId.collectAsStateWithLifecycle()
    val personajesTab by viewModel.personajesTab.collectAsStateWithLifecycle()
    val characters by viewModel.activeCharacters.collectAsStateWithLifecycle()
    val relationships by viewModel.activeRelationships.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Segmented Switcher: Fichas vs Mapa en Corcho
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (personajesTab == "fichas") MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable { viewModel.setPersonajesTab("fichas") }
                        .testTag("tab_fichas")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = if (personajesTab == "fichas") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fichas de Personajes",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (personajesTab == "fichas") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (personajesTab == "mapa") MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable { viewModel.setPersonajesTab("mapa") }
                        .testTag("tab_mapa")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = if (personajesTab == "mapa") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mapa en Corcho",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (personajesTab == "mapa") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Narrative Progress Status Bar
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Dramatis Personae",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (characters.isEmpty()) "ELENCO VACÍO" else "REGISTRO DE PERSONAJES",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${characters.size} ${if (characters.size == 1) "personaje" else "personajes"}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (characters.isEmpty()) "Sin vínculos" else "Red activa",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        if (selectedNovelId.isNullOrBlank()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("personajes_empty_state_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "No hay ninguna historia seleccionada. Selecciona una historia en la Biblioteca para gestionar sus personajes.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectTab(com.example.ui.TintaTab.BIBLIOTECA) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.testTag("empty_state_go_to_biblioteca_button")
                        ) {
                            Text("Ir a la Biblioteca")
                        }
                    }
                }
            }
        } else if (characters.isEmpty()) {
            // Native empty state when no characters exist for active novel
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("personajes_empty_state_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "Esta historia aún no tiene personajes guardados. Comienza a añadir personajes a tu elenco.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.openCreateModal("character") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("empty_state_create_character_button")
                    ) {
                        Text("Añadir personaje")
                    }
                }
            }
        } else if (personajesTab == "mapa") {
            // Corkboard Interactive View
            InteractiveCorkboard(
                characters = characters,
                relationships = relationships,
                onCreateRelationship = { from, to, type, desc ->
                    viewModel.createRelationship(from, to, type, desc)
                },
                onDeleteRelationship = { relId ->
                    viewModel.deleteRelationship(relId)
                }
            )
        } else {
            var editingCharacter by remember { mutableStateOf<CharacterEntity?>(null) }
            var viewingCharacter by remember { mutableStateOf<CharacterEntity?>(null) }

            // Real characters list
            characters.forEach { char ->
                CardGenericCharacter(
                    character = char,
                    onClick = { viewingCharacter = char },
                    onEdit = { editingCharacter = char }
                )
            }

            // Edit Character Dialog
            editingCharacter?.let { char ->
                var name by remember { mutableStateOf(char.name) }
                var roleTag by remember { mutableStateOf(char.roleTag) }
                var bio by remember { mutableStateOf(char.bio) }
                var age by remember { mutableStateOf(char.age) }
                var species by remember { mutableStateOf(char.species) }
                var ability by remember { mutableStateOf(char.specialAbility) }

                AlertDialog(
                    onDismissRequest = { editingCharacter = null },
                    title = { Text("Editar Personaje: ${char.name}") },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = roleTag,
                                onValueChange = { roleTag = it },
                                label = { Text("Rol / Arquetipo") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text("Edad") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = species,
                                onValueChange = { species = it },
                                label = { Text("Especie") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = ability,
                                onValueChange = { ability = it },
                                label = { Text("Habilidad Especial") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Biografía / Notas") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val updated = char.copy(
                                    name = name,
                                    roleTag = roleTag,
                                    epithet = roleTag,
                                    bio = bio,
                                    age = age,
                                    species = species,
                                    specialAbility = ability
                                )
                                viewModel.updateCharacter(updated)
                                editingCharacter = null
                            }
                        ) {
                            Text("Guardar Cambios")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingCharacter = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // View Detail Character Dialog
            viewingCharacter?.let { char ->
                AlertDialog(
                    onDismissRequest = { viewingCharacter = null },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(char.name)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ROL: ${char.roleTag.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text("EDAD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(char.age, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("ESPECIE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(char.species, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            Column {
                                Text("HABILIDAD / DESTREZA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(char.specialAbility, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                            }

                            Column {
                                Text("BIOGRAFÍA / HISTORIAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(char.bio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            editingCharacter = char
                            viewingCharacter = null
                        }) {
                            Text("Editar Ficha")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewingCharacter = null }) {
                            Text("Cerrar")
                        }
                    }
                )
            }

            // Fast Action Drawer Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.showToast("Filtrando por facciones del cuadrante") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Filtrar por Facciones", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = { viewModel.showToast("Elenco ordenado alfabéticamente") },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(imageVector = Icons.Default.SortByAlpha, contentDescription = "Ordenar A-Z", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun CardGenericCharacter(
    character: CharacterEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = character.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = character.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = character.epithet, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(text = character.bio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.EditNote, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun InteractiveCorkboard(
    characters: List<CharacterEntity>,
    relationships: List<Relationship>,
    onCreateRelationship: (String, String, String, String) -> Unit,
    onDeleteRelationship: (Long) -> Unit
) {
    var showAddRelDialog by remember { mutableStateOf(false) }
    var selectedCharacterName by remember { mutableStateOf<String?>(null) }
    val characterPositions = remember { mutableStateMapOf<String, Offset>() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Red de Tensión y Lealtades (${relationships.size} vínculos)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = { showAddRelDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.defaultMinSize(minHeight = 36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Nuevo Vínculo",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // Selected Character Filter Banner
        selectedCharacterName?.let { selName ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Enfocando vínculos de: $selName",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { selectedCharacterName = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar filtro",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Cork Board Container with Canvas and Draggable Cards
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1714)),
            border = BorderStroke(2.dp, Color(0xFF7C4D3B).copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(470.dp)
                .testTag("corkboard_canvas")
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val maxWidthPx = constraints.maxWidth.toFloat()
                val maxHeightPx = constraints.maxHeight.toFloat()
                val density = LocalDensity.current
                val cardWidthDp = 136.dp
                val cardHeightDp = 86.dp
                val cardWidthPx = with(density) { cardWidthDp.toPx() }
                val cardHeightPx = with(density) { cardHeightDp.toPx() }
                val pinOffsetY = with(density) { 10.dp.toPx() }

                // Automatically position characters smoothly when loaded
                LaunchedEffect(characters, maxWidthPx, maxHeightPx) {
                    if (maxWidthPx > 0 && maxHeightPx > 0 && characters.isNotEmpty()) {
                        val total = characters.size
                        characters.forEachIndexed { index, char ->
                            if (!characterPositions.containsKey(char.name)) {
                                val pos = when {
                                    total == 1 -> {
                                        Offset(
                                            x = (maxWidthPx - cardWidthPx) / 2f,
                                            y = (maxHeightPx - cardHeightPx) / 2f
                                        )
                                    }
                                    total == 2 -> {
                                        if (index == 0) Offset(20f, (maxHeightPx - cardHeightPx) / 2f)
                                        else Offset(maxWidthPx - cardWidthPx - 20f, (maxHeightPx - cardHeightPx) / 2f)
                                    }
                                    total <= 4 -> {
                                        when (index) {
                                            0 -> Offset(18f, 24f)
                                            1 -> Offset(maxWidthPx - cardWidthPx - 18f, 32f)
                                            2 -> Offset(24f, maxHeightPx - cardHeightPx - 36f)
                                            else -> Offset(maxWidthPx - cardWidthPx - 24f, maxHeightPx - cardHeightPx - 28f)
                                        }
                                    }
                                    else -> {
                                        val centerX = (maxWidthPx - cardWidthPx) / 2f
                                        val centerY = (maxHeightPx - cardHeightPx) / 2f
                                        val radiusX = ((maxWidthPx - cardWidthPx) / 2f) * 0.72f
                                        val radiusY = ((maxHeightPx - cardHeightPx) / 2f) * 0.72f
                                        val angle = (2 * Math.PI * index / total) - (Math.PI / 2)
                                        Offset(
                                            x = (centerX + radiusX * kotlin.math.cos(angle)).toFloat()
                                                .coerceIn(12f, maxWidthPx - cardWidthPx - 12f),
                                            y = (centerY + radiusY * kotlin.math.sin(angle)).toFloat()
                                                .coerceIn(12f, maxHeightPx - cardHeightPx - 12f)
                                        )
                                    }
                                }
                                characterPositions[char.name] = pos
                            }
                        }
                    }
                }

                if (characters.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "El mapa de corcho está esperando a tus personajes",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Crea personajes en 'Fichas de Personajes' para vincularlos con hilos de tensión y alianzas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Canvas to draw cork texture & real dynamic relationship threads
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Cork texture dots
                        val step = 32.dp.toPx()
                        var gx = step
                        while (gx < size.width) {
                            var gy = step
                            while (gy < size.height) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.035f),
                                    radius = 1.2f,
                                    center = Offset(gx, gy)
                                )
                                gy += step
                            }
                            gx += step
                        }

                        // Draw threads between character pins
                        relationships.forEach { rel ->
                            val fromCardPos = characterPositions.entries.firstOrNull {
                                it.key.trim().equals(rel.fromCharacterName.trim(), ignoreCase = true)
                            }?.value
                            val toCardPos = characterPositions.entries.firstOrNull {
                                it.key.trim().equals(rel.toCharacterName.trim(), ignoreCase = true)
                            }?.value

                            if (fromCardPos != null && toCardPos != null) {
                                val p1 = Offset(fromCardPos.x + cardWidthPx / 2f, fromCardPos.y + pinOffsetY)
                                val p2 = Offset(toCardPos.x + cardWidthPx / 2f, toCardPos.y + pinOffsetY)

                                val (baseColor, pathEffect) = getRelationshipStyle(rel.type)

                                val isHighlighted = selectedCharacterName == null ||
                                        rel.fromCharacterName.trim().equals(selectedCharacterName?.trim(), ignoreCase = true) ||
                                        rel.toCharacterName.trim().equals(selectedCharacterName?.trim(), ignoreCase = true)

                                val lineColor = if (isHighlighted) baseColor else baseColor.copy(alpha = 0.16f)
                                val strokeW = if (selectedCharacterName != null && isHighlighted) 4.5f else 3f

                                if (isHighlighted) {
                                    // Outer thread glow
                                    drawLine(
                                        color = lineColor.copy(alpha = 0.22f),
                                        start = p1,
                                        end = p2,
                                        strokeWidth = strokeW + 4f,
                                        pathEffect = pathEffect
                                    )
                                }

                                // Main thread line
                                drawLine(
                                    color = lineColor,
                                    start = p1,
                                    end = p2,
                                    strokeWidth = strokeW,
                                    pathEffect = pathEffect
                                )
                            }
                        }
                    }

                    // Interactive Draggable Character Cards
                    characters.forEachIndexed { idx, char ->
                        val pos = characterPositions[char.name] ?: Offset(
                            x = (20f + (idx * 24)).coerceAtMost(maxWidthPx - cardWidthPx - 10f),
                            y = (24f + (idx * 28)).coerceAtMost(maxHeightPx - cardHeightPx - 10f)
                        )
                        val isSelected = selectedCharacterName?.trim().equals(char.name.trim(), ignoreCase = true)
                        val pinColor = getCharacterPinColor(char, idx)

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                                .pointerInput(char.name) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val curr = characterPositions[char.name] ?: pos
                                        val newX = (curr.x + dragAmount.x).coerceIn(8f, maxWidthPx - cardWidthPx - 8f)
                                        val newY = (curr.y + dragAmount.y).coerceIn(8f, maxHeightPx - cardHeightPx - 8f)
                                        characterPositions[char.name] = Offset(newX, newY)
                                    }
                                }
                                .clickable {
                                    selectedCharacterName = if (isSelected) null else char.name
                                }
                        ) {
                            CorkCard(
                                name = char.name,
                                role = char.roleTag.ifBlank { "Personaje" },
                                tag = char.epithet.ifBlank { char.faction.ifBlank { "Trama" } },
                                pinColor = pinColor,
                                isSelected = isSelected
                            )
                        }
                    }

                    // Reset positions & Helper hint controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Arrastra con el dedo • Toca para enfocar",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    }

                    IconButton(
                        onClick = {
                            characterPositions.clear()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Restablecer posiciones",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Listado de hilos y relaciones narrativas del corcho con opción de eliminar
        if (relationships.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Hilos del Mapa de Relaciones",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                relationships.forEach { rel ->
                    val (badgeColor, _) = getRelationshipStyle(rel.type)
                    val isLinkedToSelected = selectedCharacterName == null ||
                            rel.fromCharacterName.trim().equals(selectedCharacterName?.trim(), ignoreCase = true) ||
                            rel.toCharacterName.trim().equals(selectedCharacterName?.trim(), ignoreCase = true)

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLinkedToSelected && selectedCharacterName != null)
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        border = if (isLinkedToSelected && selectedCharacterName != null)
                            BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.6f))
                        else
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("relationship_card_${rel.id}")
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = rel.fromCharacterName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = rel.toCharacterName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = badgeColor.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = rel.type,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteRelationship(rel.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar vínculo",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            if (rel.description.isNotBlank()) {
                                Text(
                                    text = rel.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddRelDialog) {
            AddRelationshipDialog(
                characters = characters,
                onDismiss = { showAddRelDialog = false },
                onConfirm = { from, to, type, desc ->
                    onCreateRelationship(from, to, type, desc)
                    showAddRelDialog = false
                }
            )
        }
    }
}

private fun getRelationshipStyle(type: String): Pair<Color, PathEffect?> {
    val lower = type.lowercase()
    return when {
        lower.contains("enemist") || lower.contains("rival") || lower.contains("odio") || lower.contains("conflicto") || lower.contains("antagonis") -> {
            Pair(Color(0xFFEF4444), null)
        }
        lower.contains("alianza") || lower.contains("amistad") || lower.contains("lealtad") || lower.contains("amor") || lower.contains("apoyo") -> {
            Pair(Color(0xFF10B981), null)
        }
        lower.contains("secreto") || lower.contains("traición") || lower.contains("sospecha") || lower.contains("oculto") || lower.contains("tensión") -> {
            Pair(Color(0xFFF59E0B), PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
        }
        lower.contains("famil") || lower.contains("mentor") || lower.contains("pacto") || lower.contains("herman") || lower.contains("sangre") -> {
            Pair(Color(0xFF38BDF8), PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
        }
        else -> {
            Pair(Color(0xFFA855F7), null)
        }
    }
}

private fun getCharacterPinColor(character: CharacterEntity, index: Int): Color {
    val role = character.roleTag.lowercase()
    return when {
        character.isProtagonist || role.contains("protagonista") -> Color(0xFFFB7185)
        character.isDeuteragonist || role.contains("deuteragonista") -> Color(0xFF34D399)
        role.contains("antagonista") || role.contains("villano") -> Color(0xFFEF4444)
        role.contains("aliad") || role.contains("secundar") -> Color(0xFF38BDF8)
        role.contains("mentor") || role.contains("guía") -> Color(0xFFFBBF24)
        else -> {
            val palette = listOf(
                Color(0xFFFB7185), Color(0xFF34D399), Color(0xFF38BDF8),
                Color(0xFFFBBF24), Color(0xFFA78BFA), Color(0xFFF472B6)
            )
            palette[index % palette.size]
        }
    }
}

@Composable
private fun AddRelationshipDialog(
    characters: List<CharacterEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    if (characters.size < 2) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Personajes insuficientes", fontWeight = FontWeight.Bold) },
            text = {
                Text("Necesitas al menos 2 personajes registrados en la obra para trazar vínculos narrativos entre ellos.")
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Entendido")
                }
            }
        )
        return
    }

    var fromName by remember { mutableStateOf(characters[0].name) }
    var toName by remember { mutableStateOf(characters.getOrNull(1)?.name ?: characters[0].name) }
    var relType by remember { mutableStateOf("Alianza Estratégica") }
    var description by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val presetTypes = listOf(
        "Alianza Estratégica",
        "Rivalidad Mortal",
        "Pacto Secreto",
        "Lealtad / Amistad",
        "Tensión / Sospecha",
        "Mentoría",
        "Familia / Sangre",
        "Amor / Romance"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Vínculo de Trama", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector Personaje Origen
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Personaje Origen",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        characters.forEach { char ->
                            val isSelected = fromName == char.name
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.clickable {
                                    fromName = char.name
                                    errorText = null
                                }
                            ) {
                                Text(
                                    text = char.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Selector Personaje Destino
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Personaje Destino",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        characters.forEach { char ->
                            val isSelected = toName == char.name
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
                                modifier = Modifier.clickable {
                                    toName = char.name
                                    errorText = null
                                }
                            ) {
                                Text(
                                    text = char.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Tipo de relación y presets
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Tipo de Relación",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = relType,
                        onValueChange = { relType = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetTypes.forEach { preset ->
                            val isPresetSelected = relType.equals(preset, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isPresetSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainer,
                                border = if (isPresetSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.clickable { relType = preset }
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    color = if (isPresetSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detalles del Vínculo (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                errorText?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fromName == toName) {
                        errorText = "El personaje origen y destino no pueden ser el mismo."
                    } else {
                        onConfirm(
                            fromName,
                            toName,
                            relType.ifBlank { "Vínculo" },
                            description.ifBlank { "Relación registrada en el mapa." }
                        )
                    }
                }
            ) {
                Text("Trazar Hilo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun CorkCard(
    name: String,
    role: String,
    tag: String,
    pinColor: Color,
    isSelected: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        shadowElevation = if (isSelected) 10.dp else 4.dp,
        modifier = Modifier.width(136.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            // Pin head visual centered on card top
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 2.dp)
                    .size(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(pinColor)
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.65f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
