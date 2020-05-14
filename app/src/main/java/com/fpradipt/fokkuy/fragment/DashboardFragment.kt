package com.fpradipt.fokkuy.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse

import com.fpradipt.fokkuy.R
import com.fpradipt.fokkuy.databinding.FragmentDashboardBinding
import com.fpradipt.fokkuy.db.TimerUsageDatabase
import com.fpradipt.fokkuy.utils.CustomValueFormatter
import com.fpradipt.fokkuy.view_model.DashboardViewModel
import com.fpradipt.fokkuy.view_model.DashboardViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.InternalCoroutinesApi

class DashboardFragment : Fragment() {
    private lateinit var viewModel: DashboardViewModel
    private lateinit var auth: FirebaseAuth
    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentDashboardBinding>(inflater, R.layout.fragment_dashboard, container, false)
        binding.lifecycleOwner = this

        checkAuth()
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

        binding.emailUser.text = auth.currentUser?.email
        binding.fullname.text = auth.currentUser?.displayName

        return binding.root
    }

    private fun checkAuth(){
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser == null){
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                    providers
                ).build(), SIGN_IN_RESULT_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            Log.d("EMAIL", response!!.email.toString())
            Log.d("RESPONSE", response.toString())
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    companion object{
        const val TAG = "DASHBOARD"
        const val SIGN_IN_RESULT_CODE = 1001
    }

}
