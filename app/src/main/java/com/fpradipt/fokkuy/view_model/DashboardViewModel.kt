package com.fpradipt.fokkuy.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fpradipt.fokkuy.db.TimerUsageDao
import com.fpradipt.fokkuy.model.UsageModel
import com.fpradipt.fokkuy.utils.CustomValueFormatter
import com.fpradipt.fokkuy.utils.formatDataChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.*

class DashboardViewModel(
    private val database: TimerUsageDao,
    application: Application
) : AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _histories = database.getHistory()
    private lateinit var all: List<UsageModel>
    var entries = ArrayList<BarEntry>()
    var xAxisLabel = ArrayList<String>()

    init {
//        parsingData()
    }

    private fun parsingData() {
        uiScope.launch {
            getAllData()
//            Transformations.map(_histories) { hist ->
//                entries = formatDataChart(hist)
//            }
            all.forEach { entry ->
                entries.add(BarEntry(entry.timerId.toFloat(), entry.duration.toFloat()))
//                Log.d("ENTRIES", entries.toString())
            }
        }

        Log.d("ENTRIES", entries.toString())
    }

    private suspend fun getAllData(){
        return withContext(Dispatchers.IO){
            all = database.getAll()
        }
    }

    fun getBarData(): BarData {
        all = database.getAll()
        all.forEach { entry ->
            entries.add(BarEntry(entry.timerId.toFloat(), entry.duration.toFloat()))
            xAxisLabel.add(entry.timerId.toString())
        }
        val set = BarDataSet(entries, "Duration Time (Seconds)")

        val data = BarData(set)
        data.setValueFormatter(CustomValueFormatter(getXLabel()))
        data.barWidth = 0.9f
        return data
    }

    fun getXLabel(): Array<Any> {
        Log.d("ARRAYLIST", arrayOf('1', '2', '3').toString())
        return xAxisLabel.toArray()
    }


}