package com.goose.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.goose.player.entity.Song
import com.goose.player.interfaces.SongStateListener
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 *Created by Gxxxse on 21.07.2019.
 */
class MediaPlayerController(private val context: Context) : MediaPlayer.OnCompletionListener {
    private var listener: SongStateListener? = null
    private var mediaPlayer: MediaPlayer? = null
    private var executor: ScheduledExecutorService? = null
    private var seekBarPositionUpdateTask: Runnable? = null

    private fun release() {
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    fun isPlaying(): Boolean {
        return if (mediaPlayer != null){
            mediaPlayer?.isPlaying!!
        }else{
            false
        }
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
        if (mediaPlayer != null && isPlaying()) {
            val currentPosition = mediaPlayer!!.currentPosition
            if (listener != null) {
                listener!!.onSeekBarPositionChange(currentPosition)
            }
        }
    }

    fun stopUpdatingCallbackWithPosition(resetUIPlaybackPosition: Boolean) {
        if (executor != null) {
            executor?.shutdownNow()
            executor = null
            seekBarPositionUpdateTask = null
            if (resetUIPlaybackPosition && listener != null) {
                listener!!.onSeekBarPositionChange(0)
            }
        }
    }


    fun playNewSong(song: Song){
        if (mediaPlayer != null && isPlaying()){
            release()
        }

        mediaPlayer = MediaPlayer.create(context, Uri.parse(song.path))
        mediaPlayer?.start()
        listener?.onSongPlay(song)
        mediaPlayer?.setOnCompletionListener(this)
    }

    fun play() {
        if (mediaPlayer != null && !isPlaying()) {
            mediaPlayer?.start()
            listener?.onSongResume()
        }
    }

    fun reset() {
        if (mediaPlayer != null) {
            mediaPlayer!!.reset()
        }
    }

    fun pause() {
        if (mediaPlayer != null && isPlaying()) {
            mediaPlayer!!.pause()
            listener?.onSongPause()
        }
    }

    fun seekTo(ms: Int) {
        if (mediaPlayer != null) {
            mediaPlayer?.seekTo(ms)
            play()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        listener?.onSongPause()
        Log.d("song completed", "song completed")
    }

    fun setListener(listener: SongStateListener){
        this.listener = listener
    }
}
