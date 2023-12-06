package com.example.dontforget.model

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.PopupMenu
import com.example.dontforget.MainActivity
import com.example.dontforget.R

import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.databinding.ActivityModifyScheduleBinding
import com.example.dontforget.model.db.ScheduleModel

class ModifySchedule : AppCompatActivity() {
    val binding by lazy{ ActivityModifyScheduleBinding.inflate(layoutInflater)}
    private var textSize: Float = 0f

    private var scheduleDate: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val scheduleText = intent.getStringExtra("scheduleText")
        var modifyScheduleDate = intent.getLongExtra("scheduleTime",0)
        val modifyTextSize=intent.getFloatExtra("textSize",15f)

        if (scheduleText != null) {
            binding.scheduleText.setText(scheduleText)
        }

        binding.characterSizeChange.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.character_size_settings)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                textSize = when (menuItem.itemId) {
                    R.id.size15 -> 15f
                    R.id.size20 -> 20f
                    R.id.size25 -> 25f
                    R.id.size30 -> 30f
                    R.id.size35 -> 35f
                    R.id.size40 -> 40f
                    R.id.size45 -> 45f
                    R.id.size50 -> 50f
                    else -> modifyTextSize
                }
                binding.scheduleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)

                true
            }
            popupMenu.show()
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
            if(textSize!=0f){
                modifyIntent.putExtra("textSize", textSize)
            }
            else{
                modifyIntent.putExtra("textSize", modifyTextSize)
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