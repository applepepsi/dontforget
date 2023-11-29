package com.example.dontforget.model

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.dontforget.MainActivity
import com.example.dontforget.R
import com.example.dontforget.databinding.ActivityEnterScheduleBinding

class EnterSchedule : AppCompatActivity() {
    val binding by lazy{ActivityEnterScheduleBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val enterScheduleIntent=Intent(this,MainActivity::class.java)

        binding.scheduleInputComplete.setOnClickListener {
            enterScheduleIntent.putExtra("scheduleText", binding.scheduleText.text.toString())
            enterScheduleIntent.putExtra("scheduleTime",System.currentTimeMillis())
            setResult(RESULT_OK,enterScheduleIntent)
            finish()
        }
    }
}