package eu.tutorials.stepscounter

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

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