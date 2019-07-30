package com.goose.player.controller

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.goose.player.R
import com.goose.player.entity.Song
import com.goose.player.view.MainActivity
import kotlinx.android.synthetic.main.fragment_player.view.*

/**
 *Created by Gxxxse on 29.07.2019.
 */
class PlayerFragmentController(
    private val mediaController: MediaPlayerController,
    private val context: Context,
    private val activity: Activity,
    private val songList: ArrayList<Song>,
    private val view: View): SeekBar.OnSeekBarChangeListener{

    private var actualSong: Song? = null
    private var pauseIc: Drawable = context.resources.getDrawable(R.drawable.ic_pause_btn, null)
    private var playIc: Drawable = context.resources.getDrawable(R.drawable.ic_play_button, null)

    data class Builder(var mediaController: MediaPlayerController? = null,
                       var context: Context? = null,
                       var activity: Activity? = null,
                       var songList: ArrayList<Song>? = null,
                       var view: View? = null){

        fun mediaController(mediaController: MediaPlayerController)
                = apply { this.mediaController = mediaController }
        fun setContext(context: Context)
                = apply { this.context = context }
        fun activity(activity: Activity)
                = apply { this.activity = activity }
        fun songList(songList: ArrayList<Song>)
                = apply { this.songList = songList }
        fun view(view: View)
                = apply { this.view = view }
        fun build() = PlayerFragmentController(
            mediaController!!,
            context!!,
            activity!!,
            songList!!,
            view!!
        )
    }

    init {
        view.musicDurationSeekBar.setOnSeekBarChangeListener(this)
    }

    fun onNextClick() {
        if (mediaController.isPlaying()) {
            val position = getActualSongPosition()
            if (position == songList.size - 1) {
                mediaController.playNewSong(songList[position])
            } else {
                saveActualSongPosition(position + 1)
                mediaController.playNewSong(songList[position + 1])
            }
        }
    }

    fun onPreviousClick() {
        if (mediaController.isPlaying()) {
            val position = getActualSongPosition()
            if (position == 0) {
                mediaController.playNewSong(songList[position])
            } else {
                saveActualSongPosition(position - 1)
                mediaController.playNewSong(songList[position - 1])
            }
        }
    }

    private fun getActualSongPosition(): Int {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        return sharedPref?.getInt("actualSongPosition", 0)!!
    }

    private fun saveActualSongPosition(position: Int) {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt("actualSongPosition", position)
            apply()
        }
    }

    fun showSongList() {
        (activity as MainActivity).showSongsList()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (actualSong != null) {
            formatSeekBarDurationHint(progress)?.let { setSongHintText(it) }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    private fun formatSeekBarDurationHint(progress: Int): String? {
        if (actualSong != null && progress != 0) {
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
        if (actualSong != null && seekBar.progress != 0) {
            mediaController.seekTo(seekBar.progress)
        }
    }

    fun musicStateAction() {
        if (mediaController.isPlaying()) {
            mediaController.pause()
        } else {
            mediaController.play()
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

    fun onSongPlay(song: Song) {
        view.musicDurationSeekBar.max = song.duration
        setSongDetails(song)
        mediaController.startUpdatingCallbackWithPosition()
        view.musicStateBtn.background = null
        view.musicStateBtn.background = pauseIc
        actualSong = song
    }

    fun onSongResume() {
        view.musicStateBtn.background = null
        view.musicStateBtn.background = pauseIc
        mediaController.startUpdatingCallbackWithPosition()
    }

    fun onSongPause() {
        view.musicStateBtn.background = null
        view.musicStateBtn.background = playIc
        mediaController.stopUpdatingCallbackWithPosition(false)
    }

    fun onSongRelease() {
        mediaController.stopUpdatingCallbackWithPosition(true)
    }

    fun onSeekBarPositionChange(progress: Int) {
        view.musicDurationSeekBar.progress = progress
    }

}