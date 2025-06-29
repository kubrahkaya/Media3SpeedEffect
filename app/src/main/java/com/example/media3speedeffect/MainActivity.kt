package com.example.media3speedeffect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoPlayerScreen()
                }
            }
        }
    }

}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen() {
    val context = LocalContext.current
    val viewModel: VideoTransformerViewModel = hiltViewModel()

    val isExporting by viewModel.isExporting.collectAsState()

    var currentSpeed by remember { mutableFloatStateOf(1.0f) }

    val videoUrl = VideoTransformerViewModel.SAMPLE_VIDEO_URL

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(key1 = exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Column {
        VideoPlayerView(exoPlayer)

        SpeedControlButtons(
            currentSpeed = currentSpeed,
            onSpeedChanged = { speed ->
                currentSpeed = speed
                exoPlayer.playbackParameters = PlaybackParameters(speed)
            }
        )

        ExportButton(
            currentSpeed = currentSpeed,
            isExporting = isExporting,
            onExportClicked = {
                viewModel.transformVideo(
                    videoUrl = videoUrl,
                    speed = currentSpeed
                )
            }
        )
    }
}


@Composable
fun VideoPlayerView(exoPlayer: ExoPlayer) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

@Composable
fun SpeedControlButtons(
    currentSpeed: Float,
    onSpeedChanged: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SpeedButton(
            speed = 1.0f,
            isSelected = currentSpeed == 1.0f,
            onClick = { onSpeedChanged(1.0f) }
        )

        SpeedButton(
            speed = 1.5f,
            isSelected = currentSpeed == 1.5f,
            onClick = { onSpeedChanged(1.5f) }
        )

        SpeedButton(
            speed = 2.0f,
            isSelected = currentSpeed == 2.0f,
            onClick = { onSpeedChanged(2.0f) }
        )
    }
}

@Composable
fun SpeedButton(
    speed: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondary
        )
    ) {
        Text("${speed}x")
    }
}

@Composable
fun ExportButton(
    currentSpeed: Float,
    isExporting: Boolean,
    onExportClicked: () -> Unit
) {
    Button(
        onClick = onExportClicked,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        enabled = !isExporting
    ) {
        Text(
            if (isExporting) 
                "Exporting..." 
            else 
                "Export Video with ${currentSpeed}x Speed"
        )
    }
}
