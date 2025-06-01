package eu.tutorials.stepscounter.utils

import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun AuthVideoBackground(videoUri: Uri) {
    val context = LocalContext.current
    val currentUri by rememberUpdatedState(videoUri)

    // Build ExoPlayer once
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            playWhenReady = true
        }
    }

    // Load video URI when it changes
    LaunchedEffect(currentUri) {
        exoPlayer.setMediaItem(MediaItem.fromUri(currentUri))
        exoPlayer.prepare()
    }

    // Compose AndroidView
    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    // Release player on dispose
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }
}
