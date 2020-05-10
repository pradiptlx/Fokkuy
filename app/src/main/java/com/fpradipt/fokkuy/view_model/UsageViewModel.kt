package com.fpradipt.fokkuy.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fpradipt.fokkuy.db.TimerUsageDao
import com.fpradipt.fokkuy.model.UsageModel
import com.fpradipt.fokkuy.utils.formatLog
import kotlinx.coroutines.*
import java.util.*

class UsageViewModel(
    private val database: TimerUsageDao,
    application: Application
) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val _histories = database.getHistory()
    val histories: LiveData<List<UsageModel>>
        get() = _histories

    val parsedHist = Transformations.map(_histories) { history ->
        formatLog(history, application.resources)
    }

    private var logUsage = MutableLiveData<UsageModel?>()

    init {
        initTimer()
    }

    private fun initTimer() {
        uiScope.launch {
            logUsage.value = getCurrentData()
            Log.d("INIT", logUsage.value.toString())
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onClear() {
        uiScope.launch {
            clearHist()
            Log.d("Clear", logUsage.value.toString())
        }
    }

    fun onStartTimer() {
        uiScope.launch {
            val logInit = UsageModel()
            logInit.startTimer = System.currentTimeMillis()
            insertHist(logInit)
            Log.d("START", logInit.toString())
            logUsage.value = getCurrentData()
        }
    }

    fun onStopTimer() {
        uiScope.launch {
            val oldLog = logUsage.value ?: return@launch
            oldLog.endTimer = System.currentTimeMillis()
            oldLog.createdAt = Date().toString()
            updateHist(oldLog)
            Log.d("STOP", oldLog.toString())
        }
    }

    private suspend fun getCurrentData(): UsageModel? {
        return withContext(Dispatchers.IO) {
            var log = database.getCurrent()
            Log.d("CURRENT", log.toString())
//            if (log?.endTimer != log?.startTimer) {
//                log = null
//            }

            log
        }
    }

    private suspend fun insertHist(log: UsageModel) {
        withContext(Dispatchers.IO) {
            database.insert(log)
        }
    }

    private suspend fun updateHist(log: UsageModel) {
        withContext(Dispatchers.IO) {
            database.update(log)
        }
    }

    private suspend fun clearHist() {
        withContext(Dispatchers.IO) {
            database.clearHistory()
        }
    }


}