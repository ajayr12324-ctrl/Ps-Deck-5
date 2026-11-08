package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PS5Cyan
import kotlinx.coroutines.delay

@Composable
fun PS5BootScreen(
    onBootFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation progress states
    val powerLightAlpha = remember { Animatable(0f) }
    val powerLightColorShift = remember { Animatable(0f) } // 0f: Amber, 1f: PS Blue
    val symbolsScale = remember { Animatable(0.4f) }
    val symbolsAlpha = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val promptAlpha = remember { Animatable(0f) }
    val exitScale = remember { Animatable(1f) }
    val exitAlpha = remember { Animatable(1f) }

    var isExiting by remember { mutableStateOf(false) }

    // Floating pulse for prompt
    val infiniteTransition = rememberInfiniteTransition(label = "boot_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val particlesPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particlesPhase"
    )

    fun triggerExit() {
        if (isExiting) return
        isExiting = true
        onBootFinished()
    }

    LaunchedEffect(Unit) {
        // Stage 1: Amber light bar pulses (PS5 console booting)
        powerLightAlpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        delay(300)

        // Stage 2: Light shifts from Amber to Electric PlayStation Blue
        powerLightColorShift.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))

        // Stage 3: PS Sacred symbols emerge
        symbolsAlpha.animateTo(1f, animationSpec = tween(600))
        symbolsScale.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))

        // Stage 4: "PlayStation" logo reveals
        logoAlpha.animateTo(1f, animationSpec = tween(500))

        // Stage 5: "Press PS button / Tap to start" prompt
        delay(300)
        promptAlpha.animateTo(1f, animationSpec = tween(400))

        // Auto proceed after 3.8 seconds if user hasn't tapped yet
        delay(2600)
        if (!isExiting) {
            triggerExit()
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(exitScale.value)
            .alpha(exitAlpha.value)
            .background(Color(0xFF030509))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { triggerExit() }
            )
            .testTag("ps5_boot_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Console Power Bar Glow (Amber -> PlayStation Electric Blue)
        val amberColor = Color(0xFFFF9100)
        val psBlueColor = Color(0xFF0070D1)
        val currentGlowColor = Color(
            red = amberColor.red * (1f - powerLightColorShift.value) + psBlueColor.red * powerLightColorShift.value,
            green = amberColor.green * (1f - powerLightColorShift.value) + psBlueColor.green * powerLightColorShift.value,
            blue = amberColor.blue * (1f - powerLightColorShift.value) + psBlueColor.blue * powerLightColorShift.value,
            alpha = powerLightAlpha.value
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // Center radial volumetric glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        currentGlowColor.copy(alpha = 0.35f * powerLightAlpha.value),
                        currentGlowColor.copy(alpha = 0.12f * powerLightAlpha.value),
                        Color.Transparent
                    ),
                    center = Offset(width / 2f, height / 2f),
                    radius = width * 0.75f
                )
            )

            // Volumetric curved sweep lines (signature PS5 light blades)
            val lightColor = if (powerLightColorShift.value > 0.5f) PS5Cyan.copy(alpha = 0.25f) else amberColor.copy(alpha = 0.25f)

            // Left curved beam
            val leftPath = Path().apply {
                moveTo(width * 0.1f, 0f)
                cubicTo(
                    width * 0.25f, height * 0.45f,
                    width * 0.35f, height * 0.65f,
                    width * 0.5f, height
                )
            }
            drawPath(
                path = leftPath,
                color = lightColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Right curved beam
            val rightPath = Path().apply {
                moveTo(width * 0.9f, 0f)
                cubicTo(
                    width * 0.75f, height * 0.45f,
                    width * 0.65f, height * 0.65f,
                    width * 0.5f, height
                )
            }
            drawPath(
                path = rightPath,
                color = lightColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Boot particles
            val particleCount = 18
            for (i in 0 until particleCount) {
                val seed = (i * 37) % 100 / 100f
                val y = (seed + particlesPhase * 0.3f) % 1f
                val x = ((i * 59) % 100) / 100f
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f * (1f - y)),
                    radius = 2.dp.toPx(),
                    center = Offset(x * width, y * height)
                )
            }
        }

        // Center Content: Symbols, PlayStation Logo, and Prompt
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sacred PlayStation Symbols: △ ○ ✕ □
            Row(
                modifier = Modifier
                    .scale(symbolsScale.value)
                    .alpha(symbolsAlpha.value)
                    .testTag("boot_ps_symbols"),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Triangle: Green
                SymbolCanvas(color = Color(0xFF00E676)) { size ->
                    val path = Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path, color = Color(0xFF00E676), style = Stroke(width = 3.dp.toPx()))
                }

                // Circle: Red
                SymbolCanvas(color = Color(0xFFFF3366)) { size ->
                    drawCircle(
                        color = Color(0xFFFF3366),
                        radius = size.minDimension / 2f - 2.dp.toPx(),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Cross: Blue
                SymbolCanvas(color = Color(0xFF2979FF)) { size ->
                    val stroke = 3.dp.toPx()
                    drawLine(Color(0xFF2979FF), Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
                    drawLine(Color(0xFF2979FF), Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
                }

                // Square: Pink
                SymbolCanvas(color = Color(0xFFE040FB)) { size ->
                    drawRect(
                        color = Color(0xFFE040FB),
                        topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // PlayStation Logo / Brand Title
            Column(
                modifier = Modifier.alpha(logoAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PlayStation® 5",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PLAY HAS NO LIMITS",
                    color = PS5Cyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(72.dp))

            // "Press the PS button to start" / Tap to continue
            Row(
                modifier = Modifier
                    .alpha(promptAlpha.value * pulseAlpha)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("boot_start_prompt"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = PS5Cyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Press PS button or tap to start",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun SymbolCanvas(
    color: Color,
    onDrawSymbol: androidx.compose.ui.graphics.drawscope.DrawScope.(androidx.compose.ui.geometry.Size) -> Unit
) {
    Canvas(
        modifier = Modifier.size(26.dp)
    ) {
        onDrawSymbol(size)
    }
}
