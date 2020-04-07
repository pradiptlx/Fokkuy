package com.fpradipt.fokkuy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.fpradipt.fokkuy.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil
import com.fpradipt.fokkuy.utils.PrefUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0L
    private var timerState = TimerState.Stopped
    private var secondsRemaining: Long = 0L
    private var isOpen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startTimerButton.visibility = View.INVISIBLE
        pauseTimerButton.visibility = View.INVISIBLE
        resetTimerButton.visibility = View.INVISIBLE

        // Animation
        val fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        val fabRClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)
        val fabRAntiClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise)

        // FAB
        fabButton.setOnClickListener {
            isOpen = if (isOpen) {
                fabButton.startAnimation(fabRClockwise)

                startTimerButton.visibility = View.INVISIBLE
                pauseTimerButton.visibility = View.INVISIBLE
                resetTimerButton.visibility = View.INVISIBLE

                false
            } else {
                fabButton.startAnimation(fabRAntiClockwise)

                startTimerButton.visibility = View.VISIBLE
                pauseTimerButton.visibility = View.VISIBLE
                resetTimerButton.visibility = View.VISIBLE

                true
            }
        }

        startTimerButton.setOnClickListener(this)
        pauseTimerButton.setOnClickListener(this)
        resetTimerButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.startTimerButton -> {
                    timerState = TimerState.Running
                    startTimer()
                    updateButtons()
                }

                R.id.pauseTimerButton -> {
                    timer.cancel()
                    timerState = TimerState.Paused
                    updateButtons()
                }

                R.id.resetTimerButton -> {
//                    timerState = TimerState.Stopped
                    timer.cancel()
                    onTimerFinished()
                }
            }
        }
    }

    private fun initTimer() {
        timerState = PrefUtils.getTimerState(this)

        if (timerState === TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining =
            if (timerState === TimerState.Running || timerState === TimerState.Paused)
                PrefUtils.getSecondsRemaining(this)
            else
                timerLengthSeconds


    }

    private fun startTimer() {
        timerState = TimerState.Running

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() {
                onTimerFinished()
            }

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes = PrefUtils.getTimerLength(this)
        timerLengthSeconds = lengthInMinutes * 60L
        progressCountdown.max = timerLengthSeconds.toInt()
    }

    override fun onResume() {
        super.onResume()

        initTimer()
    }

    override fun onPause() {
        super.onPause()

        if (timerState === TimerState.Running) {
            timer.cancel()
        } else if (timerState === TimerState.Paused) {

        }

        PrefUtils.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtils.setSecondsRemaining(secondsRemaining, this)
        PrefUtils.setTimerState(timerState, this)
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtils.getPreviousTimerLengthSeconds(this)
        progressCountdown.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        var stringTimer: String = getString(R.string.timer_countdown)
        timerCountdown.text =
            "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        progressCountdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons(){
        when(timerState){
            TimerState.Running -> {
                startTimerButton.isEnabled = false
                pauseTimerButton.isEnabled = true
                resetTimerButton.isEnabled = true
            }
            TimerState.Paused -> {
                startTimerButton.isEnabled = true
                pauseTimerButton.isEnabled = false
                resetTimerButton.isEnabled = true
            }
            TimerState.Stopped -> {
                startTimerButton.isEnabled = true
                pauseTimerButton.isEnabled = true
                resetTimerButton.isEnabled = false
            }
        }
    }

    private fun onTimerFinished() {
        timerState = TimerState.Stopped

        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running
        setNewTimerLength()

        progressCountdown.progress = 0

        PrefUtils.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun startLogin() {
        val login = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(login)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_timer, menu)

        return true
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when(item.id) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

}
