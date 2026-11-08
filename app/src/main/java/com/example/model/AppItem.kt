package com.example.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

enum class LauncherTab {
    GAMES,
    MEDIA
}

data class TrophyProgress(
    val platinum: Int = 0,
    val gold: Int = 0,
    val silver: Int = 0,
    val bronze: Int = 0,
    val totalEarned: Int = 0,
    val completionPercent: Int = 0
)

data class ActivityCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val progressPercent: Int,
    val tag: String,
    val isResumeAction: Boolean = false
)

data class AppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val iconBitmap: Bitmap? = null,
    val isRealApp: Boolean = true,
    val tab: LauncherTab = LauncherTab.GAMES,
    val badgeText: String = "PS5",
    val genre: String = "Application",
    val publisher: String = "Installed Application",
    val trophyProgress: TrophyProgress = TrophyProgress(),
    val activityCards: List<ActivityCard> = emptyList(),
    val storageSize: String = "2.4 GB",
    val playTime: String = "12h 45m",
    val accentColor: Color = Color(0xFF0070D1),
    val description: String = "",
    val isCustomGame: Boolean = false
)
