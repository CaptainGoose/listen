package com.goose.player.view

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.appcompat.app.AppCompatActivity
import com.goose.player.R
import com.goose.player.adapter.MainMenuViewPagerAdapter
import com.goose.player.enums.Pages.PlayerPage
import com.goose.player.enums.Pages.SongsListPage
import com.goose.player.services.MediaPlaybackService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){
    val SKIP_TO_NEXT = "com.goose.player.action.skiptonext"
    val SKIP_TO_PREVIOUS = "com.goose.player.action.skiptoprevious"

    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var systemMediaController: MediaControllerCompat
    private lateinit var connectionCallbacks: MediaBrowserCompat.ConnectionCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connectionCallbacks = createConnectionCallback()
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            connectionCallbacks,
            null // optional Bundle
        )
        mediaBrowser.connect()
    }

    fun buildTransportControls() {
        systemMediaController = MediaControllerCompat.getMediaController(this@MainActivity)
    }

    private fun createConnectionCallback(): MediaBrowserCompat.ConnectionCallback {
        return object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                mediaBrowser.sessionToken.also { token ->
                    systemMediaController = MediaControllerCompat(
                        this@MainActivity, // Context
                        token
                    )

                    MediaControllerCompat.setMediaController(this@MainActivity, systemMediaController)
                    mainPageViewPager.adapter = MainMenuViewPagerAdapter(supportFragmentManager, systemMediaController)
                }

                buildTransportControls()
            }

            override fun onConnectionSuspended() {}

            override fun onConnectionFailed() {}
        }
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        mediaBrowser.disconnect()
    }

    fun showSongsList(){
        mainPageViewPager.currentItem = SongsListPage.number
    }

    fun showPlayer(){
        mainPageViewPager.currentItem = PlayerPage.number
    }

}
