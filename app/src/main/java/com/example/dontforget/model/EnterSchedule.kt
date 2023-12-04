package com.example.dontforget.model

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.dontforget.MainActivity
import com.example.dontforget.R
import com.example.dontforget.databinding.ActivityEnterScheduleBinding
import java.text.SimpleDateFormat
import java.util.*

class EnterSchedule : AppCompatActivity() {
    val binding by lazy{ActivityEnterScheduleBinding.inflate(layoutInflater)}
    private var scheduleDate: Long? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.setDateButton.setOnClickListener {
            showDatePickerDialog()
        }

        val currentDate = DayCalculation().getCurrentDateMillis()


        val enterScheduleIntent = Intent(this, MainActivity::class.java)
        binding.scheduleInputComplete.setOnClickListener {
            if (scheduleDate != null) {
//                val DdayCalculation = ((scheduleDate!!.toLong()) - currentDate) / (24*60*60*1000)
                enterScheduleIntent.putExtra("scheduleTime", scheduleDate)

            }
            enterScheduleIntent.putExtra("scheduleText", binding.scheduleText.text.toString())
            setResult(RESULT_OK, enterScheduleIntent)
            finish()
        }
//      enterScheduleIntent.putExtra("scheduleTime", System.currentTimeMillis())
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