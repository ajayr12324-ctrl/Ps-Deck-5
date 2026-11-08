package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.ui.theme.PS5Cyan
import com.example.ui.theme.PS5Light
import com.example.ui.theme.PS5SurfaceDark
import com.example.ui.theme.PS5TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PS5AddGameDialog(
    isOpen: Boolean,
    pickerApps: List<AppItem>,
    isLoadingPickerApps: Boolean,
    onDismiss: () -> Unit,
    onAddGame: (
        name: String,
        genre: String,
        publisher: String,
        badgeText: String,
        storageSize: String,
        accentColor: Color,
        description: String,
        selectedInstalledApp: AppItem?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0: From Device Apps, 1: Custom Game

    // Form state
    var selectedApp by remember { mutableStateOf<AppItem?>(null) }
    var gameName by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Action RPG") }
    var badgeText by remember { mutableStateOf("PS5") }
    var storageSize by remember { mutableStateOf("45.0 GB") }
    var selectedColor by remember { mutableStateOf(Color(0xFF0070D1)) }
    var appSearchQuery by remember { mutableStateOf("") }

    val presetColors = listOf(
        Color(0xFF0070D1), // PS Blue
        Color(0xFF00E5FF), // Radiant Cyan
        Color(0xFFE21B24), // Spider-Man Red
        Color(0xFF9146FF), // Neon Purple
        Color(0xFFFF9100), // Horizon Amber
        Color(0xFF00E676), // Emerald
        Color(0xFFFFD600)  // Gold
    )

    val genreOptions = listOf("Action RPG", "Platformer", "Shooter", "Open World", "Racing", "Sports", "Adventure")
    val badgeOptions = listOf("PS5", "PS4", "PRO", "VR2")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PS5SurfaceDark,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("ps5_add_game_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PS5Cyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ADD GAME TO PS5",
                        color = PS5Light,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_add_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PS5TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Tabs: From Device Apps vs Custom Game
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White.copy(alpha = 0.06f),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PS5Cyan,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("add_game_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Link Device App",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Create Custom Game",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Tab 0: Pick installed app and add as Game
                if (selectedApp == null) {
                    Text(
                        text = "Select any installed app to convert into a PlayStation game tile:",
                        color = PS5TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = appSearchQuery,
                        onValueChange = { appSearchQuery = it },
                        placeholder = { Text("Search installed apps...", color = PS5TextSecondary) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = PS5Cyan)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PS5Cyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color.White.copy(alpha = 0.04f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_installed_for_game")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoadingPickerApps) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PS5Cyan)
                        }
                    } else {
                        val filteredList = pickerApps.filter {
                            if (appSearchQuery.isBlank()) true
                            else it.name.lowercase().contains(appSearchQuery.trim().lowercase())
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredList, key = { it.id }) { item ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedApp = item
                                            gameName = item.name
                                            publisher = "Device App"
                                        }
                                        .testTag("pick_app_${item.packageName}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (item.iconBitmap != null) {
                                                Image(
                                                    bitmap = item.iconBitmap.asImageBitmap(),
                                                    contentDescription = item.name,
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(PS5Cyan.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.SportsEsports,
                                                        contentDescription = null,
                                                        tint = PS5Cyan
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = item.name,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = item.packageName,
                                                    color = PS5TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Select",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Selected app configuration form
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedApp?.iconBitmap != null) {
                                Image(
                                    bitmap = selectedApp!!.iconBitmap!!.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Selected: ${selectedApp?.name}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = selectedApp?.packageName ?: "",
                                    color = PS5Cyan,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = { selectedApp = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Change app",
                                tint = PS5TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GameFormFields(
                        gameName = gameName,
                        onNameChange = { gameName = it },
                        publisher = publisher,
                        onPublisherChange = { publisher = it },
                        genre = genre,
                        onGenreChange = { genre = it },
                        badgeText = badgeText,
                        onBadgeChange = { badgeText = it },
                        selectedColor = selectedColor,
                        onColorChange = { selectedColor = it },
                        storageSize = storageSize,
                        onStorageChange = { storageSize = it },
                        genreOptions = genreOptions,
                        badgeOptions = badgeOptions,
                        presetColors = presetColors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onAddGame(
                                gameName,
                                genre,
                                publisher,
                                badgeText,
                                storageSize,
                                selectedColor,
                                "Playable game launched directly from your PlayStation 5 Home Screen.",
                                selectedApp
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_add_linked_game")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Game to PS5 Home Screen", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            } else {
                // Tab 1: Create Custom Game from scratch
                GameFormFields(
                    gameName = gameName,
                    onNameChange = { gameName = it },
                    publisher = publisher,
                    onPublisherChange = { publisher = it },
                    genre = genre,
                    onGenreChange = { genre = it },
                    badgeText = badgeText,
                    onBadgeChange = { badgeText = it },
                    selectedColor = selectedColor,
                    onColorChange = { selectedColor = it },
                    storageSize = storageSize,
                    onStorageChange = { storageSize = it },
                    genreOptions = genreOptions,
                    badgeOptions = badgeOptions,
                    presetColors = presetColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onAddGame(
                            gameName.ifBlank { "Untitled PS5 Game" },
                            genre,
                            publisher.ifBlank { "PlayStation Studios" },
                            badgeText,
                            storageSize,
                            selectedColor,
                            "Custom PlayStation title added to your collection with full trophies and activities.",
                            null
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_add_custom_game")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create & Add Game", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun GameFormFields(
    gameName: String,
    onNameChange: (String) -> Unit,
    publisher: String,
    onPublisherChange: (String) -> Unit,
    genre: String,
    onGenreChange: (String) -> Unit,
    badgeText: String,
    onBadgeChange: (String) -> Unit,
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    storageSize: String,
    onStorageChange: (String) -> Unit,
    genreOptions: List<String>,
    badgeOptions: List<String>,
    presetColors: List<Color>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Game Title
        OutlinedTextField(
            value = gameName,
            onValueChange = onNameChange,
            label = { Text("Game Title", color = PS5TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PS5Cyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_game_title")
        )

        // Publisher / Studio
        OutlinedTextField(
            value = publisher,
            onValueChange = onPublisherChange,
            label = { Text("Publisher / Studio", color = PS5TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PS5Cyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_game_publisher")
        )

        // Genre Chips
        Column {
            Text(text = "Genre", color = PS5TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(genreOptions) { g ->
                    val isSelected = genre == g
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PS5Cyan else Color.White.copy(alpha = 0.1f))
                            .clickable { onGenreChange(g) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = g,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Badge Chips & Color selection Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Badge
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Edition Badge", color = PS5TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    badgeOptions.forEach { b ->
                        val isSelected = badgeText == b
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.1f))
                                .clickable { onBadgeChange(b) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = b,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Storage Size
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Storage Estimate", color = PS5TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = storageSize,
                    onValueChange = onStorageChange,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PS5Cyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Accent Color Selection
        Column {
            Text(text = "Theme Accent Glow", color = PS5TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                presetColors.forEach { c ->
                    val isSelected = selectedColor == c
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onColorChange(c) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
