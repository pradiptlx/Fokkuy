package com.fpradipt.fokkuy.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide

import com.fpradipt.fokkuy.R
import com.fpradipt.fokkuy.databinding.FragmentAboutBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass.
 */
class AboutFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentAboutBinding>(inflater,
            R.layout.fragment_about, container, false)

        auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null){
            Glide.with(this)
                .load(auth.currentUser?.photoUrl)
                .fitCenter()
                .into(binding.imageView);
        }

        return binding.root
    }

}
