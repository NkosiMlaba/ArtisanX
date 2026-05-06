package com.example.artisanx.presentation.common

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Aurora-style background — three layered radial gradient blobs in the brand
 * colours, rendered with a heavy blur on Android 12+ for a soft, alive look.
 *
 * Place behind a [Scaffold] with `containerColor = Color.Transparent`.
 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        val canvasMod = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.fillMaxSize().blur(80.dp, BlurredEdgeTreatment.Rectangle)
        } else {
            // On older devices the radial gradients alone already look soft.
            Modifier.fillMaxSize()
        }
        Canvas(modifier = canvasMod) {
            val w = size.width
            val h = size.height

            // Indigo: large blob at upper-left
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.32f), Color.Transparent),
                    center = Offset(w * 0.10f, h * 0.05f),
                    radius = w * 0.85f
                )
            )

            // Gold: warm blob at upper-right, slightly lower
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(secondary.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(w * 0.95f, h * 0.22f),
                    radius = w * 0.70f
                )
            )

            // Teal: cool blob mid-right
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(tertiary.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(w * 1.05f, h * 0.62f),
                    radius = w * 0.65f
                )
            )

            // Indigo accent: subtle blob bottom-left to keep the page balanced
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(w * 0.05f, h * 0.95f),
                    radius = w * 0.75f
                )
            )
        }
    }
}
