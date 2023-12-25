package com.med.drawing.my_creation.presentation.my_creation_details

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bumptech.glide.Glide
import com.med.drawing.databinding.ActivityMyCreationDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class MyCreationDetailsActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null


    private lateinit var binding: ActivityMyCreationDetailsBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCreationDetailsBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        binding.back.setOnClickListener {
            onBackPressed()
        }

        val uri = intent.getStringExtra("uri")
        val isVideo = intent.getBooleanExtra("isVideo", false)

       uri.let {
           if (isVideo) {
               initializePlayer(it!!)
           } else {
               initImage(it!!)
           }
       }
    }

    private fun initImage(photoUri: String) {
        Glide.with(this)
            .load(photoUri)
            .thumbnail(0.25f)
            .into(binding.image)

        binding.image.visibility = View.VISIBLE
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(videoUri: String) {
        player = ExoPlayer.Builder(this).build().apply {

            val source = getProgressiveMediaSource(videoUri)

            setMediaSource(source)
            prepare()
            addListener(playerListener)
        }

        binding.playerView.player = player

        binding.playerView.visibility = View.VISIBLE
    }

    @OptIn(UnstableApi::class)
    private fun getProgressiveMediaSource(videoUri: String): MediaSource {
        return ProgressiveMediaSource.Factory(FileDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(Uri.parse(videoUri)))
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    override fun onResume() {
        super.onResume()
        play()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.apply {
            playWhenReady = false
            release()
        }
        player = null
    }

    private fun pause() {
        player?.playWhenReady = false
    }

    private fun play() {
        player?.playWhenReady = true
    }

    private fun restartPlayer() {
        player?.seekTo(0)
        player?.playWhenReady = true
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {

                Player.STATE_ENDED -> {
                    restartPlayer()
                }

                STATE_READY -> {
                    binding.playerView.player = player
                    play()
                }
            }
        }
    }
}















