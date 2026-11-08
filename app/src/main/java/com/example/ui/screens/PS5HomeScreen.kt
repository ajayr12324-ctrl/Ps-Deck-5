package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.ui.components.PS5ActivityCardsRow
import com.example.ui.components.PS5AddGameDialog
import com.example.ui.components.PS5ControlCenter
import com.example.ui.components.PS5DynamicBackground
import com.example.ui.components.PS5GameCarousel
import com.example.ui.components.PS5GameDetailsHero
import com.example.ui.components.PS5GameOverviewDialog
import com.example.ui.components.PS5SearchSheet
import com.example.ui.components.PS5SettingsDialog
import com.example.ui.components.PS5TopBar
import com.example.ui.theme.PS5Cyan
import com.example.viewmodel.PS5LauncherViewModel

@Composable
fun PS5HomeScreen(
    viewModel: PS5LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val filteredApps = viewModel.getFilteredApps()
    val selectedApp = viewModel.getCurrentlySelectedApp()

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("ps5_home_screen")
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = PS5Cyan,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ps5_loading_indicator")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading PlayStation Library...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // 1. Dynamic Wallpaper + Ambient particles + Vignette Overlay
            PS5DynamicBackground(
                selectedApp = selectedApp,
                particlesEnabled = uiState.ambientParticlesEnabled
            )

            // 2. Main Console Interface
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = statusBarPadding.calculateTopPadding())
                    .navigationBarsPadding()
            ) {
                // Top Row: Tabs (Games / Media) & Status (Add Game, Search, Battery, Clock, Settings, Profile)
                PS5TopBar(
                    selectedTab = uiState.selectedTab,
                    currentTimeString = uiState.currentTimeString,
                    batteryPercent = uiState.controllerBatteryPercent,
                    onTabSelected = { viewModel.selectTab(it) },
                    onAddGameClick = { viewModel.setAddGameOpen(true) },
                    onSearchClick = { viewModel.setSearchActive(true) },
                    onControllerClick = { viewModel.setControlCenterOpen(true) },
                    onSettingsClick = { viewModel.setSettingsOpen(true) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Game Carousel
                if (filteredApps.isNotEmpty()) {
                    PS5GameCarousel(
                        apps = filteredApps,
                        selectedIndex = uiState.selectedAppIndex,
                        onSelectApp = { viewModel.selectAppIndex(it) },
                        onAddGameClick = { viewModel.setAddGameOpen(true) }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No games found in this category",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap '+' on top to add your first game!",
                                color = PS5Cyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { viewModel.setAddGameOpen(true) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Selected Game Details & Play Button
                if (selectedApp != null) {
                    PS5GameDetailsHero(
                        app = selectedApp,
                        onPlayClick = { viewModel.launchApp(context, selectedApp) },
                        onOptionsClick = { viewModel.setAppDetailOpen(true) }
                    )

                    // PlayStation Activity Cards & Shortcuts
                    if (selectedApp.activityCards.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PS5ActivityCardsRow(
                            activityCards = selectedApp.activityCards,
                            onCardClick = { _ ->
                                viewModel.launchApp(context, selectedApp)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Quick PlayStation Button Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { viewModel.setControlCenterOpen(true) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("ps_home_quick_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "PS Button",
                            tint = PS5Cyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PS Control Center",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Modal Sheets and Dialogs
        PS5ControlCenter(
            isOpen = uiState.isControlCenterOpen,
            batteryPercent = uiState.controllerBatteryPercent,
            isMicMuted = uiState.isMicMuted,
            volumeLevel = uiState.volumeLevel,
            hapticsEnabled = uiState.hapticsEnabled,
            onDismiss = { viewModel.setControlCenterOpen(false) },
            onToggleMic = { viewModel.toggleMicMute() },
            onVolumeChange = { viewModel.updateVolume(it) },
            onToggleHaptics = { viewModel.toggleHaptics() },
            onReloadApps = {
                viewModel.loadApps()
                viewModel.setControlCenterOpen(false)
            }
        )

        PS5AddGameDialog(
            isOpen = uiState.isAddGameOpen,
            pickerApps = uiState.pickerApps,
            isLoadingPickerApps = uiState.isLoadingPickerApps,
            onDismiss = { viewModel.setAddGameOpen(false) },
            onAddGame = { name, genre, publisher, badge, storage, color, desc, app ->
                viewModel.addCustomGame(name, genre, publisher, badge, storage, color, desc, app)
            }
        )

        PS5SearchSheet(
            isOpen = uiState.isSearchActive,
            query = uiState.searchQuery,
            filteredApps = uiState.allApps.filter {
                if (uiState.searchQuery.isBlank()) true
                else it.name.lowercase().contains(uiState.searchQuery.lowercase().trim()) ||
                        it.genre.lowercase().contains(uiState.searchQuery.lowercase().trim())
            },
            onQueryChange = { viewModel.onSearchQueryChange(it) },
            onAppClick = { app -> viewModel.launchApp(context, app) },
            onDismiss = { viewModel.setSearchActive(false) }
        )

        PS5GameOverviewDialog(
            app = selectedApp,
            isOpen = uiState.isAppDetailOpen,
            onDismiss = { viewModel.setAppDetailOpen(false) },
            onLaunch = { app -> viewModel.launchApp(context, app) },
            onDeleteCustomGame = { gameId -> viewModel.deleteCustomGame(gameId) }
        )

        PS5SettingsDialog(
            isOpen = uiState.isSettingsOpen,
            hapticsEnabled = uiState.hapticsEnabled,
            soundEffectsEnabled = uiState.soundEffectsEnabled,
            particlesEnabled = uiState.ambientParticlesEnabled,
            showOnlyInstalled = uiState.showOnlyInstalled,
            onToggleHaptics = { viewModel.toggleHaptics() },
            onToggleSoundEffects = { viewModel.toggleSoundEffects() },
            onToggleParticles = { viewModel.toggleAmbientParticles() },
            onToggleShowOnlyInstalled = { viewModel.toggleShowOnlyInstalled() },
            onReplayBootAnimation = { viewModel.replayBootAnimation() },
            onDismiss = { viewModel.setSettingsOpen(false) }
        )

        // 4. PS5 Authentic Boot Screen Overlay
        AnimatedVisibility(
            visible = uiState.isBootAnimationActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PS5BootScreen(
                onBootFinished = { viewModel.finishBootAnimation() }
            )
        }
    }
}
