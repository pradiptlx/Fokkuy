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

        viewModel = ViewModelProvider(this, viewModelFactory).get(UsageViewModel::class.java)

        binding.usageViewModel = viewModel

        return binding.root
    }

    private fun checkAuth(){
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser == null){
            binding.scrollView2.visibility = View.GONE
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                    providers
                ).build(), DashboardFragment.SIGN_IN_RESULT_CODE
            )
        }else{
            binding.scrollView2.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                binding.scrollView2.visibility = View.VISIBLE
                // User successfully signed in
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if (response != null) {
                    Log.i(DashboardFragment.TAG, "Sign in unsuccessful ${response.error?.errorCode}")
                }
                Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object{
        const val TAG = "HISTORY"
        const val SIGN_IN_RESULT_CODE = 1001
    }
}
