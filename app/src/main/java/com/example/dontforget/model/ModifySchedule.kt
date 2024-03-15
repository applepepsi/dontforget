package com.example.dontforget.model

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import com.example.dontforget.MainActivity
import com.example.dontforget.R
import com.example.dontforget.spanInfo.TextStyleInfo
import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.databinding.ActivityModifyScheduleBinding
import com.example.dontforget.model.db.ScheduleModel
import com.example.dontforget.spanInfo.ColorInfo
import com.example.dontforget.spanInfo.SizeInfo
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import java.text.SimpleDateFormat
import kotlin.math.max

class ModifySchedule : AppCompatActivity() {
    val binding by lazy{ ActivityModifyScheduleBinding.inflate(layoutInflater)}
    private var modifyTextSize: Float = 0f
    val currentDateMilli = DayCalculation().getCurrentDateMillis()
    private var modifyScheduleMilli: Long? = null
    private var setNotification=0
    private var defaultColor=-16777216
    private val textColorList= mutableMapOf<Int,Int>()
    private val textSizeList= mutableMapOf<Int,Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var scheduleText = intent.getStringExtra("scheduleText")
        var scheduleDDay = intent.getLongExtra("scheduleDDay",0)
        val textSize=intent.getFloatExtra("textSize",15f)
        var scheduleDate=intent.getStringExtra("scheduleDate")
        var scheduleTitle = intent.getStringExtra("scheduleTitle")
        setNotification = intent.getIntExtra("setNotification",0)
        var dday = intent.getLongExtra("dday",-1)
        Log.d("수정된 노티피",setNotification.toString())
        textWatcher()
        bottomNavigation()


        if(setNotification==1){
            binding.notificationSwitch.isChecked = true
            binding.notificationText.text="알림 On"
        }

        notificationSwitchControl(scheduleDDay)

        if (scheduleText != null && scheduleTitle!=null) {
            binding.scheduleText.setText(scheduleText)
            binding.scheduleText.setTextSize(textSize)
            binding.scheduleTitle.setText(scheduleTitle)
        }
        if(scheduleDate==""){
            binding.setDate.setText("날짜 미선택")
        }
        else{
            binding.setDate.setText(scheduleDate)
        }



        binding.setDate.setOnClickListener{
            showDatePickerDialog()
        }



        binding.writeButton.setOnClickListener { handleScheduleInput(textSize,scheduleDDay,scheduleDate!!) }

        binding.backButton.setOnClickListener{
            finish()
        }
    }
    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyyMMdd")
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, day,0,0,0)

                binding.setDate.setText("${year}년 ${month+1}월 ${day}일")
                modifyScheduleMilli= dateFormat.parse(("${year}${String.format("%02d", month + 1)}${String.format("%02d", day)}"))?.time

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }
    private fun notificationSwitchControl(scheduleDDay:Long){

        binding.notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(modifyScheduleMilli!=null || scheduleDDay!=null){
                if (isChecked) {
                    binding.notificationSwitch.isChecked=true

                    binding.notificationText.text="알림 On"
                    setNotification=1
                }
                else{
                    binding.notificationText.text="알림 Off"
                    setNotification=0
                }
            }else{
                Toast.makeText(this, "알림을 설정하기 위해선 날짜를 선택해야 합니다.", Toast.LENGTH_SHORT).show()
                binding.notificationSwitch.isChecked = false
                setNotification=0
            }
        }
    }
//    private fun colorPickerDialog(){
//
//        MaterialColorPickerDialog
//            .Builder(this)
//            .setTitle("색상 선택")
//            .setColorShape(ColorShape.SQAURE)
//            .setColorSwatch(ColorSwatch._300)
//            .setColors(arrayListOf("#f6e58d", "#ffbe76", "#ff7979", "#badc58", "#dff9fb", "#7ed6df", "#e056fd", "#686de0", "#30336b", "#95afc0","#E3E3E3","#000000"))
//            .setDefaultColor("#ff7979")
//            .setColorListener { color, _ ->
////                selectedColor = color
//                defaultColor=color
//                changeColor(color)
////                binding.scheduleText.setTextColor(color)
//
//            }
//            .show()
//    }

//    private fun changeColor(color:Int){
//
//        val start = binding.scheduleText.selectionStart
//        val end = binding.scheduleText.selectionEnd
//        val text = binding.scheduleText.text
//        val spannable = SpannableStringBuilder(text)
//        Log.d("시작값 종료값 테스트", "시작값${start.toString()},종료값${end.toString()}" )
//
////        for (i in start until end){
////            textColorList[i]=color
////        }
//        Log.d("색 리스트값 확인", textColorList.toString())
//        spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        binding.scheduleText.text = spannable
//    }
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
                else -> false
            }
        }
    }

    private fun textWatcher() {
        binding.scheduleText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                binding.textCounter.setText("${s?.length ?:0} 글자")
            }
        })
    }

    private fun ddayCalculation(scheduleTime:Long): Long? {
        val currentTime = DayCalculation().getCurrentDateMillis()

        val Dday = DayCalculation().calculationDday(scheduleTime, currentTime)
        return Dday
    }



    private fun handleScheduleInput(textSize:Float,scheduleDDay:Long,scheduleDate:String) {
        val modifyIntent = Intent(this, MainActivity::class.java)

        modifyIntent.putExtra("lineCount", binding.scheduleText.getLineCount())

        if (modifyScheduleMilli != null) {
            if(currentDateMilli<=modifyScheduleMilli!!){

                modifyIntent.putExtra("modifyScheduleMilli", modifyScheduleMilli)
                modifyIntent.putExtra("scheduleDate",binding.setDate.getText().toString())
                modifyIntent.putExtra("modifySetNotification", setNotification)
                modifyIntent.putExtra("modifyDday", ddayCalculation(modifyScheduleMilli!!))


                if(binding.scheduleText.text.toString()!="" && binding.scheduleTitle.text.toString()!=""){
                    modifyIntent.putExtra("modifyText", binding.scheduleText.text.toString())
                    modifyIntent.putExtra("modifyTitle", binding.scheduleTitle.text.toString())
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
            modifyIntent.putExtra("modifySetNotification", setNotification)
            modifyIntent.putExtra("modifyDday", ddayCalculation(scheduleDDay))

            if(binding.scheduleText.text.toString()!="" && binding.scheduleTitle.text.toString()!=""){
                modifyIntent.putExtra("modifyText", binding.scheduleText.text.toString())
                modifyIntent.putExtra("modifyTitle", binding.scheduleTitle.text.toString())
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