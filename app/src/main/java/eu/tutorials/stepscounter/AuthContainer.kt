package eu.tutorials.stepscounter

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import eu.tutorials.stepscounter.viewmodels.AuthViewModel
import eu.tutorials.stepscounter.utils.AuthVideoBackground
import eu.tutorials.stepscounter.navgraphs.StartNavigationGraph

val KdamThmorPro = FontFamily(
    Font(R.font.kdam_thmor_pro, FontWeight.Normal)
)

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

        StartNavigationGraph(
            navController = navController,
            modifier = Modifier,
            authViewModel = authViewModel,
        )
    }
}