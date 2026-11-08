package com.example.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.model.ActivityCard
import com.example.model.AppItem
import com.example.model.LauncherTab
import com.example.model.TrophyProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val prefs: SharedPreferences = context.getSharedPreferences("ps5_custom_games_pref", Context.MODE_PRIVATE)

    suspend fun getInstalledApps(includeShowcase: Boolean = true): List<AppItem> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(launcherIntent, 0)

        val realGames = mutableListOf<AppItem>()
        val mediaApps = mutableListOf<AppItem>()

        resolveInfos.forEach { resolveInfo ->
            try {
                val pkg = resolveInfo.activityInfo.packageName
                if (pkg == context.packageName) return@forEach

                val name = resolveInfo.loadLabel(packageManager).toString()
                val isMedia = isMediaApp(pkg, name)
                val isGame = isGameApp(resolveInfo.activityInfo.applicationInfo, name)

                // Only take if it is an actual game or a media streaming app
                if (isGame || isMedia) {
                    val iconDrawable = resolveInfo.loadIcon(packageManager)
                    val iconBitmap = drawableToBitmap(iconDrawable)
                    val tab = if (isMedia) LauncherTab.MEDIA else LauncherTab.GAMES
                    val badge = if (isGame) "PS5" else "MEDIA"

                    val pseudoProgress = (name.hashCode().coerceAtLeast(0) % 85) + 15
                    val trophy = if (isGame) {
                        TrophyProgress(
                            platinum = if (pseudoProgress > 70) 1 else 0,
                            gold = pseudoProgress / 15,
                            silver = pseudoProgress / 8,
                            bronze = pseudoProgress / 4,
                            totalEarned = pseudoProgress / 2,
                            completionPercent = pseudoProgress
                        )
                    } else {
                        TrophyProgress(0, 0, 0, 0, 0, 0)
                    }

                    val activities = listOf(
                        ActivityCard(
                            id = "act_1_${pkg}",
                            title = if (isMedia) "Continue Watching" else "Continue Story",
                            subtitle = if (isMedia) "Resume your favorite playlist" else "Main Objective - Next Mission",
                            progressPercent = pseudoProgress,
                            tag = "RESUME",
                            isResumeAction = true
                        ),
                        ActivityCard(
                            id = "act_2_${pkg}",
                            title = if (isMedia) "Trending Now" else "Side Challenge",
                            subtitle = if (isMedia) "Discover top picks this week" else "Mastery trial in arena",
                            progressPercent = (pseudoProgress / 2),
                            tag = "ACTIVITY"
                        )
                    )

                    val appItem = AppItem(
                        id = pkg,
                        name = name,
                        packageName = pkg,
                        iconBitmap = iconBitmap,
                        isRealApp = true,
                        tab = tab,
                        badgeText = badge,
                        genre = if (isMedia) "Media & Streaming" else "Action / Adventure",
                        publisher = "Installed Application",
                        trophyProgress = trophy,
                        activityCards = activities,
                        storageSize = "${(name.hashCode().coerceAtLeast(1) % 40) / 10.0 + 1.2} GB",
                        playTime = "${(name.hashCode().coerceAtLeast(0) % 65) + 5} hours",
                        accentColor = Color(
                            red = 0.1f + ((name.hashCode() and 0xFF) / 400f),
                            green = 0.4f + (((name.hashCode() shr 8) and 0xFF) / 500f),
                            blue = 0.8f + (((name.hashCode() shr 16) and 0xFF) / 1200f),
                            alpha = 1.0f
                        ),
                        description = "Installed device game available for immediate launch on your PlayStation 5 interface."
                    )

                    if (isMedia) {
                        mediaApps.add(appItem)
                    } else {
                        realGames.add(appItem)
                    }
                }
            } catch (_: Exception) {
            }
        }

        // Custom games added by user
        val customGames = loadCustomGames()

        // Showcase games & media
        val showcaseGames = if (includeShowcase) getShowcaseGames() else emptyList()
        val showcaseMedia = if (includeShowcase) getShowcaseMedia() else emptyList()

        val allGamesList = (customGames + showcaseGames + realGames).distinctBy { it.id }
        val allMediaList = (mediaApps + showcaseMedia).distinctBy { it.id }

        allGamesList + allMediaList
    }

    suspend fun getAllInstalledAppsForPicker(): List<AppItem> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(launcherIntent, 0)
        resolveInfos.mapNotNull { resolveInfo ->
            try {
                val pkg = resolveInfo.activityInfo.packageName
                if (pkg == context.packageName) return@mapNotNull null
                val name = resolveInfo.loadLabel(packageManager).toString()
                val iconDrawable = resolveInfo.loadIcon(packageManager)
                val iconBitmap = drawableToBitmap(iconDrawable)

                AppItem(
                    id = "picker_$pkg",
                    name = name,
                    packageName = pkg,
                    iconBitmap = iconBitmap,
                    isRealApp = true,
                    genre = "App / Game",
                    publisher = "Installed Package"
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.name.lowercase() }
    }

    fun saveCustomGame(game: AppItem) {
        val existing = loadCustomGames().toMutableList()
        existing.removeAll { it.id == game.id }
        existing.add(0, game.copy(isCustomGame = true))
        persistCustomGames(existing)
    }

    fun deleteCustomGame(gameId: String) {
        val existing = loadCustomGames().toMutableList()
        existing.removeAll { it.id == gameId }
        persistCustomGames(existing)
    }

    private fun persistCustomGames(games: List<AppItem>) {
        val jsonArray = JSONArray()
        games.forEach { g ->
            val obj = JSONObject().apply {
                put("id", g.id)
                put("name", g.name)
                put("packageName", g.packageName)
                put("badgeText", g.badgeText)
                put("genre", g.genre)
                put("publisher", g.publisher)
                put("storageSize", g.storageSize)
                put("playTime", g.playTime)
                put("accentColor", g.accentColor.toArgb())
                put("description", g.description)
                put("isCustomGame", true)
                put("platinum", g.trophyProgress.platinum)
                put("gold", g.trophyProgress.gold)
                put("silver", g.trophyProgress.silver)
                put("bronze", g.trophyProgress.bronze)
                put("completionPercent", g.trophyProgress.completionPercent)
                // Encode icon bitmap if available
                if (g.iconBitmap != null) {
                    val stream = ByteArrayOutputStream()
                    g.iconBitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    put("iconBase64", base64)
                }
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("custom_games_json", jsonArray.toString()).apply()
    }

    private fun loadCustomGames(): List<AppItem> {
        val raw = prefs.getString("custom_games_json", null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<AppItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val iconBase64 = obj.optString("iconBase64", "")
                val iconBitmap = if (iconBase64.isNotEmpty()) {
                    try {
                        val bytes = Base64.decode(iconBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (_: Exception) {
                        null
                    }
                } else null

                val completion = obj.optInt("completionPercent", 45)
                val trophy = TrophyProgress(
                    platinum = obj.optInt("platinum", 0),
                    gold = obj.optInt("gold", 2),
                    silver = obj.optInt("silver", 5),
                    bronze = obj.optInt("bronze", 12),
                    totalEarned = obj.optInt("platinum", 0) + obj.optInt("gold", 2) + obj.optInt("silver", 5) + obj.optInt("bronze", 12),
                    completionPercent = completion
                )

                val name = obj.getString("name")
                val pkg = obj.optString("packageName", "")
                val id = obj.getString("id")

                val activities = listOf(
                    ActivityCard(
                        id = "act_custom_1_$id",
                        title = "Resume Campaign",
                        subtitle = "Jump straight back into your latest checkpoint",
                        progressPercent = completion,
                        tag = "RESUME",
                        isResumeAction = true
                    ),
                    ActivityCard(
                        id = "act_custom_2_$id",
                        title = "Custom Challenges",
                        subtitle = "Community trials and achievements",
                        progressPercent = (completion / 2),
                        tag = "CHALLENGE"
                    )
                )

                val colorInt = obj.optInt("accentColor", 0xFF0070D1.toInt())

                list.add(
                    AppItem(
                        id = id,
                        name = name,
                        packageName = pkg,
                        iconBitmap = iconBitmap,
                        isRealApp = pkg.isNotEmpty(),
                        tab = LauncherTab.GAMES,
                        badgeText = obj.optString("badgeText", "PS5"),
                        genre = obj.optString("genre", "Custom Game"),
                        publisher = obj.optString("publisher", "Custom Library"),
                        trophyProgress = trophy,
                        activityCards = activities,
                        storageSize = obj.optString("storageSize", "45.0 GB"),
                        playTime = obj.optString("playTime", "15 hours"),
                        accentColor = Color(colorInt),
                        description = obj.optString("description", "Custom added game title in your PlayStation 5 library."),
                        isCustomGame = true
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun launchApp(packageName: String): Boolean {
        if (packageName.isBlank()) {
            Toast.makeText(context, "Ready to play on PS5 Console!", Toast.LENGTH_SHORT).show()
            return true
        }
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(playStoreIntent)
                true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open $packageName", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun isMediaApp(pkg: String, name: String): Boolean {
        val lowerPkg = pkg.lowercase()
        val lowerName = name.lowercase()
        val mediaKeywords = listOf("youtube", "netflix", "spotify", "twitch", "prime", "video", "music", "disney", "media", "player", "tv", "gallery", "camera", "sound")
        return mediaKeywords.any { lowerPkg.contains(it) || lowerName.contains(it) }
    }

    private fun isGameApp(appInfo: ApplicationInfo, name: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME) return true
        }
        val lower = name.lowercase()
        return lower.contains("game") || lower.contains("play") || lower.contains("craft") || lower.contains("racer") || lower.contains("roblox") || lower.contains("minecraft")
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getShowcaseGames(): List<AppItem> {
        return listOf(
            AppItem(
                id = "com.playstation.spiderman2",
                name = "Marvel's Spider-Man 2",
                packageName = "com.playstation.spiderman2",
                isRealApp = false,
                tab = LauncherTab.GAMES,
                badgeText = "PS5",
                genre = "Action Adventure",
                publisher = "PlayStation Studios / Insomniac Games",
                trophyProgress = TrophyProgress(
                    platinum = 1,
                    gold = 6,
                    silver = 14,
                    bronze = 28,
                    totalEarned = 49,
                    completionPercent = 92
                ),
                activityCards = listOf(
                    ActivityCard(
                        id = "act_sm2_1",
                        title = "Resume Story: Trouble with Harry",
                        subtitle = "Confront Kraven's hunters across Queens",
                        progressPercent = 92,
                        tag = "MAIN STORY",
                        isResumeAction = true
                    ),
                    ActivityCard(
                        id = "act_sm2_2",
                        title = "Symbiote Nest Cleared",
                        subtitle = "Defeat the infected horde in Financial District",
                        progressPercent = 80,
                        tag = "SIDE ACTIVITY"
                    ),
                    ActivityCard(
                        id = "act_sm2_3",
                        title = "Photo Ops",
                        subtitle = "16 / 23 Iconic New York landmarks photographed",
                        progressPercent = 70,
                        tag = "COLLECTIBLES"
                    )
                ),
                storageSize = "98.4 GB",
                playTime = "46 hours",
                accentColor = Color(0xFFE21B24),
                description = "Peter Parker and Miles Morales return in Marvel's Spider-Man 2. Swing, jump, and utilize the new Web Wings to travel across Marvel's New York."
            ),
            AppItem(
                id = "com.playstation.astrobot",
                name = "Astro Bot",
                packageName = "com.playstation.astrobot",
                isRealApp = false,
                tab = LauncherTab.GAMES,
                badgeText = "PS5",
                genre = "Platformer",
                publisher = "PlayStation Studios / Team Asobi",
                trophyProgress = TrophyProgress(
                    platinum = 1,
                    gold = 7,
                    silver = 16,
                    bronze = 24,
                    totalEarned = 48,
                    completionPercent = 100
                ),
                activityCards = listOf(
                    ActivityCard(
                        id = "act_ab_1",
                        title = "Rescue VIP Bots",
                        subtitle = "300 / 300 Bots rescued across 5 galaxies",
                        progressPercent = 100,
                        tag = "COMPLETED",
                        isResumeAction = true
                    ),
                    ActivityCard(
                        id = "act_ab_2",
                        title = "Grand Master Challenge",
                        subtitle = "Complete the ultimate speedrun gauntlet",
                        progressPercent = 85,
                        tag = "CHALLENGE"
                    )
                ),
                storageSize = "66.2 GB",
                playTime = "28 hours",
                accentColor = Color(0xFF0091FF),
                description = "Join Astro in a brand-new supersized space adventure. Explore over 50 diverse planets packed with fun, secrets, and VIP PlayStation bots."
            ),
            AppItem(
                id = "com.playstation.gowragnarok",
                name = "God of War Ragnarök",
                packageName = "com.playstation.gowragnarok",
                isRealApp = false,
                tab = LauncherTab.GAMES,
                badgeText = "PS5",
                genre = "Action RPG",
                publisher = "PlayStation Studios / Santa Monica Studio",
                trophyProgress = TrophyProgress(
                    platinum = 1,
                    gold = 4,
                    silver = 15,
                    bronze = 22,
                    totalEarned = 42,
                    completionPercent = 88
                ),
                activityCards = listOf(
                    ActivityCard(
                        id = "act_gow_1",
                        title = "Valhalla: Overcome Death",
                        subtitle = "Master the rogue-lite combat trial in Asgard",
                        progressPercent = 75,
                        tag = "VALHALLA DLC",
                        isResumeAction = true
                    ),
                    ActivityCard(
                        id = "act_gow_2",
                        title = "Favor: Scent of Survival",
                        subtitle = "Track down the creature in the Vanaheim Crater",
                        progressPercent = 60,
                        tag = "FAVOR"
                    )
                ),
                storageSize = "118.5 GB",
                playTime = "62 hours",
                accentColor = Color(0xFF0D639C),
                description = "Kratos and Atreus embark on an epic quest for answers as the Asgardian forces prepare for a prophesied battle that will end the world."
            ),
            AppItem(
                id = "com.playstation.horizonfw",
                name = "Horizon Forbidden West",
                packageName = "com.playstation.horizonfw",
                isRealApp = false,
                tab = LauncherTab.GAMES,
                badgeText = "PS5",
                genre = "Open World RPG",
                publisher = "PlayStation Studios / Guerrilla",
                trophyProgress = TrophyProgress(
                    platinum = 0,
                    gold = 3,
                    silver = 12,
                    bronze = 35,
                    totalEarned = 50,
                    completionPercent = 76
                ),
                activityCards = listOf(
                    ActivityCard(
                        id = "act_hfw_1",
                        title = "Burning Shores: Fleet's End",
                        subtitle = "Fly your Sunwing toward the volcanic archipelago",
                        progressPercent = 76,
                        tag = "BURNING SHORES",
                        isResumeAction = true
                    ),
                    ActivityCard(
                        id = "act_hfw_2",
                        title = "Cauldron GEMINI",
                        subtitle = "Override core to unlock heavyweight machine mounts",
                        progressPercent = 100,
                        tag = "CAULDRON"
                    )
                ),
                storageSize = "104.1 GB",
                playTime = "74 hours",
                accentColor = Color(0xFFFF6D00),
                description = "Join Aloy as she braves the Forbidden West, a majestic but dangerous frontier that conceals mysterious new threats."
            ),
            AppItem(
                id = "com.playstation.gt7",
                name = "Gran Turismo 7",
                packageName = "com.playstation.gt7",
                isRealApp = false,
                tab = LauncherTab.GAMES,
                badgeText = "PS5",
                genre = "Racing Simulation",
                publisher = "PlayStation Studios / Polyphony Digital",
                trophyProgress = TrophyProgress(
                    platinum = 0,
                    gold = 2,
                    silver = 10,
                    bronze = 38,
                    totalEarned = 50,
                    completionPercent = 64
                ),
                activityCards = listOf(
                    ActivityCard(
                        id = "act_gt7_1",
                        title = "GT Café Menu Book 39",
                        subtitle = "World GT Series Championship final race",
                        progressPercent = 64,
                        tag = "MENU BOOK",
                        isResumeAction = true
                    ),
                    ActivityCard(
                        id = "act_gt7_2",
                        title = "Super License S-10",
                        subtitle = "Spa-Francorchamps wet lap time trial",
                        progressPercent = 95,
                        tag = "LICENSE TEST"
                    )
                ),
                storageSize = "112.0 GB",
                playTime = "88 hours",
                accentColor = Color(0xFF003791),
                description = "The Real Driving Simulator brings together the best features of the legendary franchise with over 400 cars and ultra-realistic weather."
            )
        )
    }

    private fun getShowcaseMedia(): List<AppItem> {
        return listOf(
            AppItem(
                id = "com.google.android.youtube",
                name = "YouTube",
                packageName = "com.google.android.youtube",
                isRealApp = false,
                tab = LauncherTab.MEDIA,
                badgeText = "MEDIA",
                genre = "Video Streaming",
                publisher = "Google LLC",
                storageSize = "150 MB",
                playTime = "120+ hours",
                accentColor = Color(0xFFFF0000),
                description = "Watch 4K HDR gaming walkthroughs, official PlayStation State of Play broadcasts, and your favorite creators."
            ),
            AppItem(
                id = "com.spotify.music",
                name = "Spotify",
                packageName = "com.spotify.music",
                isRealApp = false,
                tab = LauncherTab.MEDIA,
                badgeText = "MEDIA",
                genre = "Music & Podcasts",
                publisher = "Spotify AB",
                storageSize = "98 MB",
                playTime = "350+ hours",
                accentColor = Color(0xFF1DB954),
                description = "Listen to your game soundtracks and playlists in the background while you play."
            ),
            AppItem(
                id = "tv.twitch.android.app",
                name = "Twitch",
                packageName = "tv.twitch.android.app",
                isRealApp = false,
                tab = LauncherTab.MEDIA,
                badgeText = "MEDIA",
                genre = "Live Streaming",
                publisher = "Twitch Interactive, Inc.",
                storageSize = "120 MB",
                playTime = "45 hours",
                accentColor = Color(0xFF9146FF),
                description = "Watch top esports matches and live streams from PlayStation gamers worldwide."
            )
        )
    }
}
