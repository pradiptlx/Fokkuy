package com.fpradipt.fokkuy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fpradipt.fokkuy.MainActivity
import com.fpradipt.fokkuy.TimerState
import com.fpradipt.fokkuy.utils.NotificationService
import com.fpradipt.fokkuy.utils.PrefUtils

class TimerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MainActivity.ACTION_STOP -> {
                MainActivity.removeAlarm(context)
                PrefUtils.setTimerState(TimerState.Stopped, context)

                NotificationService.showTimerStop(context)
            }

            MainActivity.ACTION_PAUSE -> {
                var secondsRemaining = PrefUtils.getSecondsRemaining(context)
                val alarmTime = PrefUtils.getAlarmTime(context)
                val nowSeconds =
                    MainActivity.nowSeconds

                secondsRemaining -= nowSeconds - alarmTime
                PrefUtils.setSecondsRemaining(secondsRemaining, context)
                MainActivity.removeAlarm(context)

                PrefUtils.setTimerState(TimerState.Paused, context)
                NotificationService.showTimerPause(context)
            }

            MainActivity.ACTION_RESUME -> {
                val secondsRemaining = PrefUtils.getSecondsRemaining(context)
                val wakeUpTime =
                    MainActivity.setAlarm(
                        context,
                        MainActivity.nowSeconds,
                        secondsRemaining
                    )
                PrefUtils.setTimerState(TimerState.Running, context)
                NotificationService.showTimerRunning(context, wakeUpTime)
            }

            MainActivity.ACTION_START -> {
                val time = PrefUtils.getTimerLength(context)
                val secondsRemaining = time * 60L
                val wakeUpTime =
                    MainActivity.setAlarm(
                        context,
                        MainActivity.nowSeconds,
                        secondsRemaining
                    )
                NotificationService.showTimerRunning(context, wakeUpTime)
            }
        }
    }
}
