package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TintaTab
import com.example.ui.TintaViewModel
import com.example.ui.components.BackupDialog
import com.example.ui.components.CreateItemModal
import com.example.ui.components.TintaBottomBar
import com.example.ui.components.TintaToast
import com.example.ui.components.TintaTopHeader
import com.example.ui.screens.BibliotecaScreen
import com.example.ui.screens.EscribirScreen
import com.example.ui.screens.ObraScreen
import com.example.ui.screens.PersonajesScreen
import com.example.ui.screens.RecursosScreen
import com.example.ui.theme.TintaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TintaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
            val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
            val showCreateModal by viewModel.showCreateModal.collectAsStateWithLifecycle()
            val createType by viewModel.createType.collectAsStateWithLifecycle()
            val showBackupDialog by viewModel.showBackupDialog.collectAsStateWithLifecycle()
            val backupJsonString by viewModel.backupJsonString.collectAsStateWithLifecycle()

            // Handle system back navigation smoothly
            BackHandler(enabled = currentTab != TintaTab.BIBLIOTECA) {
                viewModel.selectTab(TintaTab.BIBLIOTECA)
            }

            TintaTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TintaTopHeader(
                            currentTab = currentTab,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { viewModel.toggleTheme() },
                            modifier = Modifier.statusBarsPadding()
                        )
                    },
                    bottomBar = {
                        TintaBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.selectTab(it) },
                            modifier = Modifier.navigationBarsPadding()
                        )
                    },
                    floatingActionButton = {
                        if (currentTab != TintaTab.ESCRIBIR) {
                            FloatingActionButton(
                                onClick = {
                                    val targetCreate = when (currentTab) {
                                        TintaTab.PERSONAJES -> "character"
                                        else -> "novel"
                                    }
                                    viewModel.openCreateModal(targetCreate)
                                },
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                                modifier = Modifier
                                    .padding(bottom = 12.dp)
                                    .size(56.dp)
                                    .testTag("global_fab_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DriveFileRenameOutline,
                                    contentDescription = "Crear nuevo elemento",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                            when (tab) {
                                TintaTab.BIBLIOTECA -> BibliotecaScreen(viewModel = viewModel)
                                TintaTab.OBRA -> ObraScreen(viewModel = viewModel)
                                TintaTab.ESCRIBIR -> EscribirScreen(viewModel = viewModel)
                                TintaTab.PERSONAJES -> PersonajesScreen(viewModel = viewModel)
                                TintaTab.RECURSOS -> RecursosScreen(viewModel = viewModel)
                            }
                        }

                        // Floating Toast
                        TintaToast(
                            message = toastMessage,
                            onDismiss = { viewModel.dismissToast() },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        )

                        // Modals
                        if (showCreateModal) {
                            CreateItemModal(
                                type = createType,
                                onDismiss = { viewModel.closeCreateModal() },
                                onCreateNovel = { title, genre, target, syn ->
                                    viewModel.createNovel(title, genre, target, syn)
                                },
                                onCreateCharacter = { name, role, spec, bio, abil ->
                                    viewModel.createCharacter(name, role, spec, bio, abil)
                                },
                                onCreateNote = { cat, col, cont, scope ->
                                    viewModel.addMarginNote(cat, col, cont, scope)
                                }
                            )
                        }

                        if (showBackupDialog) {
                            BackupDialog(
                                jsonString = backupJsonString,
                                onDismiss = { viewModel.closeBackupDialog() }
                            )
                        }

                    }
                }
            }
        }
    }
}
