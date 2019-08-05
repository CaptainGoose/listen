package com.goose.player.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.goose.player.R
import com.goose.player.enums.PlaybackStatus
import com.goose.player.services.*

/**
 *Created by Gxxxse on 02.08.2019.
 */
object NotificationHelper {
    fun createNotification(mediaSession: MediaSessionCompat, context: Context,
                           playbackStatus: PlaybackStatus): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, "listen_001").apply {

            var notificationAction = android.R.drawable.ic_media_pause//needs to be initialized
            var playPauseAction: PendingIntent? = null

            if (playbackStatus === PlaybackStatus.PLAYING) {
                notificationAction = R.drawable.ic_pause_btn
                playPauseAction = playbackAction(context,1, mediaSession)
            } else if (playbackStatus === PlaybackStatus.PAUSED) {
                notificationAction = R.drawable.ic_play_button
                playPauseAction = playbackAction(context, 0, mediaSession)
            }

            val metadata = mediaSession.controller.metadata

            setContentTitle(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
            setContentText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
            setLargeIcon(BitmapFactory.decodeFile(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)))
            priority = NotificationCompat.PRIORITY_LOW
            setContentIntent(mediaSession.controller.sessionActivity)
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            setSmallIcon(R.drawable.ic_vinyl)
            color = ContextCompat.getColor(context, R.color.colorPrimary)

            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_previous_arrow,
                    "Previous",
                    playbackAction(context,3, mediaSession)
                )
            )
            addAction(
                NotificationCompat.Action(
                    notificationAction,
                    "Pause",
                    playPauseAction
                )
            )
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_next_arrow,
                    "Next",
                    playbackAction(context,2, mediaSession)
                )
            )

            addAction(
                R.drawable.ic_collapse,
                "Close",
                playbackAction(context, 4, mediaSession)
            )

            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)

                    .setShowCancelButton(true)
                    .setCancelButtonIntent(playbackAction(context, 4, mediaSession))
            )
        }
    }

    fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Listen"
            val descriptionText = "Listen App Channgel"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("listen_001", name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun playbackAction(context: Context, actionNumber: Int, mediaSession: MediaSessionCompat): PendingIntent? {
        val playbackAction = Intent(context, MediaPlaybackService::class.java)
        when (actionNumber) {
            0 -> {
                playbackAction.action = ACTION_PLAY
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            1 -> {
                playbackAction.action = ACTION_PAUSE
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            2 -> {
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            3 -> {
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            4 -> {
                playbackAction.action = ACTION_STOP
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }
}