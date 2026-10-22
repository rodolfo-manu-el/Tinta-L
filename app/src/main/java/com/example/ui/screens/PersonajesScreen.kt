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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
    onCreateRelationship: (String, String, String, String) -> Unit
) {
    var showAddRelDialog by remember { mutableStateOf(false) }

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
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showAddRelDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nuevo Vínculo", style = MaterialTheme.typography.labelSmall)
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .testTag("corkboard_canvas")
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val p1 = Offset(size.width * 0.28f, size.height * 0.28f)
                    val p2 = Offset(size.width * 0.72f, size.height * 0.32f)
                    val p3 = Offset(size.width * 0.35f, size.height * 0.75f)
                    val p4 = Offset(size.width * 0.75f, size.height * 0.78f)

                    drawLine(
                        color = Color(0xFFFB7185),
                        start = p1,
                        end = p2,
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = p1,
                        end = p3,
                        strokeWidth = 3.5f
                    )
                    drawLine(
                        color = Color(0xFF34D399),
                        start = p2,
                        end = p4,
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color(0xFF38BDF8),
                        start = p1,
                        end = p4,
                        strokeWidth = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                val char1 = characters.getOrNull(0)
                val char2 = characters.getOrNull(1)
                val char3 = characters.getOrNull(2)
                val char4 = characters.getOrNull(3)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 24.dp, top = 36.dp)
                ) {
                    CorkCard(
                        name = char1?.name ?: "Aria Thorne",
                        role = char1?.roleTag ?: "Protagonista",
                        tag = char1?.epithet ?: "Escriba",
                        pinColor = Color(0xFFFB7185)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 24.dp, top = 48.dp)
                ) {
                    CorkCard(
                        name = char2?.name ?: "Mateo Corvus",
                        role = char2?.roleTag ?: "Deuteragonista",
                        tag = char2?.epithet ?: "Cartógrafo",
                        pinColor = Color(0xFF34D399)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 32.dp, bottom = 48.dp)
                ) {
                    CorkCard(
                        name = char3?.name ?: "Lord Valerius",
                        role = char3?.roleTag ?: "Antagonista",
                        tag = char3?.epithet ?: "Inquisidor",
                        pinColor = Color(0xFFEF4444)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 28.dp, bottom = 36.dp)
                ) {
                    CorkCard(
                        name = char4?.name ?: "Célere",
                        role = char4?.roleTag ?: "Compañera Alada",
                        tag = char4?.epithet ?: "Mensajero",
                        pinColor = Color(0xFF38BDF8)
                    )
                }
            }
        }

        // Listado de hilos y relaciones narrativas del corcho
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
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = rel.type,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontWeight = FontWeight.SemiBold
                                    )
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
            var fromName by remember { mutableStateOf(characters.getOrNull(0)?.name ?: "Aria Thorne") }
            var toName by remember { mutableStateOf(characters.getOrNull(1)?.name ?: "Mateo Corvus") }
            var relType by remember { mutableStateOf("Alianza Estratégica") }
            var description by remember { mutableStateOf("Pacto de lealtad mutua en la consecución del manuscrito.") }

            AlertDialog(
                onDismissRequest = { showAddRelDialog = false },
                title = { Text("Añadir Vínculo de Trama") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = fromName,
                            onValueChange = { fromName = it },
                            label = { Text("Personaje Origen") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = toName,
                            onValueChange = { toName = it },
                            label = { Text("Personaje Destino") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = relType,
                            onValueChange = { relType = it },
                            label = { Text("Tipo de Relación") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Detalles del Vínculo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onCreateRelationship(fromName, toName, relType, description)
                            showAddRelDialog = false
                        }
                    ) {
                        Text("Trazar Hilo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddRelDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun CorkCard(
    name: String,
    role: String,
    tag: String,
    pinColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = 6.dp,
        modifier = Modifier.width(130.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(pinColor)
            )
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(role, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.primary)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Text(tag, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                }
            }
        }
    }
}
