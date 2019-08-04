package com.goose.player.controller

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import com.goose.player.interfaces.SongStateListener
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 *Created by Gxxxse on 21.07.2019.
 */
class MediaPlayerController(private val context: Context,
                            private val mediaControllerCompat: MediaControllerCompat) : MediaPlayer.OnCompletionListener {

    private var listener: SongStateListener? = null
    private var executor: ScheduledExecutorService? = null
    private var seekBarPositionUpdateTask: Runnable? = null
    private var mediaPlayer = MediaPlayer()

    private fun release() {
        mediaPlayer.release()
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    private fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun startUpdatingCallbackWithPosition() {
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
            val currentPosition = mediaPlayer.currentPosition
            if (listener != null) {
//                listener!!.onSeekBarPositionChange(currentPosition)
            }
        }
    }

    fun stopUpdatingCallbackWithPosition(resetUIPlaybackPosition: Boolean) {
        if (executor != null) {
            executor?.shutdownNow()
            executor = null
            seekBarPositionUpdateTask = null
            if (resetUIPlaybackPosition && listener != null) {
//                listener!!.onSeekBarPositionChange(0)
            }
        }
    }


    fun playNewSong(path: String){
        if (isPlaying()){
            release()
        }

        mediaPlayer = MediaPlayer.create(context, Uri.parse(path))
        mediaPlayer.setOnCompletionListener(this)
    }

    fun play() {
        if (!isPlaying()) {
            mediaPlayer.start()
            Log.d("MediaSessionCallback", "playFromController")
        }
    }

    fun reset() {
        mediaPlayer.reset()
    }

    fun pause() {
        if (isPlaying()) {
            Log.d("MediaSessionCallback", "pauseFromController")
            mediaPlayer.pause()
        }
    }

    fun stop(){
        if (isPlaying()){
            mediaPlayer.stop()
        }
    }

    fun seekTo(ms: Int) {
        mediaPlayer.seekTo(ms)
        play()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.d("song completed", "song completed")
    }

}
