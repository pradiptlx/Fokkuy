package com.fpradipt.fokkuy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.fpradipt.fokkuy.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        loginStart.setOnClickListener { startLogin() }
    }

    private fun startLogin() {
        val login = Intent(this, LoginActivity::class.java)
        startActivity(login)
    }

//    override fun onClick(v: View?) {
//        if (v != null) {
//            when(v.id) {
//                R.id.loginStart -> {
//                    Toast.makeText(applicationContext,"TE", Toast.LENGTH_LONG).show()
//                    startLogin()
//                }
//
//            }
//        }
//    }

}
