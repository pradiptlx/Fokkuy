package com.fpradipt.fokkuy.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.agilie.circularpicker.presenter.CircularPickerContract

import com.fpradipt.fokkuy.R
import com.fpradipt.fokkuy.TimerState
import com.fpradipt.fokkuy.databinding.FragmentHomeBinding
import com.fpradipt.fokkuy.db.TimerUsageDatabase
import com.fpradipt.fokkuy.receiver.TimerExpiredReceiver
import com.fpradipt.fokkuy.utils.NotificationService
import com.fpradipt.fokkuy.utils.PrefUtils
import com.fpradipt.fokkuy.view_model.UsageViewModel
import com.fpradipt.fokkuy.view_model.UsageViewModelFactory
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.InternalCoroutinesApi
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment(), View.OnClickListener {
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

    private lateinit var binding: FragmentHomeBinding

    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        val app = requireNotNull(this.activity).application
        val dataSource = TimerUsageDatabase.getInstance(app).timerUsageDatabaseDao

        val usageViewModelFactory = UsageViewModelFactory(dataSource, app)
        val usageViewModel = ViewModelProviders.of(this, usageViewModelFactory)
            .get(UsageViewModel::class.java)
        binding.lifecycleOwner = this
        binding.usageViewModel = usageViewModel

        binding.startTimerButton.visibility = View.INVISIBLE
        binding.pauseTimerButton.visibility = View.INVISIBLE
        binding.resetTimerButton.visibility = View.INVISIBLE


        // Animation
        /*val fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)*/
        val fabRClockwise = AnimationUtils.loadAnimation(activity, R.anim.rotate_clockwise)
        val fabRAntiClockwise = AnimationUtils.loadAnimation(activity, R.anim.rotate_anticlockwise)
        // FAB
        binding.fabButton.setOnClickListener {
            isOpen = if (isOpen) {
                binding.fabButton.startAnimation(fabRClockwise)

                binding.startTimerButton.visibility = View.INVISIBLE
                binding.pauseTimerButton.visibility = View.INVISIBLE
                binding.resetTimerButton.visibility = View.INVISIBLE

                false
            } else {
                binding.fabButton.startAnimation(fabRAntiClockwise)

                binding.startTimerButton.visibility = View.VISIBLE
                binding.pauseTimerButton.visibility = View.VISIBLE
                binding.resetTimerButton.visibility = View.VISIBLE

                true
            }
        }

        binding.startTimerButton.setOnClickListener(this)
        binding.pauseTimerButton.setOnClickListener(this)
        binding.resetTimerButton.setOnClickListener(this)


        binding.circularPicker.apply {
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
                    if (timerState === TimerState.Stopped) {
                        binding.timerCountdown.text =
                            "${value.toString()}:00"
                        secondsRemaining = value * 60L
//                        PrefUtils.setTimerLength(value, context)
                    } else
                        binding.timerCountdown.text =
                            "${(timerLengthSeconds / 60).toString()}:00"
                    PrefUtils.setTimerLength(value, requireContext())

                }
            })
            colorChangedListener = (object : CircularPickerContract.Behavior.ColorChangedListener {
                override fun onColorChanged(r: Int, g: Int, b: Int) {
//                    timerCountdown.setHintTextColor(Color.rgb(r, g, b))
                }
            })
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.navbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        )
                || super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.startTimerButton -> {
                    timerState = TimerState.Running
                    timerLengthSeconds =
                        PrefUtils.getTimerLength(requireActivity().applicationContext) * 60L
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
    }

    private fun resumeTimer() {
        timerState = context?.let { PrefUtils.getTimerState(it) }!!

        if (timerState === TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining =
            if (timerState === TimerState.Running || timerState === TimerState.Paused)
                PrefUtils.getSecondsRemaining(requireContext().applicationContext)
            else
                timerLengthSeconds

        val alarmTime = PrefUtils.getAlarmTime(requireContext().applicationContext)
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
        binding.usageViewModel!!.onStartTimer()

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
        val lengthInMinutes = PrefUtils.getTimerLength(requireContext().applicationContext)
        timerLengthSeconds = lengthInMinutes * 60L
        progressCountdown.max = timerLengthSeconds.toInt()
    }

    override fun onResume() {
        super.onResume()

        // Start again
        resumeTimer()

        // If activity shows again, remove broadcast intent
        removeAlarm(requireContext().applicationContext)
        NotificationService.hideNotification(requireContext().applicationContext)

    }

    override fun onPause() {
        super.onPause()

        if (timerState === TimerState.Running) {
            timer.cancel()
            val wakeUpTime =
                setAlarm(requireContext().applicationContext, nowSeconds, secondsRemaining)
            NotificationService.showTimerRunning(requireContext().applicationContext, wakeUpTime)
        } else if (timerState === TimerState.Paused) {
            NotificationService.showTimerPause(requireContext().applicationContext)
        }

        PrefUtils.setPreviousTimerLengthSeconds(
            timerLengthSeconds,
            requireContext().applicationContext
        )
        PrefUtils.setSecondsRemaining(secondsRemaining, requireContext().applicationContext)
        PrefUtils.setTimerState(timerState, requireContext().applicationContext)
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds =
            PrefUtils.getPreviousTimerLengthSeconds(requireContext().applicationContext)
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
            currentValue = PrefUtils.getTimerLength(context)
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

        val timerUiLength = context?.let { PrefUtils.getTimerLength(it) }
//        Toast.makeText(context, (timerUiLength).toString(), Toast.LENGTH_SHORT).show()

        context?.let {
            if (timerUiLength != null) {
                PrefUtils.setSecondsRemaining(timerUiLength * 60L, it)
            }
        }
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
        binding.usageViewModel!!.onStopTimer()
    }

}
