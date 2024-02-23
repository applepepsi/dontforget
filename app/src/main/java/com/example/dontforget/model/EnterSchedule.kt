package com.example.dontforget.model

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Parcelable
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.dontforget.MainActivity
import com.example.dontforget.R
import com.example.dontforget.TextColorData
import com.example.dontforget.TextSizeData
import com.example.dontforget.databinding.ActivityEnterScheduleBinding
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import java.io.Serializable
import java.util.*


class EnterSchedule : AppCompatActivity() {
    val binding by lazy{ActivityEnterScheduleBinding.inflate(layoutInflater)}
    private var textSize: Float = 15f
    private var scheduleDateMilli: Long? = null
    val currentDateMilli = DayCalculation().getCurrentDateMillis()
    private val textColorList= mutableMapOf<Int,Int>()
    private val textSizeList= mutableMapOf<Int,Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        bottomNavigation()

        textWatcher()


        binding.writeButton.setOnClickListener { handleScheduleInput() }


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
                selectedCalendar.set(year, month, day)

                scheduleDateMilli = selectedCalendar.timeInMillis
                binding.setDate.setText("${year}년 ${month+1}월 ${day}일")

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun colorPickerDialog(){
        val changeColor=MaterialColorPickerDialog
            .Builder(this)
            .setTitle("색상 선택")
            .setColorShape(ColorShape.SQAURE)
            .setColorSwatch(ColorSwatch._300)
            .setColors(arrayListOf("#f6e58d", "#ffbe76", "#ff7979", "#badc58", "#dff9fb", "#7ed6df", "#e056fd", "#686de0", "#30336b", "#95afc0","#E3E3E3","#000000"))
            .setDefaultColor("#ff7979")
            .setColorListener { color, _ ->
//                selectedColor = color
                changeColor(color)
//                binding.scheduleText.setTextColor(color)

            }
            .show()
    }

    private fun changeColor(color:Int){

        val start = binding.scheduleText.selectionStart
        val end = binding.scheduleText.selectionEnd
        val text = binding.scheduleText.text
        val spannable = SpannableStringBuilder(text)
        Log.d("시작값 종료값 테스트", "시작값${start.toString()},종료값${end.toString()}" )

        for (i in start until end){
            textColorList[i]=color
        }
        Log.d("색 리스트값 확인", textColorList.toString())
        spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.scheduleText.text = spannable

    }

    private fun textWatcher() {
        binding.scheduleText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val currentCursorPosition = binding.scheduleText.selectionStart
                val spannable = SpannableStringBuilder(binding.scheduleText.text)

            }
        })
    }

    private fun bottomNavigation(){
        binding.bottomNavigationView.setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.selectDate->{
                    showDatePickerDialog()
                    true
                }
                R.id.textSize->{
                    showTextSizeChangePopUp2()
                    true
                }
                R.id.selectColor->{
                    colorPickerDialog()

                    true
                }
                else -> false
            }
        }
    }



    private fun showTextSizeChangePopUp(){
        val popupMenu = PopupMenu(this, binding.bottomNavigationView)
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
    private fun showTextSizeChangePopUp2(){
        val popupMenu = PopupMenu(this, binding.bottomNavigationView)
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
            changeTextSize(textSize)
            true
        }
        popupMenu.show()
    }

    private fun changeTextSize(textSize: Float){
        val start = binding.scheduleText.selectionStart
        val end = binding.scheduleText.selectionEnd
        val text = binding.scheduleText.text
        val spannable = SpannableStringBuilder(text)

        for (i in start until end){
            textSizeList[i]=textSize
        }
        Log.d("글자 크기 리스트값 확인", textSizeList.toString())
        spannable.setSpan(AbsoluteSizeSpan(textSize.toInt(),true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.scheduleText.text=spannable
    }

    private fun handleScheduleInput() {
        val enterScheduleIntent = Intent(this, MainActivity::class.java)


        if (scheduleDateMilli != null) {
            if (currentDateMilli <= scheduleDateMilli!!) {
                enterScheduleIntent.putExtra("scheduleDate", binding.setDate.text.toString())
                enterScheduleIntent.putExtra("scheduleDateMilli", scheduleDateMilli)
                enterScheduleIntent.putExtra("textSize", textSize)
                enterScheduleIntent.putExtra("textSizeList", TextSizeData(textSizeList))
                enterScheduleIntent.putExtra("textColorList", TextColorData(textColorList))

                if (binding.scheduleText.text.toString().isNotEmpty()) {
                    enterScheduleIntent.putExtra("scheduleText", binding.scheduleText.text.toString())
                    setResult(RESULT_OK, enterScheduleIntent)
                    finish()
                } else {
                    Toast.makeText(this@EnterSchedule, "메모 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@EnterSchedule, "날짜는 최소 내일로 선택해 주세요.", Toast.LENGTH_SHORT).show()
            }
        } else {
            enterScheduleIntent.putExtra("scheduleDate", "")
            enterScheduleIntent.putExtra("scheduleDateMilli", 0)
            enterScheduleIntent.putExtra("textSize", textSize)

            if (binding.scheduleText.text.toString().isNotEmpty()) {
                enterScheduleIntent.putExtra("scheduleText", binding.scheduleText.text.toString())
                setResult(RESULT_OK, enterScheduleIntent)
                finish()
            } else {
                Toast.makeText(this@EnterSchedule, "메모 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }



}