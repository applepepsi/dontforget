package com.example.dontforget.model

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.*
import com.example.dontforget.MainActivity
import com.example.dontforget.R
import com.example.dontforget.databinding.ActivityEnterScheduleBinding
import java.text.SimpleDateFormat
import java.util.*

class EnterSchedule : AppCompatActivity() {
    val binding by lazy{ActivityEnterScheduleBinding.inflate(layoutInflater)}
    private var textSize: Float = 15f
    private var scheduleDateMilli: Long? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val currentDateMilli = DayCalculation().getCurrentDateMillis()

        binding.setDateButton.setOnClickListener {
            showDatePickerDialog()
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
                    else -> 15f
                }
                binding.scheduleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)

                true
            }
            popupMenu.show()
        }

        val enterScheduleIntent = Intent(this, MainActivity::class.java)
        binding.scheduleInputComplete.setOnClickListener {
            if (scheduleDateMilli != null) {
                if(currentDateMilli<=scheduleDateMilli!!) {

                    enterScheduleIntent.putExtra("scheduleDate", binding.setDate.getText().toString())
                    enterScheduleIntent.putExtra("scheduleDateMilli", scheduleDateMilli)
                    enterScheduleIntent.putExtra("textSize", textSize)
                    if (binding.scheduleText.text.toString() != "") {
                        enterScheduleIntent.putExtra("scheduleText", binding.scheduleText.text.toString())
                        setResult(RESULT_OK, enterScheduleIntent)
                        finish()
                    } else {
                        Toast.makeText(this@EnterSchedule, "메모 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this@EnterSchedule, "날짜는 최소 내일로 선택해 주세요.", Toast.LENGTH_SHORT).show()
                }

            }
            else{
                enterScheduleIntent.putExtra("scheduleDate", "")
                enterScheduleIntent.putExtra("scheduleDateMilli", 0)
                enterScheduleIntent.putExtra("textSize", textSize)
                if (binding.scheduleText.text.toString() != "") {
                    enterScheduleIntent.putExtra("scheduleText", binding.scheduleText.text.toString())
                    setResult(RESULT_OK, enterScheduleIntent)
                    finish()
                } else {
                    Toast.makeText(this@EnterSchedule, "메모 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, day)

                scheduleDateMilli = selectedCalendar.timeInMillis
                binding.setDate.setText("${year}년${month+1}월${day}일")

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

}