package com.fpradipt.fokkuy

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import com.fpradipt.fokkuy.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil
import com.fpradipt.fokkuy.utils.NotificationService
import com.fpradipt.fokkuy.utils.PrefUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpMs = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpMs, pendingIntent)
            PrefUtils.setAlarmTime(nowSeconds, context)
            return wakeUpMs
        }

        fun removeAlarm(context: Context) {
            val alarmIntent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtils.setAlarmTime(0, context) // Reset time
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000

        const val GSO_ID = "GSO"
        const val RC_SIGN_IN: Int = 1
        fun getLaunchIntent(from: Context) = Intent(from, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        const val ACTION_STOP = "stop"
        const val ACTION_START = "start"
        const val ACTION_PAUSE = "pause"
        const val ACTION_RESUME = "resume"
    }

    private lateinit var timer: CountDownTimer
    private var timerState = TimerState.Stopped
    private var timerLengthSeconds: Long = 0L
    private var secondsRemaining: Long = 0L
    private var isOpen: Boolean = false

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // GSO
        configureGSO()

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

        simpleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                Toast.makeText(this@MainActivity, progress.toString(), Toast.LENGTH_SHORT).show()
                PrefUtils.setTimerLength(progress, this@MainActivity)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Toast.makeText(applicationContext, "Change Timer", Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    Toast.makeText(applicationContext, seekBar.progress.toString(), Toast.LENGTH_SHORT).show()
                    PrefUtils.setTimerLength(seekBar.progress, this@MainActivity)
                }
            }
        })
        startTimerButton.setOnClickListener(this)
        pauseTimerButton.setOnClickListener(this)
        resetTimerButton.setOnClickListener(this)
//        gsoButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null){
            startActivity(getLaunchIntent(this))
            finish()
        }

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

//                R.id.gsoButton -> {
//                    signIn()
//                }
            }
        }
    }

    private fun configureGSO() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent,
            RC_SIGN_IN
        )
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(GSO_ID, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(GSO_ID, "signInWithCredential:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(GSO_ID, "signInWithCredential:failure", task.exception)
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

        val alarmTime = PrefUtils.getAlarmTime(this)
        if (alarmTime > 0){
            val afterPauseTime = nowSeconds - alarmTime
            secondsRemaining -= nowSeconds - alarmTime
        }

        if(secondsRemaining <= 0)
            onTimerFinished()
        else if(timerState == TimerState.Running)
            startTimer()

//        updateCountdownUI()
//        updateButtons()
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

        // Start again
        initTimer()

        // If activity shows again, remove broadcast intent
        removeAlarm(this)
        NotificationService.hideNotification(this)

    }

    override fun onPause() {
        super.onPause()

        if (timerState === TimerState.Running) {
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            NotificationService.showTimerRunning(this, wakeUpTime)
        } else if (timerState === TimerState.Paused) {
            NotificationService.showTimerPause(this)
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
        val minutesUntilFinished = secondsRemaining / 60 // Convert to minute
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60 // If minutes > 0, secondsInMinutesUntilFinished !== secondsRemaining
        val secondsStr = secondsInMinuteUntilFinished.toString()
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
