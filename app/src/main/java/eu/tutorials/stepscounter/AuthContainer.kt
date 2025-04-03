package eu.tutorials.stepscounter

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val KdamThmorPro = FontFamily(
    Font(R.font.kdam_thmor_pro, FontWeight.Normal)
)

val BORDOWY = Color(0xFF431511)
val JASNY_KREMOWY = Color(0xFFE1E1E1)

@Composable
fun AuthContainer(
    videoUri: Uri,
    modifier: Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
){
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        AuthVideoBackground(
            videoUri = videoUri)

        NavigationGraph(
            videoUri = videoUri,
            navController = navController,
            modifier = Modifier,
            authViewModel = authViewModel,
        )
    }
}