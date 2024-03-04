package com.example.dontforget.model

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import com.example.dontforget.MainActivity
import com.example.dontforget.R

import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.databinding.ActivityModifyScheduleBinding
import com.example.dontforget.model.db.ScheduleModel

class ModifySchedule : AppCompatActivity() {
    val binding by lazy{ ActivityModifyScheduleBinding.inflate(layoutInflater)}
    private var modifyTextSize: Float = 0f
    val currentDateMilli = DayCalculation().getCurrentDateMillis()
    private var modifyScheduleMilli: Long? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val scheduleText = intent.getStringExtra("scheduleText")
        var scheduleDDay = intent.getLongExtra("scheduleDDay",0)
        val textSize=intent.getFloatExtra("textSize",15f)
        var scheduleDate=intent.getStringExtra("scheduleDate")

        val oldTextStyleList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            intent?.getParcelableArrayListExtra<Parcelable>("textStyleList")
        } else {
            intent?.getParcelableArrayListExtra("textStyleList")
        }
        Log.d("수정하기 위해 보낸 스타일리스트", oldTextStyleList.toString())
        bottomNavigation()

        if (scheduleText != null) {
            binding.scheduleText.setText(scheduleText)
            binding.scheduleText.setTextSize(textSize)
        }
        if(scheduleDDay!=null){
            if(scheduleDate==""){
                binding.setDate.setText("날짜 미선택")
            }
            else{
                binding.setDate.setText(scheduleDate)
            }
        }

        binding.writeButton.setOnClickListener { handleScheduleInput(textSize,scheduleDDay,scheduleDate!!) }

        binding.backButton.setOnClickListener{
            finish()
        }
    }
    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, day,0,0,0)

                modifyScheduleMilli = selectedCalendar.timeInMillis
                binding.setDate.setText("${year}년 ${month+1}월 ${day}일")

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun bottomNavigation(){
        binding.bottomNavigationView.setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.selectDate->{
                    showDatePickerDialog()
                    true
                }
                R.id.textSize->{
                    showTextSizeChangePopUp()
                    true
                }
//                R.id.write->{
//                    handleScheduleInput(textSize,scheduleDDay,scheduleDate)
//                    true
//                }
                else -> false
            }
        }
    }


    private fun showTextSizeChangePopUp(){
        val popupMenu = PopupMenu(this, binding.bottomNavigationView)
        popupMenu.inflate(R.menu.character_size_settings)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            modifyTextSize = when (menuItem.itemId) {
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
            binding.scheduleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, modifyTextSize)
            true
        }
        popupMenu.show()
    }


    private fun handleScheduleInput(textSize:Float,scheduleDDay:Long,scheduleDate:String) {
        val modifyIntent = Intent(this, MainActivity::class.java)

        if (modifyScheduleMilli != null) {
            if(currentDateMilli<=modifyScheduleMilli!!){
                modifyIntent.putExtra("modifyScheduleMilli", modifyScheduleMilli)
                modifyIntent.putExtra("scheduleDate",binding.setDate.getText().toString())
                if(binding.scheduleText.text.toString()!=""){
                    modifyIntent.putExtra("modifyText", binding.scheduleText.text.toString())
                    if(modifyTextSize!=0f){
                        modifyIntent.putExtra("textSize", modifyTextSize)
                        setResult(RESULT_OK, modifyIntent)
                        finish()
                    }
                    else{
                        modifyIntent.putExtra("textSize", textSize)
                        setResult(RESULT_OK, modifyIntent)
                        finish()
                    }
                }
                else{
                    Toast.makeText(this@ModifySchedule, "메모 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this@ModifySchedule, "날짜는 최소 내일로 선택해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            modifyIntent.putExtra("modifyScheduleMilli", scheduleDDay)
            modifyIntent.putExtra("scheduleDate",scheduleDate)
            if(binding.scheduleText.text.toString()!=""){
                modifyIntent.putExtra("modifyText", binding.scheduleText.text.toString())
                if(modifyTextSize!=0f){
                    modifyIntent.putExtra("textSize", modifyTextSize)
                    setResult(RESULT_OK, modifyIntent)
                    finish()
                }
                else{
                    modifyIntent.putExtra("textSize", textSize)
                    setResult(RESULT_OK, modifyIntent)
                    finish()
                }
            }
            else{
                Toast.makeText(this@ModifySchedule, "메모 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}