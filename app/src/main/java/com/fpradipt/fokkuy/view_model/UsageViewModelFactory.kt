package com.fpradipt.fokkuy.view_model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fpradipt.fokkuy.db.TimerUsageDao

class UsageViewModelFactory(
    private val dataSource: TimerUsageDao,
    private val application: Application
): ViewModelProvider.Factory
{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UsageViewModel::class.java)){
            return UsageViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}