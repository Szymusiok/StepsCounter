package eu.tutorials.stepscounter

import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView


@Composable
fun StartScreen(
    videoUri: Uri,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val brandGreen = Color(0xFF4CAF50)

    PutBackground(context = context, videoUri = videoUri)

    // vertical gradient overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
    )

    // foreground content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Section: Icon, Title, Subtitle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Let’s Get Moving!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your steps and discover new trails",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Version label
            Text(
                text = "v0.1",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Login (Outlined) Button
            OutlinedButton(
                onClick = onNavigateToLogin,
                shape = RoundedCornerShape(50), // pill-shaped
                border = BorderStroke(2.dp, brandGreen),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = brandGreen
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(64.dp)
            ) {
                Text(
                    "Login",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 20.sp
                )
            }

            // Register Button
            Button(
                onClick = onNavigateToSignUp,
                shape = RoundedCornerShape(50), // pill-shaped
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(64.dp)
            ) {
                Text(
                    "Get Started",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 20.sp
                )
            }
        }
    }
}
