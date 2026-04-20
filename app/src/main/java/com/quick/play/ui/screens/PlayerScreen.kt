package com.quick.play.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.quick.play.R
import com.quick.play.data.Channel

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    channel: Channel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    
    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setMaxVideoSize(1920, 1080)
                    .setPreferredVideoMimeType("video/avc")
            )
        }
    }
    
    val exoPlayer = remember {
        val dataSourceFactory = DefaultHttpDataSource.Factory().apply {
            if (channel.userAgent.isNotEmpty()) {
                setUserAgent(channel.userAgent)
            }
            if (channel.cookie.isNotEmpty()) {
                setDefaultRequestProperties(mapOf("Cookie" to channel.cookie))
            }
        }
        
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(trackSelector)
            .build().apply {
                if (channel.url.isNotEmpty()) {
                    val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(channel.url))
                    
                    val drmUuid = when (channel.licenseType.lowercase()) {
                        "clearkey" -> C.CLEARKEY_UUID
                        "com.widevine.alpha", "widevine" -> C.WIDEVINE_UUID
                        "playready" -> C.PLAYREADY_UUID
                        else -> null
                    }
                    
                    if (drmUuid != null && channel.licenseKey.isNotEmpty()) {
                        mediaItemBuilder.setDrmConfiguration(
                            MediaItem.DrmConfiguration.Builder(drmUuid)
                                .setLicenseUri(channel.licenseKey)
                                .build()
                        )
                    }
                    
                    setMediaItem(mediaItemBuilder.build())
                    prepare()
                    playWhenReady = true
                }
            }
    }

    DisposableEffect(Unit) {
        val window = activity?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        onDispose {
            exoPlayer.release()
            activity?.requestedOrientation = originalOrientation
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                val layout = LayoutInflater.from(ctx).inflate(R.layout.custom_player_layout, null) as FrameLayout
                val playerView = layout.findViewById<PlayerView>(R.id.player_view)
                val hqButton = layout.findViewById<android.widget.ImageButton>(R.id.hq_button)
                val audioButton = layout.findViewById<android.widget.ImageButton>(R.id.audio_button)
                
                playerView.player = exoPlayer
                
                hqButton.setOnClickListener {
                    val mappedTrackInfo = trackSelector.currentMappedTrackInfo
                    if (mappedTrackInfo != null) {
                        androidx.media3.ui.TrackSelectionDialogBuilder(
                            ctx,
                            "Select Video Quality",
                            exoPlayer,
                            C.TRACK_TYPE_VIDEO
                        ).setTheme(android.R.style.Theme_DeviceDefault_Dialog).build().show()
                    }
                }

                audioButton.setOnClickListener {
                    val mappedTrackInfo = trackSelector.currentMappedTrackInfo
                    if (mappedTrackInfo != null) {
                        var hasAudioTracks = false
                        for (i in 0 until mappedTrackInfo.rendererCount) {
                            if (mappedTrackInfo.getRendererType(i) == C.TRACK_TYPE_AUDIO) {
                                val trackGroups = mappedTrackInfo.getTrackGroups(i)
                                if (trackGroups.length > 0) {
                                    hasAudioTracks = true
                                    break
                                }
                            }
                        }
                        
                        if (hasAudioTracks) {
                            val trackSelectionDialogBuilder = androidx.media3.ui.TrackSelectionDialogBuilder(
                                ctx, "Select Audio Track", exoPlayer, C.TRACK_TYPE_AUDIO
                            )
                            trackSelectionDialogBuilder.setTheme(android.R.style.Theme_DeviceDefault_Dialog)
                            trackSelectionDialogBuilder.setShowDisableOption(false)
                            trackSelectionDialogBuilder.build().show()
                        } else {
                            android.widget.Toast.makeText(ctx, "No alternative audio tracks found", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
                layout.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                layout
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
