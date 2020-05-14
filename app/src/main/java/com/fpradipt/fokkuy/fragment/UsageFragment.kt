package com.fpradipt.fokkuy.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.firebase.ui.auth.AuthUI

import com.fpradipt.fokkuy.R
import com.fpradipt.fokkuy.databinding.FragmentUsageBinding
import com.fpradipt.fokkuy.db.TimerUsageDatabase
import com.fpradipt.fokkuy.view_model.UsageViewModel
import com.fpradipt.fokkuy.view_model.UsageViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.InternalCoroutinesApi

/**
 * A simple [Fragment] subclass.
 */
class UsageFragment : Fragment() {
    private lateinit var viewModel: UsageViewModel
    private lateinit var binding: FragmentUsageBinding
    private lateinit var auth: FirebaseAuth
    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_usage, container, false
        )
        binding.lifecycleOwner = this

        checkAuth()
        val application = requireNotNull(this.activity).application
        val dataSource = TimerUsageDatabase.getInstance(application).timerUsageDatabaseDao
        val viewModelFactory = UsageViewModelFactory(dataSource, application)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UsageViewModel::class.java)

        binding.usageViewModel = viewModel

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
                ).build(), DashboardFragment.SIGN_IN_RESULT_CODE
            )
        }
    }
}
