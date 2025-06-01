package eu.tutorials.stepscounter.screens

import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.stepscounter.KdamThmorPro
import eu.tutorials.stepscounter.R
import eu.tutorials.stepscounter.ui.theme.BORDOWY
import eu.tutorials.stepscounter.ui.theme.JASNY_KREMOWY

@Composable
fun StartScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.auth_video}") // your video file

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding() // handle notch, status bar
    ) {
        // 🎥 Background Video
        // AuthVideoBackground(videoUri = videoUri)

        // 🌓 Gradient Overlay (faded, subtle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 600f
                    )
                )
        )

        // 🧱 Foreground UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Image",
                    modifier = Modifier.size(460.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onNavigateToLogin,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = BORDOWY),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("LOGIN", fontSize = 18.sp, color = JASNY_KREMOWY, fontFamily = KdamThmorPro)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onNavigateToSignUp,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("GET STARTED", fontSize = 18.sp, color = BORDOWY, fontFamily = KdamThmorPro)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("v0.1", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
