package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherTab
import com.example.ui.theme.PS5Cyan
import com.example.ui.theme.PS5Light
import com.example.ui.theme.PS5TextSecondary

import androidx.compose.material.icons.filled.Add

@Composable
fun PS5TopBar(
    selectedTab: LauncherTab,
    currentTimeString: String,
    batteryPercent: Int,
    onTabSelected: (LauncherTab) -> Unit,
    onAddGameClick: () -> Unit,
    onSearchClick: () -> Unit,
    onControllerClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("ps5_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Tabs: Games and Media
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(
                title = "Games",
                isSelected = selectedTab == LauncherTab.GAMES,
                testTag = "tab_games",
                onClick = { onTabSelected(LauncherTab.GAMES) }
            )

            Spacer(modifier = Modifier.width(22.dp))

            TabItem(
                title = "Media",
                isSelected = selectedTab == LauncherTab.MEDIA,
                testTag = "tab_media",
                onClick = { onTabSelected(LauncherTab.MEDIA) }
            )
        }

        // Right Status items: Add Game (+), Search, Controller pill, Clock, Settings, Profile Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add Custom Game (+) Button
            IconButton(
                onClick = onAddGameClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(40.dp)
                    .testTag("top_add_game_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(PS5Cyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom Game",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Search Icon
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(40.dp)
                    .testTag("top_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Games and Apps",
                    tint = PS5Light,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Controller battery pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(onClick = onControllerClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("controller_status_pill"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "DualSense Controller",
                    tint = PS5Cyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$batteryPercent%",
                    color = PS5Light,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Current Time
            Text(
                text = currentTimeString,
                color = PS5Light,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .testTag("top_clock_text")
            )

            // Settings Gear Icon
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(40.dp)
                    .testTag("top_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = PS5TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // PS Profile Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF003791))
                    .clickable(onClick = onControllerClick)
                    .testTag("profile_avatar_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PS",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                // Online Indicator Green Dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676))
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) PS5Light else PS5TextSecondary,
        label = "tabTextColor"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = if (isSelected) 22.sp else 20.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Active glowing indicator pill
        Box(
            modifier = Modifier
                .width(if (isSelected) 18.dp else 0.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isSelected) PS5Cyan else Color.Transparent)
        )
    }
}
