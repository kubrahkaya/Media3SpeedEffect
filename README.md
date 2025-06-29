Media3SpeedEffect is an Android application that demonstrates the power of Android's Media3 library for video manipulation. This app allows users to play videos with different playback speeds, preview the speed-adjusted videos, and export them as new video files. It showcases modern Android development practices including Jetpack Compose for UI, Hilt for dependency injection, and the Media3 library for video processing.

## Project Overview


The Media3SpeedEffect app provides a simple yet powerful interface for manipulating video playback speeds. Users can:


1. Play a sample video at normal speed
2. Adjust the playback speed to 1.0x, 1.5x, or 2.0x
3. Export the video with the selected speed as a new video file


The application demonstrates how to use Android's Media3 library to apply speed effects.
## Key Features


### Video Playback with Speed Control


The app uses ExoPlayer from the Media3 library to play videos with adjustable playback speeds. Users can select from predefined speed options (1.0x, 1.5x, and 2.0x) and see the changes applied in real-time.
### Video Export with Speed Effects


The app can export videos with the selected speed applied permanently. This is done using the Media3 Transformer API, which processes the video and creates a new file with the speed effect applied to both audio and video tracks.
### User-Friendly Interface with Jetpack Compose


The application features a clean, modern UI built with Jetpack Compose. The interface includes:
- A video player for viewing the content
- Speed control buttons for selecting the desired playback speed
- Preview and export buttons for testing and saving the speed-adjusted video

### VideoTransformerViewModel


The `VideoTransformerViewModel` is the core component that handles video transformation logic. It:


1. Manages state for tracking export and preview operations
2. Creates speed effects using Media3's SpeedProvider
3. Handles the video export process using Transformer
