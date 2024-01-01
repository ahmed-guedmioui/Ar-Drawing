package com.med.drawing.my_creation.presentation.my_creation_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bumptech.glide.Glide
import com.med.drawing.R
import com.med.drawing.databinding.ActivityMyCreationDetailsBinding
import com.med.drawing.my_creation.domian.model.Creation
import com.med.drawing.my_creation.presentation.my_creation_list.MyCreationListActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class MyCreationDetailsActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null

    private val myCreationDetailsViewModel: MyCreationDetailsViewModel by viewModels()

    private lateinit var binding: ActivityMyCreationDetailsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCreationDetailsBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        binding.back.setOnClickListener {
            Intent(this, MyCreationListActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

        val uri = intent.getStringExtra("uri")
        val isVideo = intent.getBooleanExtra("isVideo", false)

        uri.let { creationUri ->
            if (isVideo) {
                initializePlayer(creationUri!!)
            } else {
                initImage(creationUri!!)
            }

            binding.delete.setOnClickListener {
                showDeleteAlertDialog(creationUri)
            }
        }

        val creation = Creation(
            uri = Uri.parse(uri), isVideo = isVideo
        )

        binding.shareWhatsapp.setOnClickListener {
            shareCreationOnPlatform(creation, 0)
        }

        binding.shareFacebook.setOnClickListener {
            shareCreationOnPlatform(creation, 1)
        }

        binding.shareInstagram.setOnClickListener {
            shareCreationOnPlatform(creation, 2)
        }

        binding.shareMore.setOnClickListener {
            shareCreationOnPlatform(creation, 3)
        }
    }

    private fun showDeleteAlertDialog(creationUri: String) {

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(
                getString(R.string.are_you_sure_you_want_to_delete_this_creation)
            )
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                deleteCreation(creationUri)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create()

        alertDialog.show()
    }

    private fun deleteCreation(creationUri: String) {
        myCreationDetailsViewModel.onEvent(
            MyCreationDetailsUiEvent.DeleteCreation(creationUri)
        )
        Toast.makeText(
            this, getString(R.string.creation_deleted), Toast.LENGTH_SHORT
        ).show()

        Intent(this, MyCreationListActivity::class.java).also {
            startActivity(it)
            finish()
        }

    }

    override fun onBackPressed() {
        Intent(this, MyCreationListActivity::class.java).also {
            startActivity(it)
            finish()
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

        return ProgressiveMediaSource.Factory(DefaultDataSource.Factory(this))
            .createMediaSource(MediaItem.fromUri(videoUri))
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

                Player.STATE_BUFFERING -> {
                }

                Player.STATE_IDLE -> {
                }
            }
        }
    }

    private fun shareCreationOnPlatform(creation: Creation, platform: Int) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = if (creation.isVideo) "video/*" else "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, creation.uri)

        // Specify the package name based on the selected platform
        val packageName = when (platform) {
            0 -> "com.whatsapp"
            1 -> "com.facebook.katana"
            2 -> "com.instagram.android"
            else -> null // For global share or other custom logic
        }

        if (packageName != null) {
            shareIntent.`package` = packageName

            val chooserIntent = Intent.createChooser(shareIntent, "Share creation with:")
            startActivity(chooserIntent)
        } else {
            // Handle global share or other custom logic
            // You can implement additional sharing options here
            when (platform) {
                3 -> {
                    // Global share logic
                    val globalShareIntent = Intent(Intent.ACTION_SEND)
                    globalShareIntent.type = if (creation.isVideo) "video/*" else "image/*"
                    globalShareIntent.putExtra(Intent.EXTRA_STREAM, creation.uri)

                    val globalChooserIntent = Intent.createChooser(
                        globalShareIntent,
                        "Share creation with:"
                    )
                    startActivity(globalChooserIntent)
                }
                // Add more cases for other custom logic
            }
        }
    }

}















