package com.example.dontforget.setting

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.dontforget.R
import com.example.dontforget.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    val binding by lazy{ ActivitySettingsBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var maxId = intent.getIntExtra("maxId",0)
        Log.d("맥스아이디", maxId.toString())
        setupListeners()
    }

    private fun setupListeners(){

        binding.backButton.setOnClickListener{ finish() }

    }
}