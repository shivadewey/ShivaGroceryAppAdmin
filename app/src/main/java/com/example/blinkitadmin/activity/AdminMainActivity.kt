package com.example.blinkitadmin.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.blinkitadmin.R
import com.example.blinkitadmin.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAdminMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NavigationUI.setupWithNavController(binding.bottomMenu , Navigation.findNavController(this ,R.id.fragmentContainerView2))
    }
}