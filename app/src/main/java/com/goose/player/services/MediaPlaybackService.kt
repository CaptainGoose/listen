package com.goose.player.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
import android.net.Uri
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.goose.player.entity.Song
import com.goose.player.enums.PlaybackStatus
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

const val ACTION_PLAY = "com.goose.player.listen.ACTION_PLAY"
const val ACTION_PAUSE = "com.goose.player.listen.ACTION_PAUSE"
const val ACTION_PREVIOUS = "com.goose.player.listen.ACTION_PREVIOUS"
const val ACTION_NEXT = "com.goose.player.listen.ACTION_NEXT"
const val ACTION_STOP = "com.goose.player.listen.ACTION_STOP"

class MediaPlaybackService : MediaBrowserServiceCompat() {
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var myPlayerNotification: Notification
    private lateinit var context: Context
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager

    private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()
    private val intentFilter = IntentFilter(ACTION_AUDIO_BECOMING_NOISY)

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createMediaSession()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        handleIncomingActions(intent)
        return super.onStartCommand(intent, flags, startId)
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
            mediaSession.setPlaybackState(buildPlayState())
            registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
            buildNotification(PlaybackStatus.PLAYING)
        }

        override fun onPause() {
            super.onPause()
            buildNotification(PlaybackStatus.PAUSED)
            mediaSession.isActive = false
            mediaSession.setPlaybackState(buildPauseState())
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            val song = extras?.getSerializable("song") as Song
            mediaSession.setPlaybackState(buildBufferingState())
            mediaSession.setMetadata(buildMetadata(song))
            buildNotification(PlaybackStatus.PLAYING)
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            buildNotification(PlaybackStatus.PLAYING)
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            buildNotification(PlaybackStatus.PLAYING)
        }

        override fun onStop() {
            super.onStop()
            mediaSession.isActive = false
            stopSelf()
            stopForeground(true)
            mediaSession.setPlaybackState(buildPauseState())
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
                SPEED_PAUSE
            ).build()
    }

    private fun buildPlayState(): PlaybackStateCompat {
        return PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                SPEED_PLAYING
            ).build()
    }

    private fun buildBufferingState(): PlaybackStateCompat {
        return PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_BUFFERING,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                SPEED_PLAYING
            ).build()
    }

    private fun buildNotification(playbackStatus: PlaybackStatus) {
        createNotificationChannel(notificationManager)
        val builder = createNotification(mediaSession, context, playbackStatus)
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


    private fun handleIncomingActions(playbackAction: Intent) {
        if (playbackAction.action == null) return

        val actionString = playbackAction.action
        val mediaController = mediaSession.controller
        when {
            actionString!!.equals(ACTION_PLAY, ignoreCase = true) ->  mediaController.transportControls.play()
            actionString.equals(ACTION_PAUSE, ignoreCase = true) -> mediaController.transportControls.pause()
            actionString.equals(ACTION_NEXT, ignoreCase = true) ->
            {
                val intent = Intent(MainActivity().SKIP_TO_NEXT)
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                sendBroadcast(intent)
            }
            actionString.equals(ACTION_PREVIOUS, ignoreCase = true) -> {
                val intent = Intent(MainActivity().SKIP_TO_PREVIOUS)
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                sendBroadcast(intent)
            }
            actionString.equals(ACTION_STOP, ignoreCase = true) -> mediaController.transportControls.stop()
        }
    }

    private class BecomingNoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_AUDIO_BECOMING_NOISY) {

            }
        }
    }
}