package com.official.recipesnap.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun SplashScreen(onContinue: () -> Unit) {
    val darkGreen = Color(0xFF173D32)
    val creamWhite = Color(0xFFFAF8F5)
    val coralOrange = Color(0xFFE8734A)
    val mutedGreen = Color(0xFF2C564A)
    val darkContainer = Color(0xFF1F4A3E)

    // Animation States
    val bgOpacity = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.65f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotation = remember { Animatable(-8f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleTranslationY = remember { Animatable(12f) }
    val taglineAlpha = remember { Animatable(0f) }
    val ringsScale = remember { Animatable(0.95f) }
    val ringsAlpha = remember { Animatable(0f) }

    // Floating animation offset variables
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val float1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "f1"
    )
    val float2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(5500, easing = LinearEasing)), label = "f2"
    )
    val float3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "f3"
    )

    LaunchedEffect(Unit) {
        // Auto-navigate timer
        launch {
            delay(3800)
            onContinue()
        }

        // Timeline: 0.0 - 0.5s Background Rings + floating food fade in
        launch {
            bgOpacity.animateTo(1f, tween(500, easing = LinearEasing))
            ringsAlpha.animateTo(1f, tween(500, easing = LinearEasing))
        }

        // Timeline: 0.4 - 1.2s Logo appears
        launch {
            delay(400)
            launch {
                logoAlpha.animateTo(1f, tween(600, easing = EaseOut))
            }
            launch {
                logoScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }

        // Timeline: 1.0 - 1.7s Logo rotation
        launch {
            delay(1000)
            logoRotation.animateTo(8f, tween(300, easing = EaseInOutSine))
            logoRotation.animateTo(0f, tween(400, easing = EaseOutBack))
        }

        // Timeline: 1.4 - 2.0s Title sliding up
        launch {
            delay(1400)
            launch {
                titleAlpha.animateTo(1f, tween(600, easing = EaseOut))
            }
            launch {
                titleTranslationY.animateTo(0f, tween(600, easing = EaseOutCubic))
            }
        }

        // Tagline delay
        launch {
            delay(1600)
            taglineAlpha.animateTo(1f, tween(600, easing = LinearEasing))
        }

        // Timeline: 2.5 - 3.2s Rings subtle pulse (continuous)
        launch {
            delay(2500)
            ringsScale.animateTo(1.03f, tween(700, easing = EaseInOutSine))
            ringsScale.animateTo(1.0f, tween(700, easing = EaseInOutSine))
            // Loop slightly
            while (true) {
                ringsScale.animateTo(1.02f, tween(1500, easing = EaseInOutSine))
                ringsScale.animateTo(1.0f, tween(1500, easing = EaseInOutSine))
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onContinue()
            },
        color = darkGreen
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            
            // Concentric Rings
            Canvas(modifier = Modifier.fillMaxSize().alpha(ringsAlpha.value).scale(ringsScale.value)) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(color = mutedGreen, radius = size.width * 0.4f, center = center, style = Stroke(width = 2.dp.toPx()), alpha = 0.4f)
                drawCircle(color = mutedGreen, radius = size.width * 0.6f, center = center, style = Stroke(width = 2.dp.toPx()), alpha = 0.2f)
                drawCircle(color = mutedGreen, radius = size.width * 0.8f, center = center, style = Stroke(width = 2.dp.toPx()), alpha = 0.1f)
            }

            // Floating Food Elements (Emojis with reduced opacity)
            Box(modifier = Modifier.fillMaxSize().alpha(bgOpacity.value)) {
                FloatingEmoji("🍋", 42.sp, 0.15f, 0.2f, float1)
                FloatingEmoji("🍅", 36.sp, 0.8f, 0.25f, float2)
                FloatingEmoji("🥑", 48.sp, 0.15f, 0.7f, float3)
                FloatingEmoji("🫐", 32.sp, 0.85f, 0.65f, float1)
                FloatingEmoji("🌿", 44.sp, 0.75f, 0.1f, float2)
                FloatingEmoji("🥕", 36.sp, 0.2f, 0.85f, float3)
            }

            // Main Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo Container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(RoundedCornerShape(28.dp))
                        .background(darkContainer)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner Logo Graphic
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(mutedGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Restaurant,
                            contentDescription = "Logo",
                            tint = creamWhite,
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    rotationZ = logoRotation.value
                                }
                        )
                        // Small accent dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .background(coralOrange, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Recipe Snap",
                    color = creamWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .alpha(titleAlpha.value)
                        .graphicsLayer {
                            translationY = titleTranslationY.value
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tagline
                Text(
                    text = "SNAP · COOK · SHARE",
                    color = coralOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.alpha(taglineAlpha.value)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Page Indicator
                Row(
                    modifier = Modifier.alpha(taglineAlpha.value),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(coralOrange, CircleShape))
                    Box(modifier = Modifier.size(6.dp).background(mutedGreen, CircleShape))
                    Box(modifier = Modifier.size(6.dp).background(mutedGreen, CircleShape))
                }
            }

            // Bottom Text
            Text(
                text = "Tap anywhere to continue",
                color = mutedGreen.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .alpha(taglineAlpha.value) // Fade in with the tagline
            )
        }
    }
}

@Composable
fun BoxScope.FloatingEmoji(
    emoji: String,
    size: androidx.compose.ui.unit.TextUnit,
    xRatio: Float,
    yRatio: Float,
    floatPhase: Float
) {
    Text(
        text = emoji,
        fontSize = size,
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(
                x = (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp * xRatio).dp + (sin(floatPhase) * 15).dp,
                y = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * yRatio).dp + (sin(floatPhase * 1.5f) * 15).dp
            )
            .alpha(0.15f)
            .graphicsLayer {
                rotationZ = sin(floatPhase) * 15f
            }
    )
}
