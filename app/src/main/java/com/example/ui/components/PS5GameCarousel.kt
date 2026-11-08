package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.ui.theme.PS5FocusBorder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon

@Composable
fun PS5GameCarousel(
    apps: List<AppItem>,
    selectedIndex: Int,
    onSelectApp: (Int) -> Unit,
    onAddGameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Keep active selected item visible/centered in the carousel
    LaunchedEffect(selectedIndex) {
        if (selectedIndex in apps.indices) {
            val targetScroll = (selectedIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    Box(
        modifier = modifier
            .height(132.dp)
            .testTag("ps5_game_carousel")
    ) {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = apps,
                key = { _, item -> item.id }
            ) { index, item ->
                val isSelected = selectedIndex == index

                val cardWidth by animateDpAsState(
                    targetValue = if (isSelected) 92.dp else 74.dp,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                    label = "cardWidth"
                )

                val cardHeight by animateDpAsState(
                    targetValue = if (isSelected) 116.dp else 94.dp,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                    label = "cardHeight"
                )

                val cornerRadius = 16.dp
                val cardShape = RoundedCornerShape(cornerRadius)

                Box(
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight)
                        .shadow(
                            elevation = if (isSelected) 12.dp else 2.dp,
                            shape = cardShape,
                            ambientColor = if (isSelected) Color.White.copy(alpha = 0.4f) else Color.Transparent,
                            spotColor = if (isSelected) item.accentColor else Color.Black
                        )
                        .clip(cardShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    item.accentColor.copy(alpha = 0.45f),
                                    Color(0xFF141A29)
                                )
                            )
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    BorderStroke(2.5.dp, PS5FocusBorder),
                                    shape = cardShape
                                )
                            } else {
                                Modifier.border(
                                    BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                    shape = cardShape
                                )
                            }
                        )
                        .clickable { onSelectApp(index) }
                        .testTag("carousel_item_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.iconBitmap != null) {
                        Image(
                            bitmap = item.iconBitmap.asImageBitmap(),
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Sleek procedural fallback card for showcase games
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            item.accentColor,
                                            item.accentColor.copy(alpha = 0.6f),
                                            Color(0xFF0A0F1D)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                color = Color.White,
                                fontSize = if (isSelected) 24.sp else 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Top/Bottom Badge (PS5 / PS4 / MEDIA)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.badgeText,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Dedicated "+ Add Game" Card
            item {
                val cardShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .width(74.dp)
                        .height(94.dp)
                        .clip(cardShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(
                            BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f)),
                            shape = cardShape
                        )
                        .clickable(onClick = onAddGameClick)
                        .testTag("carousel_add_game_button"),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Game",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add Game",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
