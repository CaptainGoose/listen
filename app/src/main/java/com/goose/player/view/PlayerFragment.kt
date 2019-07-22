package com.goose.player.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.goose.player.MediaPlayerController
import com.goose.player.R
import com.goose.player.entity.Song
import com.goose.player.extensions.setGone
import com.goose.player.extensions.setVisible
import com.goose.player.extensions.toast
import com.goose.player.interfaces.SongStateListener
import com.goose.player.utils.FileHelper.getAllAudioFromDevice
import kotlinx.android.synthetic.main.fragment_player.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class PlayerFragment : Fragment(), SeekBar.OnSeekBarChangeListener, View.OnClickListener, SongStateListener {

    private val REQUEST_READ_PERMISSIONS = 1
    private var songList = ArrayList<Song>()
    private var mediaController: MediaPlayerController? = null
    private lateinit var pauseIc: Drawable
    private lateinit var playIc: Drawable
    private var actualSong: Song? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        musicDurationSeekBar.setOnSeekBarChangeListener(this)
        checkPermissions()
        musicStateBtn.setOnClickListener(this)
        mediaController?.setListener(this)
        pauseIc = resources.getDrawable(R.drawable.ic_pause_btn, null)
        playIc = resources.getDrawable(R.drawable.ic_play_button, null)
        showSongListBtn.setOnClickListener(this)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                toast("you need to enable permissions")
            } else {
                ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_PERMISSIONS)
                songList = getAllAudioFromDevice(context!!)
            }
        } else {
            songList = getAllAudioFromDevice(context!!)
        }
    }

    override fun onClick(v: View) {
        when(v){
            musicStateBtn -> musicStateAction()
            showSongListBtn -> showSongList()
        }
    }

    private fun showSongList() {
        (activity as MainActivity).showSongsList()
    }

    private fun musicStateAction() {
        if (mediaController!!.isPlaying()){
            musicStateBtn.background = pauseIc
            mediaController!!.pause()
        }else if (!mediaController!!.isPlaying()){
            musicStateBtn.background = playIc
            mediaController!!.play()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (actualSong != null) {
            formatSeekBarDurationHint(progress)?.let { setSongHintText(it) }
            val percent = progress / seekBar.max.toDouble()
            val offset = seekBar.thumbOffset
            val seekWidth = seekBar.width
            val dontKnowWhatIsThis = (percent * (seekWidth - 2 * offset)).roundToInt()
            val labelWidth = durationText.width
            durationText.x = (offset + seekBar.x + dontKnowWhatIsThis - (percent * offset).roundToInt()
                    - (percent * labelWidth / 2).roundToInt())
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        durationText.setVisible()
        seekBar.isClickable = actualSong == null
    }

    private fun formatSeekBarDurationHint(seekBarTouchProgress: Int): String? {
        if (actualSong != null && seekBarTouchProgress != 0){
            val duration = ceil(actualSong!!.duration * (seekBarTouchProgress.toDouble() / 10))
            val hours = (duration / 1000 / 60 / 60).roundToInt()
            val minutes = (duration / 1000 / 60).roundToInt()
            val seconds = (duration / 1000 - minutes * 60).roundToInt()
            return getSongDuration(hours.toString(), minutes.toString(), seconds.toString())
        }
        return null
    }

    private fun setSongHintText(duration: String) {
        durationText.text = duration
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (actualSong == null){
            seekBar.isClickable = false
        }else{
            if (seekBar.progress != 0){
                mediaController?.seekTo(actualSong!!.duration / (seekBar.progress / 10))
            }
            seekBar.isClickable = true
            durationText.setGone()
        }
    }

    override fun onResume() {
        super.onResume()
        checkMusicStateBtn()
    }

    private fun checkMusicStateBtn() {
        if (mediaController!!.isPlaying()){
            musicStateBtn.background = pauseIc
        }else{
            musicStateBtn.background = playIc
        }
    }

    fun setMediaController(controller: MediaPlayerController){
        mediaController = controller
    }

    override fun onSongPlay(song: Song) {
        setSongDetails(song)
        checkMusicStateBtn()
        actualSong = song
    }

    private fun setSongDetails(song: Song) {
        songDuration.text = getSongDuration(song.hours, song.minutes, song.seconds)
        artist.text = song.artist
        songName.text = song.name
    }

    private fun getSongDuration(hours: String, minutes: String, seconds: String): String {
        var songMinutes = minutes
        var songSeconds = seconds

        if (minutes.toInt() < 10){
            songMinutes = "0$minutes"
        }
        if (seconds.toInt() < 10){
            songSeconds = "0$seconds"
        }

        return "$hours:$songMinutes:$songSeconds"
    }

    override fun onSongPause() {
        checkMusicStateBtn()
    }
}
