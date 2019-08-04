package com.goose.player

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.goose.player.controller.MediaPlayerController
import com.goose.player.entity.Song
import com.goose.player.utils.NotificationHelper.createNotification
import com.goose.player.utils.NotificationHelper.createNotificationChannel
import com.goose.player.view.MainActivity

/**
 *Created by Gxxxse on 31.07.2019.
 */

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_media_root_id"
private const val SPEED_PLAYING = 1F
private const val SPEED_PAUSE = 0F
private const val SPEED_REWIND = -1F

class MediaPlaybackService : MediaBrowserServiceCompat() {
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var myPlayerNotification: Notification
    private lateinit var context: Context
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var player: MediaPlayer
    private lateinit var notificationManager: NotificationManager
    private lateinit var myMediaController: MediaPlayerController

    private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()
    private val intentFilter = IntentFilter(ACTION_AUDIO_BECOMING_NOISY)

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        player = MediaPlayer()
        createMediaSession()
        myMediaController = MediaPlayerController(context, player, mediaSession)
    }

    private fun createMediaSession() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        mediaSession = MediaSessionCompat(baseContext, "MediaSessionLogTag").apply {

            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PAUSE
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            setPlaybackState(stateBuilder.build())
            setCallback(callback)
            setSessionToken(sessionToken)
            setSessionActivity(pendingIntent)
        }
    }

    private val callback = object: MediaSessionCompat.Callback() {
        override fun onPlay() {
            startService(Intent(context, MediaBrowserService::class.java))
            mediaSession.isActive = true
            myMediaController.play()
            mediaSession.setPlaybackState(buildPlayState())
            registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
            Log.d("MediaSessionCallback", "play")
        }

        override fun onPause() {
            super.onPause()
            mediaSession.isActive = false
            myMediaController.pause()
            mediaSession.setPlaybackState(buildPauseState())
            Log.d("MediaSessionCallback", "pause")
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            val song = extras?.getSerializable("song") as Song
            myMediaController.playNewSong(song)
            mediaSession.setPlaybackState(buildPlayState())
            mediaSession.setMetadata(buildMetadata(song))
            buildNotification(song)
            Log.d("MediaSessionCallback", "fromUri")
        }

        override fun onStop() {
            super.onStop()
            unregisterReceiver(myNoisyAudioStreamReceiver)
            mediaSession.isActive = false
            player.stop()
            stopSelf()
            stopForeground(false)
            unregisterReceiver(myNoisyAudioStreamReceiver)
            Log.d("MediaSessionCallback", "stop")
        }

    }

    private fun buildMetadata(song: Song): MediaMetadataCompat? {
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.name)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration.toLong())
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.path)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.album)
            .putString("hours", song.hours)
            .putString("minutes", song.minutes)
            .putString("seconds", song.seconds)
            .build()
    }

    private fun buildPauseState(): PlaybackStateCompat {
        return PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                SPEED_PAUSE).build()
    }

    private fun buildPlayState(): PlaybackStateCompat {
        return PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                SPEED_PLAYING).build()
    }

    fun isPlaying(): Boolean {
        return mediaSession.controller.playbackState.playbackState == PlaybackStateCompat.STATE_PLAYING
    }

    private fun buildNotification(song: Song) {
        createNotificationChannel(notificationManager)
        val builder = createNotification(song, mediaSession, context)
        myPlayerNotification = builder.build()
        startForeground(1, myPlayerNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancel(1)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        if (MY_EMPTY_MEDIA_ROOT_ID == parentId) {
            result.sendResult(null)
            return
        }
        val mediaItems = emptyList<MediaBrowserCompat.MediaItem>()
        result.sendResult(mediaItems.toMutableList())
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("root_id_001", null)
    }

    private class BecomingNoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {

            }
        }
    }
}