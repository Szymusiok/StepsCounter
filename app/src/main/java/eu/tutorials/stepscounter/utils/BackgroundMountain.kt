package eu.tutorials.stepscounter.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.io.path.Path

@Composable
fun MountainHeaderFullScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // Mountains Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val backgroundPeaks = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, height)
                lineTo(0f, height * 0.15f)
                cubicTo(width * 0.25f, 0f, width * 0.75f, height * 0.3f, width, height * 0.15f)
                lineTo(width, height)
                close()
            }

            val midPeaks = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, height)
                lineTo(0f, height * 0.3f)
                cubicTo(width * 0.3f, height * 0.05f, width * 0.7f, height * 0.4f, width, height * 0.2f)
                lineTo(width, height)
                close()
            }

            val frontPeaks = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, height)
                lineTo(0f, height * 0.45f)
                cubicTo(width * 0.2f, height * 0.2f, width * 0.8f, height * 0.6f, width, height * 0.35f)
                lineTo(width, height)
                close()
            }

            drawPath(backgroundPeaks, Color(0xFFDCD6C5))
            drawPath(midPeaks, Color(0xFFBBB3A2))
            drawPath(frontPeaks, Color(0xFF998F7A))
        }

        // Stronger overlay fade from bottom only
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFFAF9F6)),
                        startY = 600f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
    }
}
