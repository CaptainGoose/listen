package com.goose.player.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.fromParts
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.goose.player.R
import com.goose.player.controller.MediaPlayerController
import com.goose.player.controller.PlayerFragmentController
import com.goose.player.entity.Song
import com.goose.player.interfaces.SongStateListener
import com.goose.player.utils.FileHelper.getAllAudioFromDevice
import kotlinx.android.synthetic.main.fragment_player.*

private const val REQUEST_READ_PERMISSIONS = 1
private const val SETTINGS_CODE = 2

class PlayerFragment : Fragment(), View.OnClickListener, SongStateListener {

    private lateinit var playerFragmentController: PlayerFragmentController
    private var mediaController: MediaPlayerController? = null
    private var songList = ArrayList<Song>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissions()
        mediaController?.setListener(this)
        musicStateBtn.setOnClickListener(this)
        showSongListBtn.setOnClickListener(this)
        nextBtn.setOnClickListener(this)
        prevBtn.setOnClickListener(this)
        initFragmentController()
    }

    private fun initFragmentController() {
        playerFragmentController = PlayerFragmentController.Builder()
            .activity(activity!!)
            .setContext(context!!)
            .mediaController(mediaController!!)
            .songList(songList)
            .view(view!!)
            .build()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                createDialog()
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_PERMISSIONS
                )
            }
        } else {
            songList = getAllAudioFromDevice(context!!)
        }
    }

    override fun onClick(v: View) {
        when (v) {
            musicStateBtn -> playerFragmentController.musicStateAction()
            showSongListBtn -> playerFragmentController.showSongList()
            nextBtn -> playerFragmentController.onNextClick()
            prevBtn -> playerFragmentController.onPreviousClick()
        }
    }

    fun setMediaController(controller: MediaPlayerController) {
        mediaController = controller
    }

    override fun onSongPlay(song: Song) {
        playerFragmentController.onSongPlay(song)
    }

    override fun onSongResume() {
        playerFragmentController.onSongResume()
    }

    override fun onSongPause() {
        playerFragmentController.onSongPause()
    }

    override fun onSongRelease() {
        playerFragmentController.onSongRelease()
    }

    override fun onSeekBarPositionChange(progress: Int) {
        playerFragmentController.onSeekBarPositionChange(progress)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_PERMISSIONS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    songList = getAllAudioFromDevice(context!!)
                } else {
                    createDialog()
                }
                return
            }
            else -> {
                return
            }
        }
    }

    private fun createDialog() {
        val builder = AlertDialog.Builder(context!!)
        with(builder) {
            setPositiveButton("Ok") { _, _ -> openAppSettings() }
            setNegativeButton("Cancel") { _, _ -> activity?.finish() }
            setTitle("Enable Permissions")
            setMessage("For use this application you need enable memory permission")
            setCancelable(false)
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent()
        intent.action = ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = fromParts("package", activity?.packageName, null)
        intent.data = uri
        startActivityForResult(intent, SETTINGS_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_CODE) {
            checkPermissions()
        }
    }


}
