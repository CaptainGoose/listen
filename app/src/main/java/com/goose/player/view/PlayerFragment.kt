package com.goose.player.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.fromParts
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.goose.player.R
import com.goose.player.controller.PlayerFragmentController
import com.goose.player.entity.Song
import com.goose.player.utils.FileHelper.getAllAudioFromDevice
import kotlinx.android.synthetic.main.fragment_player.*


private const val REQUEST_READ_PERMISSIONS = 1
private const val SETTINGS_CODE = 2

class PlayerFragment : Fragment(), View.OnClickListener {

    private lateinit var playerFragmentController: PlayerFragmentController
    private var mediaController: MediaControllerCompat? = null
    private var songList = ArrayList<Song>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissions()
        mediaController = MediaControllerCompat.getMediaController(activity as MainActivity)
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
            .view(view!!)
            .build()
    }

    private fun checkPermissions() {
        val permissionState
                = ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
        val shouldShowRationale
                = ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRationale) {
                createDialog()
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_PERMISSIONS)
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

    fun setSystemMediaController(systemControllerCompat: MediaControllerCompat) {
        this.mediaController = systemControllerCompat
    }
}
