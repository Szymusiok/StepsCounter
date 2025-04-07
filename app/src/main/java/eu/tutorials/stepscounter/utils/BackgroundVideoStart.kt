package eu.tutorials.stepscounter.utils

import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

    fun Context.buildExoPlayer(uri: Uri): ExoPlayer =
        ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            playWhenReady = true
            prepare()
        }

    @OptIn(UnstableApi::class)
    fun Context.buildPlayerView(exoPlayer: ExoPlayer): PlayerView {
        return PlayerView(this).apply {
            player = exoPlayer
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
    }

    @Composable
    fun AuthVideoBackground(videoUri: Uri) {
        val context = LocalContext.current
        val exoPlayer = remember { context.buildExoPlayer(uri = videoUri) }
        AndroidView(
            factory = { context.buildPlayerView(exoPlayer) },
            modifier = Modifier.fillMaxSize()
        )
        DisposableEffect(exoPlayer) {
            onDispose { exoPlayer.release() }
        }
    }