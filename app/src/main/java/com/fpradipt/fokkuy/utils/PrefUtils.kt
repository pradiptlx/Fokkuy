package com.fpradipt.fokkuy.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.fpradipt.fokkuy.TimerState

class PrefUtils {

    companion object {
        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID =
            "com.fpradipt.fokkuy.previous_timer_length_seconds"

        private const val TIMER_VALUE = "timer_value"

        fun setTimerLength(minutes:Int, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt(TIMER_VALUE, minutes).apply()
        }

        fun getTimerLength(context: Context): Int {
            val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)
            return preferenceManager.getInt(TIMER_VALUE, 1)
        }

        fun getPreviousTimerLengthSeconds(context: Context): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
        }

        fun setPreviousTimerLengthSeconds(seconds: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
            editor.apply()
        }

        private const val TIMER_STATE_ID = "com.fpradipt.fokkuy.timer_state"

        fun getTimerState(context: Context): TimerState {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val pos = preferences.getInt(TIMER_STATE_ID, 0)
            return TimerState.values()[pos]
        }

        fun setTimerState(state: TimerState, context:Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val pos = state.ordinal
            editor.putInt(TIMER_STATE_ID, pos)
            editor.apply()
        }

        private const val SECONDS_REMAINING = "com.fpradipt.fokkuy.seconds_remaining"

        fun getSecondsRemaining(context: Context): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING, 0)
        }

        fun setSecondsRemaining(seconds: Long, context:Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING, seconds)
            editor.apply()
        }

        private const val TIMER_BACKGROUND_ID = "com.fpradipt.fokkuy.timer_background"

        fun getAlarmTime(context: Context): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(TIMER_BACKGROUND_ID, 0)
        }

        fun setAlarmTime(time: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context)
            editor.edit().putLong(TIMER_BACKGROUND_ID, time).apply()
        }
    }

}