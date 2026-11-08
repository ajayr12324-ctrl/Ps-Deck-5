package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PS5Cyan
import com.example.ui.theme.PS5Light
import com.example.ui.theme.PS5SurfaceDark
import com.example.ui.theme.PS5TextSecondary

import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PS5SettingsDialog(
    isOpen: Boolean,
    hapticsEnabled: Boolean,
    soundEffectsEnabled: Boolean,
    particlesEnabled: Boolean,
    showOnlyInstalled: Boolean,
    onToggleHaptics: () -> Unit,
    onToggleSoundEffects: () -> Unit,
    onToggleParticles: () -> Unit,
    onToggleShowOnlyInstalled: () -> Unit,
    onReplayBootAnimation: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PS5SurfaceDark,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("ps5_settings_sheet")
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
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = PS5Cyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "SETTINGS & PREFERENCES",
                        color = PS5Light,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PS5TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings list
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        title = "DualSense Haptic Feedback",
                        subtitle = "Tactile impulse vibration on carousel focus and button presses",
                        checked = hapticsEnabled,
                        onCheckedChange = { onToggleHaptics() },
                        testTag = "switch_haptics"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingToggleRow(
                        title = "Sound & Audio Effects",
                        subtitle = "PlayStation navigation audio and chime effects",
                        checked = soundEffectsEnabled,
                        onCheckedChange = { onToggleSoundEffects() },
                        testTag = "switch_sound_effects"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingToggleRow(
                        title = "Dynamic Ambient Particles",
                        subtitle = "Cosmic floating particle effects in dynamic background",
                        checked = particlesEnabled,
                        onCheckedChange = { onToggleParticles() },
                        testTag = "switch_ambient_particles"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingToggleRow(
                        title = "Show Only Installed Apps",
                        subtitle = "Hide featured PlayStation showcase demo game titles",
                        checked = showOnlyInstalled,
                        onCheckedChange = { onToggleShowOnlyInstalled() },
                        testTag = "switch_installed_only"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onReplayBootAnimation,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("replay_boot_animation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = PS5Cyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Replay PS5 Boot Animation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PS5 Launcher Edition 1.0 • Running on Android Jetpack Compose",
                color = PS5TextSecondary.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = PS5TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PS5Cyan,
                uncheckedThumbColor = PS5TextSecondary,
                uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
