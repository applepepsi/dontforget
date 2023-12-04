package com.example.dontforget.model

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
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


    private var scheduleDate: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val scheduleText = intent.getStringExtra("scheduleText")
        var modifyScheduleDate = intent.getLongExtra("scheduleTime",0)

        if (scheduleText != null) {
            binding.scheduleText.setText(scheduleText)
        }


        binding.timeModifyButton.setOnClickListener {
            showDatePickerDialog()
        }

        val currentDate = DayCalculation().getCurrentDateMillis()

        val modifyIntent = Intent(this, MainActivity::class.java)
        binding.modifyScheduleButton.setOnClickListener {
            if (scheduleDate != null) {
//                val DdayCalculation = ((scheduleDate!!.toLong()) - currentDate) / (24*60*60*1000)
                modifyIntent.putExtra("scheduleTime", scheduleDate)
            }
            else{
                modifyIntent.putExtra("scheduleTime", modifyScheduleDate)
            }

            modifyIntent.putExtra("modifyText", binding.scheduleText.text.toString())
            setResult(RESULT_OK, modifyIntent)
            finish()
        }

//        binding.modifyScheduleButton.setOnClickListener{
//            modifyIntent.putExtra("modifyText", binding.scheduleText.text.toString())
//            setResult(RESULT_OK,modifyIntent)
//            finish()

    }
    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, day)

                scheduleDate = selectedCalendar.timeInMillis
                binding.setDate.setText("${year}년${month+1}월${day}일")

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }
}