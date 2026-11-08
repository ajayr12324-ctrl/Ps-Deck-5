package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppItem
import kotlin.random.Random

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float
)

@Composable
fun PS5DynamicBackground(
    selectedApp: AppItem?,
    particlesEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val particles = remember {
        List(28) {
            Particle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                radius = Random.nextFloat() * 2.2f + 1.2f,
                speed = Random.nextFloat() * 0.4f + 0.2f,
                alpha = Random.nextFloat() * 0.5f + 0.25f
            )
        }
    }

    val accentColor = selectedApp?.accentColor ?: Color(0xFF0070D1)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06080D))
            .testTag("ps5_dynamic_background")
    ) {
        // Base Ambient image
        Image(
            painter = painterResource(id = R.drawable.img_ps5_ambient),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.55f)
        )

        // Selected app dynamic backdrop (using icon bitmap scaled and blurred, or accent color glow)
        if (selectedApp?.iconBitmap != null) {
            Image(
                bitmap = selectedApp.iconBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.30f)
                    .blur(20.dp)
            )
        }

        // Accent radial glow behind the selected game
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.4f)
            val radius = size.maxDimension * 0.65f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.22f),
                        accentColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                )
            )
        }

        // Floating ambient PlayStation particles
        if (particlesEnabled) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("ambient_particles_canvas")
            ) {
                particles.forEach { p ->
                    val y = (p.initialY - (phase * p.speed)) % 1f
                    val currentY = if (y < 0f) y + 1f else y
                    val currentX = (p.initialX + (phase * 0.05f)) % 1f
                    drawCircle(
                        color = Color.White.copy(alpha = p.alpha),
                        radius = p.radius.dp.toPx(),
                        center = Offset(currentX * size.width, currentY * size.height)
                    )
                }
            }
        }

        // PS5 Dark Vignette effect: dark top gradient + deep dark bottom vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.70f),
                        0.25f to Color.Black.copy(alpha = 0.20f),
                        0.60f to Color.Black.copy(alpha = 0.55f),
                        1.0f to Color.Black.copy(alpha = 0.95f)
                    )
                )
        )
    }
}
