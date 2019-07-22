package com.goose.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.goose.player.entity.Song
import com.goose.player.interfaces.SongStateListener

/**
 *Created by Gxxxse on 21.07.2019.
 */
class MediaPlayerController(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var listener: SongStateListener? = null

    private fun release() {
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    fun isPlaying(): Boolean {
        return if (mediaPlayer?.isPlaying != null){
            mediaPlayer?.isPlaying!!
        }else{
            false
        }
    }

    fun playNewSong(song: Song){
        if (mediaPlayer != null && isPlaying()){
            release()
        }

        mediaPlayer = MediaPlayer.create(context, Uri.parse(song.path))
        mediaPlayer?.start()
        listener?.onSongPlay(song)
    }

    fun play() {
        if (mediaPlayer != null && !isPlaying()) {
            mediaPlayer?.start()
            listener?.onSongPause()
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

    fun seekTo(ms: Int){
        if (mediaPlayer != null){
            mediaPlayer?.seekTo(ms)
        }
    }

    fun setListener(listener: SongStateListener){
        this.listener = listener
    }
}
