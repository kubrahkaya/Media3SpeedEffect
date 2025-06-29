package com.example.media3speedeffect

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.SpeedProvider
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@HiltViewModel
class VideoTransformerViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
) : ViewModel() {
    companion object {
        private const val TAG = "Speedy"
        const val SAMPLE_VIDEO_URL = 
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    }

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    @OptIn(UnstableApi::class)
    fun transformVideo(
        videoUrl: String,
        speed: Float
    ) {
        if (_isExporting.value) return

        _isExporting.value = true

        viewModelScope.launch {
            try {
                performTransformation(application, videoUrl, speed)
            } catch (e: Exception) {
                handleTransformationError(application, e)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun performTransformation(
        context: Context,
        videoUrl: String,
        speed: Float
    ) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        val effects = createSpeedEffects(speed)

        // Create an edited media item with the effects
        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            .setEffects(effects)
            .build()

        val outputFile = createOutputFile(context, speed)

        val transformer = createTransformer(context, outputFile)

        showExportStartedToast(context, speed)

        transformer.start(editedMediaItem, outputFile.path)
    }

    private fun createOutputFile(context: Context, speed: Float): File {
        return File(
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "exported_video_${speed}x.mp4"
        )
    }

    @OptIn(UnstableApi::class)
    private fun createTransformer(
        context: Context,
        outputFile: File
    ): Transformer {
        return Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    super.onCompleted(composition, exportResult)
                    // Update UI on main thread
                    viewModelScope.launch(Dispatchers.Main) {
                        _isExporting.value = false
                        Toast.makeText(
                            context,
                            "Video export completed to ${outputFile.path}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    super.onError(composition, exportResult, exportException)
                    // Update UI on main thread
                    viewModelScope.launch(Dispatchers.Main) {
                        _isExporting.value = false
                        Toast.makeText(
                            context,
                            "Error: ${exportException.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e(TAG, "Export error", exportException)
                    }
                }
            })
            .build()
    }

    private suspend fun showExportStartedToast(context: Context, speed: Float) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Exporting video with ${speed}x speed...",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun handleTransformationError(
        context: Context,
        e: Exception
    ) {
        withContext(Dispatchers.Main) {
            _isExporting.value = false
            Toast.makeText(
                context,
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            Log.e(TAG, "Error setting up transformation", e)
        }
    }

    @OptIn(UnstableApi::class)
    private fun createSpeedEffects(speed: Float): Effects {
        val speedProvider = object : SpeedProvider {
            override fun getSpeed(timeUs: Long): Float = speed
            override fun getNextSpeedChangeTimeUs(timeUs: Long): Long = C.TIME_UNSET
        }

        // Create the speed changing effect
        val speedEffect = Effects.createExperimentalSpeedChangingEffect(speedProvider)

        // Create effects with the speed effect
        return Effects(
            /* audioProcessors= */ listOf(speedEffect.first),
            /* videoEffects= */ listOf(speedEffect.second)
        )
    }
}
