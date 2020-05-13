package com.fpradipt.fokkuy.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders

import com.fpradipt.fokkuy.R
import com.fpradipt.fokkuy.databinding.FragmentDashboardBinding
import com.fpradipt.fokkuy.db.TimerUsageDatabase
import com.fpradipt.fokkuy.utils.CustomValueFormatter
import com.fpradipt.fokkuy.view_model.DashboardViewModel
import com.fpradipt.fokkuy.view_model.DashboardViewModelFactory
import kotlinx.coroutines.InternalCoroutinesApi

class DashboardFragment : Fragment() {
    private lateinit var viewModel: DashboardViewModel

    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentDashboardBinding>(inflater, R.layout.fragment_dashboard, container, false)
        binding.lifecycleOwner = this

        val application = requireNotNull(this.activity).application
        val dataSource = TimerUsageDatabase.getInstance(application).timerUsageDatabaseDao
        val viewModelFactory = DashboardViewModelFactory(dataSource, application)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DashboardViewModel::class.java)
        binding.dashboardViewModel = viewModel

        val chart = binding.chart
        chart.data = viewModel.getBarData()

//        chart.xAxis.valueFormatter = CustomValueFormatter(viewModel.getXLabel())
        chart.setFitBars(true)
        chart.invalidate()

        return binding.root
    }

}
