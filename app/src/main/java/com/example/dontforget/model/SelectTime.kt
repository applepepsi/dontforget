package com.example.dontforget.model

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.dontforget.databinding.ActivityMainBinding

class SelectTime : AppCompatActivity() {
    val binding by lazy{ ActivityMainBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}