package com.goose.player.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.goose.player.MediaPlayerController
import com.goose.player.R
import com.goose.player.entity.Song
import com.goose.player.extensions.setInvisible
import com.goose.player.extensions.toast
import com.goose.player.interfaces.SongStateListener
import com.goose.player.utils.FileHelper.getAllAudioFromDevice
import kotlinx.android.synthetic.main.fragment_player.*


class PlayerFragment : androidx.fragment.app.Fragment(), SeekBar.OnSeekBarChangeListener, View.OnClickListener, SongStateListener {

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
        checkPermissions()
        musicDurationSeekBar.setOnSeekBarChangeListener(this)
        showSongListBtn.setOnClickListener(this)
        musicStateBtn.setOnClickListener(this)
        mediaController?.setListener(this)
        nextBtn.setOnClickListener(this)
        prevBtn.setOnClickListener(this)

        pauseIc = resources.getDrawable(R.drawable.ic_pause_btn, null)
        playIc = resources.getDrawable(R.drawable.ic_play_button, null)
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
            }
        } else {
            songList = getAllAudioFromDevice(context!!)
        }
    }

    private fun onNextClick(){
        if (mediaController!!.isPlaying()){
            val position = getActualSongPosition()
            if (position == songList.size){
                mediaController?.playNewSong(songList[position])
            }else{
                saveActualSongPosition(position + 1)
                mediaController?.playNewSong(songList[position + 1])
            }
            saveActualSongPosition(position)
        }
    }

    private fun onPreviousClick(){
        if (mediaController!!.isPlaying()){
            val position = getActualSongPosition()
            if (position == 0){
                mediaController?.playNewSong(songList[position])
            }else{
                saveActualSongPosition(position - 1)
                mediaController?.playNewSong(songList[position - 1])
            }
        }
    }

    private fun saveActualSongPosition(position: Int) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt("actualSongPosition", position)
            apply()
        }
    }

    private fun getActualSongPosition(): Int {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        return sharedPref?.getInt("actualSongPosition", 0)!!
    }

    override fun onClick(v: View) {
        when(v){
            musicStateBtn -> musicStateAction()
            showSongListBtn -> showSongList()
            nextBtn -> onNextClick()
            prevBtn -> onPreviousClick()
        }
    }

    private fun showSongList() {
        (activity as MainActivity).showSongsList()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (actualSong != null){
            formatSeekBarDurationHint(progress)?.let { setSongHintText(it) }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
    }

    private fun formatSeekBarDurationHint(progress: Int): String? {
        if (actualSong != null && progress != 0){
            val hours = progress / 1000 / 60 / 60
            val minutes = progress / 1000 / 60
            val seconds = progress / 1000 - minutes * 60
            return getSongDuration(hours.toString(), minutes.toString(), seconds.toString())
        }
        return null
    }

    private fun setSongHintText(duration: String) {
        actualDuration.text = duration
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (actualSong != null && seekBar.progress != 0){
            mediaController?.seekTo(seekBar.progress)
        }
    }

    override fun onResume() {
        super.onResume()
        musicStateAction()
    }

    fun setMediaController(controller: MediaPlayerController){
        mediaController = controller
    }

    override fun onSongPlay(song: Song) {
        musicDurationSeekBar.max = song.duration
        setSongDetails(song)
        mediaController!!.startUpdatingCallbackWithPosition()
        musicStateBtn.background = null
        musicStateBtn.background = pauseIc
        actualSong = song
    }

    override fun onSongResume() {
        musicStateBtn.background = null
        musicStateBtn.background = pauseIc
        mediaController!!.startUpdatingCallbackWithPosition()
    }

    private fun musicStateAction() {
        if (mediaController!!.isPlaying()){
            mediaController!!.pause()
        }else{
            mediaController!!.play()
        }
    }

    private fun setSongDetails(song: Song) {
        songDuration.text = getSongDuration(song.hours, song.minutes, song.seconds)
        artist.text = song.artist
        songName.text = song.name
        if (song.album != null){
            Glide.with(this).load(song.path).into(album)
        }else{
            album.setInvisible()
        }
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
        musicStateBtn.background = null
        musicStateBtn.background = playIc
        mediaController?.stopUpdatingCallbackWithPosition(false)
    }

    override fun onSongRelease(){
        mediaController?.stopUpdatingCallbackWithPosition(true)
    }

    override fun onSeekBarPositionChange(progress: Int) {
        musicDurationSeekBar.progress = progress
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_PERMISSIONS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    songList = getAllAudioFromDevice(context!!)
                } else {
                    checkPermissions()
                }
                return
            }
            else -> {
                return
            }
        }
    }
}
