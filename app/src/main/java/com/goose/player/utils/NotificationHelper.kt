package com.goose.player.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.goose.player.R
import com.goose.player.entity.Song

/**
 *Created by Gxxxse on 02.08.2019.
 */
object NotificationHelper {
    fun createNotification(song: Song, mediaSession: MediaSessionCompat, context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, "listen_001").apply {
            setContentTitle(song.artist)
            setContentText(song.name)
            setLargeIcon(BitmapFactory.decodeFile(song.album))
            priority = NotificationCompat.PRIORITY_LOW
            setContentIntent(mediaSession.controller.sessionActivity)
            setOngoing(true)
            setDefaults(0)
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
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )

            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_pause_btn,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )

            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_next_arrow,
                    "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )

            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)

                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
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
}