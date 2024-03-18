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
import android.util.Size
import android.util.TypedValue
import kotlin.math.abs
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.dontforget.*
import com.example.dontforget.databinding.ActivityEnterScheduleBinding
import com.example.dontforget.spanInfo.ColorInfo
import com.example.dontforget.spanInfo.SizeInfo
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max


class EnterSchedule : AppCompatActivity() {
    val binding by lazy{ActivityEnterScheduleBinding.inflate(layoutInflater)}
    private var textSize: Float = 15f

    private var scheduleDateMilli: Long? = null
    val currentDateMilli = DayCalculation().getCurrentDateMillis()
    private var setNotification=0
    private val textColorList= mutableMapOf<Int,Int>()
    private val textSizeList= mutableMapOf<Int,Float>()
    private var defaultColor=-16777216

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initUi()
        setupListeners()

    }

    private fun initUi(){
        binding.scheduleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
        binding.setDate.setPaddingRelative(30, 0, 0, 0)
    }

    private fun setupListeners(){
        binding.setDate.setOnClickListener{ showDatePickerDialog() }
        binding.backButton.setOnClickListener{ finish() }
        binding.writeButton.setOnClickListener { handleScheduleInput() }
        textWatcher()
        bottomNavigation()
        notificationSwitchControl()
    }


    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyyMMdd")

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, day, 0, 0, 0)

                binding.setDate.setText("${year}년 ${month + 1}월 ${day}일")
                scheduleDateMilli = dateFormat.parse("${year}${String.format("%02d", month + 1)}${String.format("%02d", day)}")?.time
                deleteScheduleTimeControl()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun deleteScheduleTimeControl() {
        binding.deleteScheduleTime.visibility = View.VISIBLE
        binding.setDate.setPaddingRelative(100, 0, 0, 0)
        binding.deleteScheduleTime.setOnClickListener { deleteScheduleTime() }
    }

    private fun deleteScheduleTime() {
        binding.setDate.setText("날짜 미선택")
        scheduleDateMilli = null
        binding.deleteScheduleTime.visibility = View.GONE
        binding.setDate.setPaddingRelative(30, 0, 0, 0)
        binding.notificationText.text = "알림 Off"
        setNotification = 0
        binding.notificationSwitch.isChecked = false
    }

    private fun notificationSwitchControl() {
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (scheduleDateMilli != null) {
                updateNotificationStatus(isChecked)
            } else {
                binding.notificationSwitch.isChecked = false
                setNotification = 0
            }
        }
    }

    private fun updateNotificationStatus(isChecked: Boolean) {
        val notificationStatus = if (isChecked) "알림 On" else "알림 Off"
        binding.notificationText.text = notificationStatus
        setNotification = if (isChecked) 1 else 0
        binding.notificationSwitch.isChecked = isChecked
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

//
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

    private fun ddayCalculation(): Long? {
        val currentTime = DayCalculation().getCurrentDateMillis()

        val Dday = DayCalculation().calculationDday(scheduleDateMilli, currentTime)
        return Dday
    }


    private fun handleScheduleInput() {
        val enterScheduleIntent = Intent(this, MainActivity::class.java)
        val text=binding.scheduleText.text as Editable
        val dday=ddayCalculation()
//        enterScheduleIntent.putParcelableArrayListExtra("spanInfo", ArrayList(spanInfos))
        enterScheduleIntent.putExtra("lineCount", binding.scheduleText.getLineCount())

        if (scheduleDateMilli != null) {
            if (currentDateMilli <= scheduleDateMilli!!) {
                enterScheduleIntent.putExtra("scheduleDate", binding.setDate.text.toString())

                enterScheduleIntent.putExtra("scheduleDateMilli", scheduleDateMilli)
                enterScheduleIntent.putExtra("textSize", textSize)
                enterScheduleIntent.putExtra("setNotification", setNotification)
                enterScheduleIntent.putExtra("dday", dday)

                if (binding.scheduleText.text.toString().isNotEmpty() && binding.scheduleTitle.text.toString().isNotEmpty()) {
                    enterScheduleIntent.putExtra("scheduleText", binding.scheduleText.text.toString())
                    enterScheduleIntent.putExtra("scheduleTitle", binding.scheduleTitle.text.toString())
                    setResult(RESULT_OK, enterScheduleIntent)
                    finish()
                } else {
                    Toast.makeText(this@EnterSchedule, "메모 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@EnterSchedule, "날짜는 최소 내일로 선택해 주세요.", Toast.LENGTH_SHORT).show()
            }
        } else {
            enterScheduleIntent.putExtra("scheduleDate", "날짜 미선택")
            enterScheduleIntent.putExtra("scheduleDateMilli", 0)
            enterScheduleIntent.putExtra("textSize", textSize)
            enterScheduleIntent.putExtra("setNotification", 0)
            enterScheduleIntent.putExtra("dday", -1)

            if (binding.scheduleText.text.toString().isNotEmpty() && binding.scheduleTitle.text.toString().isNotEmpty()) {
                enterScheduleIntent.putExtra("scheduleText", binding.scheduleText.text.toString())
                enterScheduleIntent.putExtra("scheduleTitle", binding.scheduleTitle.text.toString())
                setResult(RESULT_OK, enterScheduleIntent)
                finish()
            } else {
                Toast.makeText(this@EnterSchedule, "메모 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }



}