package com.fpradipt.fokkuy.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.fpradipt.fokkuy.db.TimerUsageDao
import com.fpradipt.fokkuy.model.UsageModel
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DashboardViewModel(
    database: TimerUsageDao,
    application: Application
) : AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _histories = database.getHistory()
    val histories: LiveData<List<UsageModel>>
        get() = _histories

    private var entries = ArrayList<BarEntry>()

    private fun parsingData(){

    }


}