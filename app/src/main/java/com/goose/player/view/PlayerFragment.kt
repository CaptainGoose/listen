package com.goose.player.view

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.goose.player.R
import com.goose.player.entity.Song
import com.goose.player.extensions.toast
import com.goose.player.utils.FileHelper.getAllAudioFromDevice
import kotlinx.android.synthetic.main.fragment_player.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class PlayerFragment : Fragment(), SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private val REQUEST_GET_MEDIA = 1
    private val REQUEST_READ_PERMISSIONS = 2
    private var songList = ArrayList<Song>()
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        musicDurationSeekBar.setOnSeekBarChangeListener(this)
        initMediaPlayer()
        checkPermissions()
        musicStateBtn.setOnClickListener(this)
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
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

    fun playSong(song: Song) {
        val uri = Uri.parse(song.path)
        mediaPlayer.apply {
            setDataSource(context!!, uri)
            prepare()
            start()
        }
        setSongDetails(song)
    }

    private fun setSongDetails(song: Song) {
        artist.text = song.artist
        songName.text = song.name
        val hours = song.hours
        var minutes = song.minutes
        var seconds = song.seconds

        if (song.minutes.toInt() < 10){
            minutes = "0" + song.minutes
        }
        if (song.seconds.toInt() < 10){
            seconds = "0" + song.seconds
        }

        songDuration.text = "$hours:$minutes:$seconds"
    }

    private fun resumeSong(){
        mediaPlayer.start()
    }

    private fun pauseSong(){
        mediaPlayer.pause()
    }

    fun stopSong(){
        mediaPlayer.stop()
    }

    override fun onClick(v: View) {
        when(v){
            musicStateBtn -> {
                musicStateAction()
            }
        }
    }

    private fun musicStateAction() {
        val pauseIc = resources.getDrawable(R.drawable.ic_pause_btn, null)
        val playIc = resources.getDrawable(R.drawable.ic_play_button, null)
        if (mediaPlayer.isPlaying){
            musicStateBtn.background = playIc
            pauseSong()
        }else if (!mediaPlayer.isPlaying){
            musicStateBtn.background = pauseIc
            resumeSong()
        }else{

        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val x = ceil(progress / 1000f).toInt()

        if (x < 10)
            durationText.text = "0:0$x"
        else
            durationText.text = "0:$x"

        val percent = progress / seekBar.max.toDouble()
        val offset = seekBar.thumbOffset
        val seekWidth = seekBar.width
        val dontKnowWhatIsThis = (percent * (seekWidth - 2 * offset)).roundToInt()
        val labelWidth = durationText.width
        durationText.x = (offset + seekBar.x + dontKnowWhatIsThis - (percent * offset).roundToInt()
                - (percent * labelWidth / 2).roundToInt())

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onResume() {
        super.onResume()
        checkMusicStateBtn()
    }

    private fun checkMusicStateBtn() {
        val pauseIc = resources.getDrawable(R.drawable.ic_pause_btn, null)
        val playIc = resources.getDrawable(R.drawable.ic_play_button, null)

        if (mediaPlayer.isPlaying){
            musicStateBtn.background = pauseIc
        }else{
            musicStateBtn.background = playIc
        }
    }
}
