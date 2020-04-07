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
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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
