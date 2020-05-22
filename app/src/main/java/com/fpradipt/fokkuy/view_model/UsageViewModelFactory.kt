package com.fpradipt.fokkuy.view_model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fpradipt.fokkuy.db.TimerUsageDao
import com.google.firebase.auth.FirebaseAuth

class UsageViewModelFactory(
    private val dataSource: TimerUsageDao,
    private val application: Application,
    private val auth: FirebaseAuth
): ViewModelProvider.Factory
{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UsageViewModel::class.java)){
            return UsageViewModel(dataSource, application, auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}