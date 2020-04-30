package com.fpradipt.fokkuy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fpradipt.fokkuy.fragment.HomeFragment
import com.fpradipt.fokkuy.TimerState
import com.fpradipt.fokkuy.utils.NotificationService
import com.fpradipt.fokkuy.utils.PrefUtils

class TimerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            HomeFragment.ACTION_STOP -> {
                HomeFragment.removeAlarm(context)
                PrefUtils.setTimerState(TimerState.Stopped, context)

                NotificationService.showTimerStop(context)
            }

            HomeFragment.ACTION_PAUSE -> {
                var secondsRemaining = PrefUtils.getSecondsRemaining(context)
                val alarmTime = PrefUtils.getAlarmTime(context)
                val nowSeconds =
                    HomeFragment.nowSeconds

                secondsRemaining -= nowSeconds - alarmTime
                PrefUtils.setSecondsRemaining(secondsRemaining, context)
                HomeFragment.removeAlarm(context)

                PrefUtils.setTimerState(TimerState.Paused, context)
                NotificationService.showTimerPause(context)
            }

            HomeFragment.ACTION_RESUME -> {
                val secondsRemaining = PrefUtils.getSecondsRemaining(context)
                val wakeUpTime =
                    HomeFragment.setAlarm(
                        context,
                        HomeFragment.nowSeconds,
                        secondsRemaining
                    )
                PrefUtils.setTimerState(TimerState.Running, context)
                NotificationService.showTimerRunning(context, wakeUpTime)
            }

            HomeFragment.ACTION_START -> {
                val time = PrefUtils.getTimerLength(context)
                val secondsRemaining = time * 60L
                val wakeUpTime =
                    HomeFragment.setAlarm(
                        context,
                        HomeFragment.nowSeconds,
                        secondsRemaining
                    )
                NotificationService.showTimerRunning(context, wakeUpTime)
            }
        }
    }
}
