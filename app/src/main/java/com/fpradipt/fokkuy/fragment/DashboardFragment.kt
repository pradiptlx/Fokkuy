package com.fpradipt.fokkuy.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse

import com.fpradipt.fokkuy.R
import com.fpradipt.fokkuy.databinding.FragmentDashboardBinding
import com.fpradipt.fokkuy.db.TimerUsageDatabase
import com.fpradipt.fokkuy.view_model.DashboardViewModel
import com.fpradipt.fokkuy.view_model.DashboardViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.InternalCoroutinesApi

class DashboardFragment : Fragment() {
    private lateinit var viewModel: DashboardViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentDashboardBinding
    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.lifecycleOwner = this

        checkAuth()
        val application = requireNotNull(this.activity).application
        val dataSource = TimerUsageDatabase.getInstance(application).timerUsageDatabaseDao
        val viewModelFactory = DashboardViewModelFactory(dataSource, application)

        viewModel = ViewModelProvider(this, viewModelFactory).get(DashboardViewModel::class.java)
        binding.dashboardViewModel = viewModel

        val chart = binding.chart
        chart.data = viewModel.getBarData()

//        chart.xAxis.valueFormatter = CustomValueFormatter(viewModel.getXLabel())
        chart.setFitBars(true)
        chart.invalidate()

        binding.signOutButton.setOnClickListener {
            onSignOut()
        }
        Log.d("AUTH", auth.currentUser?.photoUrl.toString())

        return binding.root
    }

    private fun checkAuth(){
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser == null){
            binding.signOutButton.visibility = View.GONE
            binding.fullname.text = "Your Name"
            binding.emailUser.text = "Your Email"
            binding.chart.visibility = View.GONE

            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                    providers
                ).build(), SIGN_IN_RESULT_CODE
            )
        }else{
            Glide.with(this)
                .load(auth.currentUser?.photoUrl)
                .fitCenter()
                .into(binding.imageView);
            binding.emailUser.text = auth.currentUser?.email
            binding.fullname.text = auth.currentUser?.displayName
        }
    }

    private fun onSignOut(){
        auth.signOut()

        this.findNavController().navigate(R.id.action_dashboardFragment_to_homeApp)

        Toast.makeText(context, "Successfully sign out", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            Log.d("EMAIL", response!!.email.toString())
            Log.d("RESPONSE", response.toString())
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(this)
                    .load(auth.currentUser?.photoUrl)
                    .fitCenter()
                    .into(binding.imageView);
                binding.plotText.visibility = View.VISIBLE
                binding.chart.visibility = View.VISIBLE
                binding.emailUser.text = auth.currentUser?.email
                binding.fullname.text = auth.currentUser?.displayName
                binding.signOutButton.visibility = View.VISIBLE
                // User successfully signed in
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response.error?.errorCode}")
                this.findNavController().navigate(R.id.action_dashboardFragment_to_homeApp)
                Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object{
        const val TAG = "DASHBOARD"
        const val SIGN_IN_RESULT_CODE = 1001
    }

}
