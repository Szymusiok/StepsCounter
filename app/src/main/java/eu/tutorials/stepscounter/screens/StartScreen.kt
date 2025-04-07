package eu.tutorials.stepscounter.screens

import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.stepscounter.KdamThmorPro
import eu.tutorials.stepscounter.R
import eu.tutorials.stepscounter.ui.theme.BORDOWY
import eu.tutorials.stepscounter.ui.theme.JASNY_KREMOWY


@Composable
fun StartScreen(
    videoUri: Uri,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // LOGO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 128.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
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
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LOGIN button
            Button(
                onClick = onNavigateToLogin,
                shape = RoundedCornerShape(50), // pill-shaped
                colors = ButtonDefaults.buttonColors(
                    containerColor = BORDOWY,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(
                    text = "LOGIN",
                    fontSize = 24.sp,
                    color = JASNY_KREMOWY,
                    fontFamily = KdamThmorPro
                    )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GET STARTED button
            Button(
                onClick = onNavigateToSignUp,
                shape = RoundedCornerShape(50), // pill-shaped
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = JASNY_KREMOWY
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(
                    text = "GET STARTED",
                    fontSize = 24.sp,
                    color = BORDOWY,
                    fontFamily = KdamThmorPro
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Version label
            Text(
                text = "v0.1",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
