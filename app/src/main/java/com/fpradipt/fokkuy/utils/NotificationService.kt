package com.fpradipt.fokkuy.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fpradipt.fokkuy.MainActivity
import com.fpradipt.fokkuy.R
import com.fpradipt.fokkuy.receiver.TimerNotificationReceiver
import java.text.SimpleDateFormat
import java.util.*

class NotificationService {
    companion object {
        private const val CHANNEL_ID = "fokkuy"
        private const val CHANNEL_NAME = "Fokkuy Timer"
        private const val TIMER_ID = 0

        fun showTimerStop(context: Context) {
            val intent = Intent(context, TimerNotificationReceiver::class.java)
            intent.action = MainActivity.ACTION_START

            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val notifBuilder = getNotificationBuilder(context, CHANNEL_ID, true)
            notifBuilder.setContentTitle("Time to break!")
                .setContentText("Start again")
                .setContentIntent(getIntentStack(context, MainActivity::class.java))
                .addAction(R.drawable.ic_play_arrow_white_24dp, "Start", pendingIntent)
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.createNotificationChannel(CHANNEL_ID, CHANNEL_NAME, true)
            notifManager.notify(TIMER_ID, notifBuilder.build())
        }

        fun showTimerRunning(context: Context, time: Long) {
            val intentStop = Intent(context, TimerNotificationReceiver::class.java)
            intentStop.action = MainActivity.ACTION_STOP
            val stopPendingIntent = PendingIntent.getBroadcast(context, 0, intentStop , PendingIntent.FLAG_UPDATE_CURRENT)

            val intentPause = Intent(context, TimerNotificationReceiver::class.java)
            intentPause.action = MainActivity.ACTION_PAUSE
            val pausePendingIntent =
                PendingIntent.getBroadcast(context, 0, intentPause, PendingIntent.FLAG_UPDATE_CURRENT)

            val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
            val notifBuilder = getNotificationBuilder(context, CHANNEL_ID, true)
            notifBuilder.setContentTitle("Fokkuy is running")
                .setContentText("End: ${df.format(Date(time))}")
                .setContentIntent(getIntentStack(context, MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_pause_white_24dp, "Pause", pausePendingIntent)
                .addAction(R.drawable.ic_history_white_24dp, "Reset", stopPendingIntent)
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.createNotificationChannel(CHANNEL_ID, CHANNEL_NAME, true)
            notifManager.notify(TIMER_ID, notifBuilder.build())
        }

        fun showTimerPause(context: Context) {
            val intentStop = Intent(context, TimerNotificationReceiver::class.java)
            intentStop.action = MainActivity.ACTION_STOP
            val stopPendingIntent = PendingIntent.getBroadcast(context, 0, intentStop , PendingIntent.FLAG_UPDATE_CURRENT)

            val intentResume = Intent(context, TimerNotificationReceiver::class.java)
            intentResume.action = MainActivity.ACTION_RESUME
            val resumePendingIntent =
                PendingIntent.getBroadcast(context, 0, intentResume, PendingIntent.FLAG_UPDATE_CURRENT)

            val notifBuilder = getNotificationBuilder(context, CHANNEL_ID, true)
            notifBuilder.setContentTitle("Fokkuy is paused")
                .setContentText("Paused")
                .setContentIntent(getIntentStack(context, MainActivity::class.java))
                .addAction(R.drawable.ic_play_arrow_white_24dp, "Resume", resumePendingIntent)
                .addAction(R.drawable.ic_history_white_24dp, "Reset", stopPendingIntent)
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.createNotificationChannel(CHANNEL_ID, CHANNEL_NAME, true)
            notifManager.notify(TIMER_ID, notifBuilder.build())
        }

        private fun NotificationManager.createNotificationChannel(channelId: String, channelName: String, playSound: Boolean) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val importance = if(playSound) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_LOW
                val notifChannel = NotificationChannel(channelId, channelName, importance)
                notifChannel.lightColor = Color.WHITE
                this.createNotificationChannel(notifChannel)
            }
        }

        private fun getNotificationBuilder(
            context: Context,
            channelId: String,
            playSound: Boolean
        ): NotificationCompat.Builder {
            val notificationSoundUri: Uri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_alarm_white_24dp)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
            if (playSound)
                builder.setSound(notificationSoundUri)
            return builder
        }

        fun hideNotification(context: Context){
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.cancel(TIMER_ID)
        }

        private fun <T> getIntentStack(context: Context, javaClass: Class<T>): PendingIntent {
            val resIntent = Intent(context, javaClass)
            //Set flag for intent. Not launch when already running and close the other top activity layer
            resIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resIntent)

            return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}