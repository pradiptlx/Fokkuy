package com.fpradipt.fokkuy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fpradipt.fokkuy.utils.PrefUtils

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        PrefUtils.setTimerState(TimerState.Stopped, context)
        PrefUtils.setAlarmTime(0, context)
    }
}
