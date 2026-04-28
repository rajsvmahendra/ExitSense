package com.rajsv.exitsense.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rajsv.exitsense.data.model.SettingsDataStore
import com.rajsv.exitsense.ui.theme.BackgroundPrimary
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import com.rajsv.exitsense.ui.theme.GradientEnd
import com.rajsv.exitsense.ui.theme.GradientMiddle
import com.rajsv.exitsense.ui.theme.GradientStart
import com.rajsv.exitsense.ui.theme.LightThemeColors
import com.rajsv.exitsense.ui.theme.TextPrimary
import com.rajsv.exitsense.ui.theme.TextSecondary
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// Particle data class
data class Particle(
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val size: Float,
    val colorRatio: Float
)

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Screen dimensions
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val centerX = screenWidthPx / 2
    val centerY = screenHeightPx / 2

    // Theme
    val isDarkMode by SettingsDataStore.getDarkMode(context).collectAsState(initial = true)
    val backgroundColor = if (isDarkMode) BackgroundPrimary else LightThemeColors.BackgroundPrimary
    val textPrimaryColor = if (isDarkMode) TextPrimary else LightThemeColors.TextPrimary
    val textSecondaryColor = if (isDarkMode) TextSecondary else LightThemeColors.TextSecondary

    // Phase: 1 = Shatter, 2 = Logo + Text
    var currentPhase by remember { mutableStateOf(1) }

    // Shatter animation
    val shatterProgress = remember { Animatable(0f) }
    val particlesAlpha = remember { Animatable(1f) }

    // Logo + Text animation
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val taglineOffset = remember { Animatable(20f) }

    // Generate particles
    val particles = remember {
        List(80) { index ->
            val angle = (index * 4.5) * (Math.PI / 180.0)
            val speed = Random.nextFloat() * 900f + 500f
            Particle(
                startX = centerX,
                startY = centerY,
                velocityX = (cos(angle) * speed).toFloat(),
                velocityY = (sin(angle) * speed).toFloat(),
                size = Random.nextFloat() * 12f + 6f,
                colorRatio = index.toFloat() / 80f
            )
        }
    }

    // Gradient color helper
    fun getGradientColor(ratio: Float): Color {
        return when {
            ratio < 0.5f -> {
                val localRatio = ratio * 2f
                Color(
                    red = GradientStart.red + (GradientMiddle.red - GradientStart.red) * localRatio,
                    green = GradientStart.green + (GradientMiddle.green - GradientStart.green) * localRatio,
                    blue = GradientStart.blue + (GradientMiddle.blue - GradientStart.blue) * localRatio,
                    alpha = 1f
                )
            }
            else -> {
                val localRatio = (ratio - 0.5f) * 2f
                Color(
                    red = GradientMiddle.red + (GradientEnd.red - GradientMiddle.red) * localRatio,
                    green = GradientMiddle.green + (GradientEnd.green - GradientMiddle.green) * localRatio,
                    blue = GradientMiddle.blue + (GradientEnd.blue - GradientMiddle.blue) * localRatio,
                    alpha = 1f
                )
            }
        }
    }

    // Phase 1: Shatter effect starts immediately
    LaunchedEffect(key1 = Unit) {
        // Small delay to let system splash finish
        delay(100)

        // Shatter animation
        val shatterJob = async {
            shatterProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            )
        }

        // Fade out particles
        delay(300)
        val fadeJob = async {
            particlesAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 350,
                    easing = LinearEasing
                )
            )
        }

        shatterJob.await()
        fadeJob.await()

        // Switch to logo phase
        currentPhase = 2
    }

    // Phase 2: Logo + Text appears
    LaunchedEffect(key1 = currentPhase) {
        if (currentPhase != 2) return@LaunchedEffect

        // Logo fades in
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )

        delay(100)

        // App name fades in
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 350,
                easing = FastOutSlowInEasing
            )
        )

        delay(80)

        // Tagline fades in and slides up
        launch {
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 350,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            taglineOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 350,
                    easing = FastOutSlowInEasing
                )
            )
        }

        // Wait then navigate
        delay(700)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Phase 1: Shatter Particles
        if (currentPhase == 1) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(particlesAlpha.value)
            ) {
                particles.forEach { particle ->
                    val progress = shatterProgress.value
                    val currentX = particle.startX + (particle.velocityX * progress)
                    val currentY = particle.startY + (particle.velocityY * progress)
                    val gravityOffset = progress * progress * 150f
                    val particleColor = getGradientColor(particle.colorRatio)

                    drawCircle(
                        color = particleColor,
                        radius = particle.size * (1f - progress * 0.4f),
                        center = Offset(currentX, currentY + gravityOffset),
                        alpha = 1f - (progress * 0.2f)
                    )
                }
            }
        }

        // Phase 2: Logo + Text
        if (currentPhase == 2) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Gradient Logo Box
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .alpha(logoAlpha.value)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    GradientStart,
                                    GradientMiddle,
                                    GradientEnd
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = "ExitSense Logo",
                        tint = TextPrimary,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App Name
                Text(
                    text = "ExitSense",
                    style = ExitSenseTypography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor,
                    modifier = Modifier.alpha(textAlpha.value)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tagline
                Text(
                    text = "Smart reminders before you leave",
                    style = ExitSenseTypography.bodyLarge,
                    color = textSecondaryColor,
                    modifier = Modifier
                        .alpha(taglineAlpha.value)
                        .offset { IntOffset(0, taglineOffset.value.roundToInt()) }
                )
            }
        }
    }
}