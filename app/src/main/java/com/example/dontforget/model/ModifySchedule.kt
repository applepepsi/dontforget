package com.example.dontforget.model

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import com.example.dontforget.MainActivity
import com.example.dontforget.R

import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.databinding.ActivityModifyScheduleBinding
import com.example.dontforget.model.db.ScheduleModel

class ModifySchedule : AppCompatActivity() {
    val binding by lazy{ ActivityModifyScheduleBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val scheduleText = intent.getStringExtra("scheduleText")

        if (scheduleText != null) {
            binding.scheduleText.setText(scheduleText)
        }

        val modifyIntent=Intent(this, MainActivity::class.java)

        binding.modifyScheduleButton.setOnClickListener{
            modifyIntent.putExtra("modifyText", binding.scheduleText.text.toString())
            startActivity(modifyIntent)
            finish()
        }
    }
}