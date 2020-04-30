package com.fpradipt.fokkuy

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.agilie.circularpicker.presenter.CircularPickerContract
import com.fpradipt.fokkuy.databinding.ActivityMainBinding
import com.fpradipt.fokkuy.receiver.TimerExpiredReceiver
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

class MainActivity : AppCompatActivity() {

    companion object {
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

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        drawerLayout = binding.drawerLayout

        val navController = this.findNavController(R.id.navHostFragment)

        NavigationUI.setupActionBarWithNavController(this,navController, drawerLayout)

        NavigationUI.setupWithNavController(binding.navView, navController)

        // GSO
        configureGSO()

        /*startTimerButton.visibility = View.INVISIBLE
        pauseTimerButton.visibility = View.INVISIBLE
        resetTimerButton.visibility = View.INVISIBLE

        // Animation
        *//*val fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)*//*
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

        circularPicker.apply {
            colors = (intArrayOf(
                Color.parseColor("#00EDE9"),
                Color.parseColor("#0087D9"),
                Color.parseColor("#8A1CC3")
            ))

            gradientAngle = 220
            maxLapCount = 1
            currentValue = 1
            maxValue = 60
            centeredTextSize = 60f

            valueChangedListener = (object : CircularPickerContract.Behavior.ValueChangedListener {
                override fun onValueChanged(value: Int) {
                    Log.d("COUNT", value.toString())
                    if (timerState === TimerState.Stopped){
                        timerCountdown.text =
                            "${value.toString()}:00"
                        secondsRemaining = value * 60L
//                        PrefUtils.setTimerLength(value, this@MainActivity)
                    }
                    else
                        timerCountdown.text =
                            "${(timerLengthSeconds / 60).toString()}:00"
                    PrefUtils.setTimerLength(value, this@MainActivity)

                }
            })
            colorChangedListener = (object : CircularPickerContract.Behavior.ColorChangedListener {
                override fun onColorChanged(r: Int, g: Int, b: Int) {
//                    timerCountdown.setHintTextColor(Color.rgb(r, g, b))
                }
            })
        }*/
//        gsoButton.setOnClickListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.navView)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }


    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(getLaunchIntent(this))
            finish()
        }

    }

    /*override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.startTimerButton -> {
                    timerState = TimerState.Running
                    timerLengthSeconds = PrefUtils.getTimerLength(this) * 60L
                    startTimer()
                    updateButtons()
                }

                R.id.pauseTimerButton -> {
                    timer.cancel()
                    timerState = TimerState.Paused
                    updateButtons()
                }

                R.id.resetTimerButton -> {
                    timer.cancel()
                    onTimerFinished()
                }
            }
        }
    }*/

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
        startActivityForResult(
            signInIntent,
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

   /* private fun resumeTimer() {
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
        if (alarmTime > 0) {
            val afterPauseTime = nowSeconds - alarmTime
            secondsRemaining -= afterPauseTime
        }

        if (secondsRemaining <= 0)
            onTimerFinished()
        else if (timerState == TimerState.Running)
            startTimer()

        updateCountdownUI()
        updateButtons()
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
        resumeTimer()

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
        Log.d("minutesUntilFinished", minutesUntilFinished.toString())
        Log.d("timerLengthSeconds", timerLengthSeconds.toString())
        val secondsInMinuteUntilFinished =
            secondsRemaining - minutesUntilFinished * 60 // If minutes > 0, secondsInMinutesUntilFinished !== secondsRemaining
        val secondsStr = secondsInMinuteUntilFinished.toString()
        timerCountdown.text =
            "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        Log.d("countdown", (timerLengthSeconds - secondsRemaining).toString())
        progressCountdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
        circularPicker.apply {
            currentValue = PrefUtils.getTimerLength(this@MainActivity)
        }
    }

    private fun updateButtons() {
        when (timerState) {
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
                pauseTimerButton.isEnabled = false
                resetTimerButton.isEnabled = false
            }
        }
    }

    private fun onTimerFinished() {
        timerState = TimerState.Stopped

        setNewTimerLength()

        progressCountdown.progress = 0

        val timerUiLength = PrefUtils.getTimerLength(this)
//        Toast.makeText(this, (timerUiLength).toString(), Toast.LENGTH_SHORT).show()

        PrefUtils.setSecondsRemaining(timerUiLength * 60L, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun startLogin() {
        val login = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(login)
    }*/

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_timer, menu)
//
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when(item.id) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

}
