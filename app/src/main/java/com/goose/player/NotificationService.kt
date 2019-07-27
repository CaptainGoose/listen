package com.goose.player

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.goose.player.view.MainActivity


/**
 *Created by Gxxxse on 27.07.2019.
 */
class NotificationService : Service() {
    private val LOG_TAG = "NotificationService"
    var status = Notification()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when {
            intent.action == Constants.ACTION.STARTFOREGROUND_ACTION -> {
                showNotification()
                Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()

            }
            intent.action == Constants.ACTION.PREV_ACTION -> {
                Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show()
                Log.i(LOG_TAG, "Clicked Previous")
            }
            intent.action == Constants.ACTION.PLAY_ACTION -> {
                Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show()
                Log.i(LOG_TAG, "Clicked Play")
            }
            intent.action == Constants.ACTION.NEXT_ACTION -> {
                Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show()
                Log.i(LOG_TAG, "Clicked Next")
            }
            intent.action == Constants.ACTION.STOPFOREGROUND_ACTION -> {
                Log.i(LOG_TAG, "Received Stop Foreground Intent")
                Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }


    private fun showNotification() {
        // Using RemoteViews to bind custom layouts into Notification
        val views = RemoteViews(
            packageName,
            R.layout.status_bar
        )
        val bigViews = RemoteViews(
            packageName,
            R.layout.status_bar_expanded
        )

        // showing default album image
        bigViews.setImageViewResource(R.id.status_bar_album_art, R.drawable.ic_vinyl)

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = Constants.ACTION.MAIN_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )

        val previousIntent = Intent(this, NotificationService::class.java)
        previousIntent.action = Constants.ACTION.PREV_ACTION
        val ppreviousIntent = PendingIntent.getService(
            this, 0,
            previousIntent, 0
        )

        val playIntent = Intent(this, NotificationService::class.java)
        playIntent.action = Constants.ACTION.PLAY_ACTION
        val pplayIntent = PendingIntent.getService(
            this, 0,
            playIntent, 0
        )

        val nextIntent = Intent(this, NotificationService::class.java)
        nextIntent.action = Constants.ACTION.NEXT_ACTION
        val pnextIntent = PendingIntent.getService(
            this, 0,
            nextIntent, 0
        )

        val closeIntent = Intent(this, NotificationService::class.java)
        closeIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
        val pcloseIntent = PendingIntent.getService(
            this, 0,
            closeIntent, 0
        )

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)

        views.setImageViewResource(
            R.id.status_bar_play,
            R.drawable.ic_pause_btn
        )
        bigViews.setImageViewResource(
            R.id.status_bar_play,
            R.drawable.ic_pause_btn
        )

        views.setTextViewText(R.id.status_bar_track_name, "Song Title")
        bigViews.setTextViewText(R.id.status_bar_track_name, "Song Title")

        views.setTextViewText(R.id.status_bar_artist_name, "Artist Name")
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Artist Name")

        bigViews.setTextViewText(R.id.status_bar_album_name, "Album Name")



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val NOTIFICATION_CHANNEL_ID = "com.goose.listen"
            val channelName = "Music player background service"
            val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)

            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            val notification = notificationBuilder
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_vinyl)
                .setContentTitle("Listen!")
                .setPriority(NotificationManager.IMPORTANCE_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContent(views)
                .setCustomBigContentView(bigViews)
                .build()
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification)
        } else {
            status = Notification.Builder(applicationContext).build()
            with(status) {
                contentView = views
                bigContentView = bigViews
                flags = Notification.FLAG_ONGOING_EVENT
                icon = R.drawable.ic_vinyl
                contentIntent = pendingIntent
            }
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status)
        }
    }
}