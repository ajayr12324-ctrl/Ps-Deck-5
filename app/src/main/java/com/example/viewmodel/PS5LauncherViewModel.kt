package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.model.ActivityCard
import com.example.model.AppItem
import com.example.model.LauncherTab
import com.example.model.TrophyProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PS5UiState(
    val isLoading: Boolean = true,
    val isBootAnimationActive: Boolean = true,
    val selectedTab: LauncherTab = LauncherTab.GAMES,
    val selectedAppIndex: Int = 0,
    val allApps: List<AppItem> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isAddGameOpen: Boolean = false,
    val pickerApps: List<AppItem> = emptyList(),
    val isLoadingPickerApps: Boolean = false,
    val isControlCenterOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isAppDetailOpen: Boolean = false,
    val soundEffectsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val ambientParticlesEnabled: Boolean = true,
    val showOnlyInstalled: Boolean = false,
    val currentTimeString: String = "12:00 PM",
    val controllerBatteryPercent: Int = 85,
    val isMicMuted: Boolean = false,
    val volumeLevel: Float = 0.8f
)

class PS5LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application.applicationContext)
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val _uiState = MutableStateFlow(PS5UiState())
    val uiState: StateFlow<PS5UiState> = _uiState.asStateFlow()

    init {
        loadApps()
        startClockTicker()
    }

    fun finishBootAnimation() {
        performHaptic(isStrong = true)
        _uiState.update { it.copy(isBootAnimationActive = false) }
    }

    fun replayBootAnimation() {
        performHaptic()
        _uiState.update {
            it.copy(
                isBootAnimationActive = true,
                isControlCenterOpen = false,
                isSettingsOpen = false
            )
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val apps = repository.getInstalledApps(includeShowcase = !_uiState.value.showOnlyInstalled)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    allApps = apps,
                    selectedAppIndex = 0.coerceAtMost((apps.size - 1).coerceAtLeast(0))
                )
            }
        }
    }

    fun setAddGameOpen(open: Boolean) {
        performHaptic()
        _uiState.update { it.copy(isAddGameOpen = open) }
        if (open && _uiState.value.pickerApps.isEmpty()) {
            loadPickerApps()
        }
    }

    fun loadPickerApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPickerApps = true) }
            val apps = repository.getAllInstalledAppsForPicker()
            _uiState.update { it.copy(pickerApps = apps, isLoadingPickerApps = false) }
        }
    }

    fun addCustomGame(
        name: String,
        genre: String,
        publisher: String,
        badgeText: String,
        storageSize: String,
        accentColor: androidx.compose.ui.graphics.Color,
        description: String,
        selectedInstalledApp: AppItem?
    ) {
        performHaptic(isStrong = true)
        val gameId = "custom_" + UUID.randomUUID().toString().take(8)
        val customApp = AppItem(
            id = gameId,
            name = name.ifBlank { selectedInstalledApp?.name ?: "Custom Game" },
            packageName = selectedInstalledApp?.packageName ?: "",
            iconBitmap = selectedInstalledApp?.iconBitmap,
            isRealApp = selectedInstalledApp != null,
            tab = LauncherTab.GAMES,
            badgeText = badgeText.ifBlank { "PS5" },
            genre = genre.ifBlank { "Action Adventure" },
            publisher = publisher.ifBlank { "Custom Studio" },
            storageSize = storageSize.ifBlank { "48.2 GB" },
            playTime = "0 hours",
            accentColor = accentColor,
            description = description.ifBlank { "Custom added PlayStation title." },
            isCustomGame = true,
            trophyProgress = TrophyProgress(
                platinum = 0,
                gold = 1,
                silver = 4,
                bronze = 10,
                totalEarned = 15,
                completionPercent = 25
            ),
            activityCards = listOf(
                ActivityCard(
                    id = "act_${gameId}_1",
                    title = "Start Journey",
                    subtitle = "Begin your campaign chapter 1",
                    progressPercent = 10,
                    tag = "NEW GAME",
                    isResumeAction = true
                ),
                ActivityCard(
                    id = "act_${gameId}_2",
                    title = "First Trophy Target",
                    subtitle = "Complete intro tutorial challenge",
                    progressPercent = 0,
                    tag = "TROPHY"
                )
            )
        )

        repository.saveCustomGame(customApp)
        setAddGameOpen(false)
        loadApps()
    }

    fun deleteCustomGame(gameId: String) {
        performHaptic(isStrong = true)
        repository.deleteCustomGame(gameId)
        _uiState.update { it.copy(isAppDetailOpen = false) }
        loadApps()
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            while (isActive) {
                val nowStr = timeFormat.format(Date())
                _uiState.update { it.copy(currentTimeString = nowStr) }
                delay(15_000)
            }
        }
    }

    fun selectTab(tab: LauncherTab) {
        if (_uiState.value.selectedTab == tab) return
        performHaptic()
        _uiState.update { state ->
            state.copy(
                selectedTab = tab,
                selectedAppIndex = 0
            )
        }
    }

    fun selectAppIndex(index: Int) {
        if (_uiState.value.selectedAppIndex == index) return
        performHaptic()
        _uiState.update { it.copy(selectedAppIndex = index) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSearchActive(active: Boolean) {
        performHaptic()
        _uiState.update { it.copy(isSearchActive = active, searchQuery = if (!active) "" else it.searchQuery) }
    }

    fun setControlCenterOpen(open: Boolean) {
        performHaptic()
        _uiState.update { it.copy(isControlCenterOpen = open) }
    }

    fun setSettingsOpen(open: Boolean) {
        performHaptic()
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun setAppDetailOpen(open: Boolean) {
        performHaptic()
        _uiState.update { it.copy(isAppDetailOpen = open) }
    }

    fun toggleHaptics() {
        _uiState.update { it.copy(hapticsEnabled = !it.hapticsEnabled) }
        performHaptic()
    }

    fun toggleSoundEffects() {
        performHaptic()
        _uiState.update { it.copy(soundEffectsEnabled = !it.soundEffectsEnabled) }
    }

    fun toggleAmbientParticles() {
        performHaptic()
        _uiState.update { it.copy(ambientParticlesEnabled = !it.ambientParticlesEnabled) }
    }

    fun toggleShowOnlyInstalled() {
        performHaptic()
        val newValue = !_uiState.value.showOnlyInstalled
        _uiState.update { it.copy(showOnlyInstalled = newValue) }
        loadApps()
    }

    fun toggleMicMute() {
        performHaptic()
        _uiState.update { it.copy(isMicMuted = !it.isMicMuted) }
    }

    fun updateVolume(level: Float) {
        _uiState.update { it.copy(volumeLevel = level.coerceIn(0f, 1f)) }
    }

    fun launchSelectedApp(context: Context) {
        val currentList = getFilteredApps()
        val index = _uiState.value.selectedAppIndex
        if (index in currentList.indices) {
            launchApp(context, currentList[index])
        }
    }

    fun launchApp(context: Context, app: AppItem) {
        performHaptic(isStrong = true)
        repository.launchApp(app.packageName)
    }

    fun getFilteredApps(): List<AppItem> {
        val state = _uiState.value
        val tabApps = state.allApps.filter { it.tab == state.selectedTab }
        if (state.searchQuery.isBlank()) {
            return tabApps
        }
        val query = state.searchQuery.trim().lowercase()
        return tabApps.filter {
            it.name.lowercase().contains(query) || it.genre.lowercase().contains(query)
        }
    }

    fun getCurrentlySelectedApp(): AppItem? {
        val list = getFilteredApps()
        val index = _uiState.value.selectedAppIndex
        return if (index in list.indices) list[index] else list.firstOrNull()
    }

    private fun performHaptic(isStrong: Boolean = false) {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (isStrong) {
                    VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(18, 50)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (isStrong) 40L else 15L)
            }
        } catch (_: Exception) {
        }
    }
}
