package com.github.yohannes.appstats.ui.activities.screens.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Credit:- https://semicolonspace.com/jetpack-compose-progress-indicator-2/
 */

@Composable
fun GradientProgressbar(
    progress: Float = 0F,
    indicatorHeight: Dp = 24.dp,
    backgroundIndicatorColor: Color = Color.LightGray.copy(alpha = 0.3f),
    indicatorPadding: Dp = 24.dp,
    gradientColors: List<Color> = listOf(
        Color(0xFF6ce0c4),
        Color(0xFF035FFF)
    ),
    animationDuration: Int = 1000,
    animationDelay: Int = 0
) {

    val animateNumber = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay
        )
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(indicatorHeight)
            .padding(start = indicatorPadding, end = indicatorPadding)
    ) {

        // Background indicator
        drawLine(
            color = backgroundIndicatorColor,
            cap = StrokeCap.Round,
            strokeWidth = size.height,
            start = Offset(x = 0f, y = 0f),
            end = Offset(x = size.width, y = 0f)
        )

        // Convert the downloaded percentage into progress (width of foreground indicator)
        val progress =
            (animateNumber.value / 100) * size.width // size.width returns the width of the canvas

        // Foreground indicator
        drawLine(
            brush = Brush.linearGradient(
                colors = gradientColors
            ),
            cap = StrokeCap.Round,
            strokeWidth = size.height,
            start = Offset(x = 0f, y = 0f),
            end = Offset(x = progress, y = 0f)
        )

    }
}
