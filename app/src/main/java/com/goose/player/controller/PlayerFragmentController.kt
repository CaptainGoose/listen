package com.goose.player.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.goose.player.R
import com.goose.player.entity.Song
import com.goose.player.receivers.SkipToNextReceiver
import com.goose.player.receivers.SkipToPreviousReceiver
import com.goose.player.utils.StorageUtil.loadAudioIndex
import com.goose.player.utils.StorageUtil.loadSongList
import com.goose.player.utils.StorageUtil.storeSongIndex
import com.goose.player.view.MainActivity
import kotlinx.android.synthetic.main.fragment_player.view.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 *Created by Gxxxse on 29.07.2019.
 */
class PlayerFragmentController(
    private val mediaController: MediaControllerCompat,
    private val context: Context,
    private val activity: Activity,
    private val view: View): SeekBar.OnSeekBarChangeListener, MediaControllerCompat.Callback() {

    private var songList = ArrayList<Song>()
    private var executor: ScheduledExecutorService? = null
    private var seekBarPositionUpdateTask: Runnable? = null
    private var pauseIc: Drawable = context.resources.getDrawable(R.drawable.ic_pause_btn, null)
    private var playIc: Drawable = context.resources.getDrawable(R.drawable.ic_play_button, null)
    private var player: MediaPlayer
    private var myMediaController: MediaPlayerController

    data class Builder(var mediaController: MediaControllerCompat? = null,
                       var context: Context? = null,
                       var activity: Activity? = null,
                       var view: View? = null){

        fun mediaController(mediaController: MediaControllerCompat)
                = apply { this.mediaController = mediaController }
        fun setContext(context: Context)
                = apply { this.context = context }
        fun activity(activity: Activity)
                = apply { this.activity = activity }
        fun view(view: View)
                = apply { this.view = view }
        fun build() = PlayerFragmentController(
            mediaController!!,
            context!!,
            activity!!,
            view!!
        )
    }

    init {
        view.musicDurationSeekBar.setOnSeekBarChangeListener(this)
        mediaController.registerCallback(this)
        player = MediaPlayer()
        myMediaController = MediaPlayerController(context, mediaController)
        registerReceivers()
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
        super.onPlaybackStateChanged(state)
        when {
            state.state == PlaybackStateCompat.STATE_PLAYING -> onSongPlay()
            state.state == PlaybackStateCompat.STATE_PAUSED -> onSongPause()
            state.state == PlaybackStateCompat.STATE_BUFFERING -> onSongBuffering()
            state.state == PlaybackStateCompat.STATE_STOPPED -> onSongStop()
        }
    }

    fun onNextClick() {
        val position = loadAudioIndex(context)
        val bundle = Bundle()
        if (position == songList.size - 1) {
            bundle.putSerializable("song", songList[position])
            mediaController.transportControls.playFromUri(Uri.parse(songList[position].path), bundle)
        } else {
            bundle.putSerializable("song", songList[position + 1])
            mediaController.transportControls.playFromUri(Uri.parse(songList[position + 1].path), bundle)
            storeSongIndex(position + 1, context)
        }
    }

    fun onPreviousClick() {
        val position = loadAudioIndex(context)
        val bundle = Bundle()
        if (position == 0) {
            bundle.putSerializable("song", songList[position])
            mediaController.transportControls.playFromUri(Uri.parse(songList[position].path), bundle)
        } else {
            bundle.putSerializable("song", songList[position - 1])
            mediaController.transportControls.playFromUri(Uri.parse(songList[position - 1].path), bundle)
            storeSongIndex(position - 1, context)
        }
    }

    fun showSongList() {
        (activity as MainActivity).showSongsList()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        formatSeekBarDurationHint(progress)?.let { setSongHintText(it) }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    private fun formatSeekBarDurationHint(progress: Int): String? {
        if (progress != 0) {
            val hours = progress / 1000 / 60 / 60
            val minutes = progress / 1000 / 60
            val seconds = progress / 1000 - minutes * 60
            return getSongDuration(hours.toString(), minutes.toString(), seconds.toString())
        }
        return null
    }

    private fun setSongHintText(duration: String) {
        view.actualDuration.text = duration
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (seekBar.progress != 0) {
            myMediaController.seekTo(seekBar.progress)
        }
    }

    private fun isPlaying(): Boolean {
        return mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING
    }

    fun musicStateAction() {
        if (isPlaying()) {
            mediaController.transportControls.pause()
        } else {
            mediaController.transportControls.play()
        }
    }

    private fun setSongDetails(song: Song) {
        with(view){
            songDuration.text = getSongDuration(song.hours, song.minutes, song.seconds)
            artist.text = song.artist
            songName.text = song.name
        }
        if (song.album != null) {
            Glide.with(context).load(song.album).into(view.album)
        } else {
            Glide.with(context).load(context.resources.getDrawable(R.drawable.ic_vinyl, null)).into(view.album)
        }
    }

    private fun getSongDuration(hours: String, minutes: String, seconds: String): String {
        var songMinutes = minutes
        var songSeconds = seconds

        if (minutes.toInt() < 10) {
            songMinutes = "0$minutes"
        }
        if (seconds.toInt() < 10) {
            songSeconds = "0$seconds"
        }

        return "$hours:$songMinutes:$songSeconds"
    }

    private fun onSongPlay() {
        if (!view.musicStateBtn.isEnabled){
            view.musicStateBtn.isEnabled = true
            view.nextBtn.isEnabled = true
            view.prevBtn.isEnabled = true
        }
        view.musicStateBtn.background = null
        view.musicStateBtn.background = pauseIc
        myMediaController.play()
        startUpdatingCallbackWithPosition()
    }

    private fun onSongBuffering() {
        val metadata = mediaController.metadata
        songList = loadSongList(context)
        view.musicDurationSeekBar.max = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()
        myMediaController.playNewSong(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))
        mediaController.transportControls.play()
    }

    private fun onSongStop() {
        myMediaController.pause()
    }

    private fun createSongInstanceFromMetadata(metadata: MediaMetadataCompat): Song {
        return Song(
            metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI),
            metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
            metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI),
            metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST),
            metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt(),
            metadata.getString("hours"),
            metadata.getString("minutes"),
            metadata.getString("seconds")
        )
    }

    private fun onSongPause() {
        view.musicStateBtn.background = null
        view.musicStateBtn.background = playIc
        myMediaController.pause()
        stopUpdatingCallbackWithPosition(false)
    }

    private fun startUpdatingCallbackWithPosition() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor()
        }
        if (seekBarPositionUpdateTask == null) {
            seekBarPositionUpdateTask = Runnable { updateProgressCallbackTask() }
        }
        executor!!.scheduleAtFixedRate(
            seekBarPositionUpdateTask,
            0,
            1000,
            TimeUnit.MILLISECONDS
        )
    }

    private fun updateProgressCallbackTask() {
        if (isPlaying()) {
            val currentPosition = myMediaController.getCurrentPosition()
            onSeekBarPositionChange(currentPosition)
        }
    }

    private fun stopUpdatingCallbackWithPosition(resetUIPlaybackPosition: Boolean) {
        if (executor != null) {
            executor?.shutdownNow()
            executor = null
            seekBarPositionUpdateTask = null
            if (resetUIPlaybackPosition) {
                onSeekBarPositionChange(0)
            }
        }
    }

    private fun onSeekBarPositionChange(progress: Int) {
        view.musicDurationSeekBar.progress = progress
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat) {
        super.onMetadataChanged(metadata)
        setSongDetails(createSongInstanceFromMetadata(metadata))
    }

    private fun registerReceivers(){
        context.registerReceiver(createSkipToNextReceiver(), IntentFilter(MainActivity().SKIP_TO_NEXT))
        context.registerReceiver(createSkipToPrevReceiver(), IntentFilter(MainActivity().SKIP_TO_PREVIOUS))
    }

    private fun createSkipToNextReceiver(): SkipToNextReceiver {
        return object : SkipToNextReceiver(){
            override fun onReceive(context: Context, intent: Intent) {
                onNextClick()
            }
        }
    }

    private fun createSkipToPrevReceiver(): SkipToPreviousReceiver {
        return object : SkipToPreviousReceiver(){
            override fun onReceive(context: Context, intent: Intent) {
                onPreviousClick()
            }
        }
    }

}