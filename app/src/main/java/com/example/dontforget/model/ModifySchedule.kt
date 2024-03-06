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
import kotlin.math.max

class ModifySchedule : AppCompatActivity() {
    val binding by lazy{ ActivityModifyScheduleBinding.inflate(layoutInflater)}
    private var modifyTextSize: Float = 15f
    val currentDateMilli = DayCalculation().getCurrentDateMillis()
    private var modifyScheduleMilli: Long? = null
    private var defaultColor=-16777216
    private val textColorList= mutableMapOf<Int,Int>()
    private val textSizeList= mutableMapOf<Int,Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var scheduleText = intent.getStringExtra("scheduleText")
        var scheduleDDay = intent.getLongExtra("scheduleDDay",0)
        val textSize=intent.getFloatExtra("textSize",0f)
        var scheduleDate=intent.getStringExtra("scheduleDate")





        bottomNavigation()
        textWatcher()

        if (scheduleText != null) {
            binding.scheduleText.setText(scheduleText)
//            binding.scheduleText.setTextSize(textSize)
        }
        if(scheduleDDay!=null){
            if(scheduleDate==""){
                binding.setDate.setText("날짜 미선택")
            }
            else{
                binding.setDate.setText(scheduleDate)
            }
        }
        val oldTextStyleList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            intent?.getParcelableArrayListExtra<TextStyleInfo>("textStyleList")
        } else {
            intent?.getParcelableArrayListExtra("textStyleList")
        }


        val oldSpannableText= SpannableStringBuilder(scheduleText)
        Log.d("수정하기 위해 보낸 스타일리스트", oldTextStyleList.toString())

        oldTextStyleList?.forEach { textStyleInfo ->

            val start=textStyleInfo.startIndex
            val end=textStyleInfo.endIndex
            Log.d("리스트 확인", "${start},${end},${textStyleInfo.color},${textStyleInfo.textSize},")

            textStyleInfo.color?.let { color ->
                oldSpannableText.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }


            textStyleInfo.textSize?.let { textSize ->
                oldSpannableText.setSpan(
                    AbsoluteSizeSpan(textSize.toInt(),true),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }


        binding.scheduleText.setText(oldSpannableText)
        Log.d("수정된 스판 확인", oldSpannableText.toString())



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

    private fun colorPickerDialog(){

        MaterialColorPickerDialog
            .Builder(this)
            .setTitle("색상 선택")
            .setColorShape(ColorShape.SQAURE)
            .setColorSwatch(ColorSwatch._300)
            .setColors(arrayListOf("#f6e58d", "#ffbe76", "#ff7979", "#badc58", "#dff9fb", "#7ed6df", "#e056fd", "#686de0", "#30336b", "#95afc0","#E3E3E3","#000000"))
            .setDefaultColor("#ff7979")
            .setColorListener { color, _ ->
//                selectedColor = color
                defaultColor=color
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

//        for (i in start until end){
//            textColorList[i]=color
//        }
        Log.d("색 리스트값 확인", textColorList.toString())
        spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.scheduleText.text = spannable
    }

    private fun changeDefaultColor(text:Editable){

        val end=text.length
        var lastColorIndex=-1
        for (i in  0 until end) {
            val spans = text.getSpans(i, i +  1, ForegroundColorSpan::class.java)
            if (spans.isNotEmpty()) {
                lastColorIndex = i
            }
        }
        val start = if (lastColorIndex >=  0) lastColorIndex +  1 else  0
        Log.d("디폴트색 확인", defaultColor.toString())
        text.setSpan(
            ForegroundColorSpan(defaultColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

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
            changeTextSize(modifyTextSize)
//            binding.scheduleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, modifyTextSize)
            true
        }
        popupMenu.show()
    }
    private fun changeTextSize(textSize: Float){
        val start = binding.scheduleText.selectionStart
        val end = binding.scheduleText.selectionEnd
        val text = binding.scheduleText.text
        val spannable = SpannableStringBuilder(text)

        Log.d("글자 크기 리스트값 확인", textSizeList.toString())
        spannable.setSpan(AbsoluteSizeSpan(textSize.toInt(),true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.scheduleText.text=spannable
    }

    private fun changeDefaultTextSize(text: Editable){
        val end=text.length
        var lastSizeIndex=-1
        for (i in  0 until end) {
            val spans = text.getSpans(i, i +  1, AbsoluteSizeSpan::class.java)
            if (spans.isNotEmpty()) {
                lastSizeIndex = i
            }
        }
        val start = if (lastSizeIndex >=  0) lastSizeIndex +  1 else  0
        Log.d("디폴트색 확인", defaultColor.toString())
        text.setSpan(
            AbsoluteSizeSpan(modifyTextSize.toInt(),true),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

    }

    private fun textWatcher() {
        binding.scheduleText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let{changeDefaultColor(it)}
                s?.let{changeDefaultTextSize(it)}

            }
        })
    }

    private fun getSpansInfo(text: Editable): List<Parcelable> {

        val colorInfo=mutableListOf<ColorInfo>()
        val sizeInfo=mutableListOf<SizeInfo>()

        val colorSpans = text.getSpans(0, text.length, ForegroundColorSpan::class.java)


        for (span in colorSpans) {
            if(span.foregroundColor!=-16777216){
                val startIndex = text.getSpanStart(span)
                val endIndex = text.getSpanEnd(span)
                val color = span.foregroundColor

                val newSpanInfo=ColorInfo(startIndex,endIndex,color)
                colorInfo.add(newSpanInfo)
            }
        }
//        Log.d("컬러 중복제거 테스트", deleteDuplicateColorInfo(colorInfo).toString())
//        Log.d("test1", (colorInfo).toString())

        val sizeSpans = text.getSpans(0, text.length, AbsoluteSizeSpan::class.java)

        for (span in sizeSpans) {
            if((span.size).toString() != "15")
            {
                val startIndex = text.getSpanStart(span)
                val endIndex = text.getSpanEnd(span)
                val size = span.size
                val newSpanInfo=SizeInfo(startIndex, endIndex, size.toFloat())


                sizeInfo.add(newSpanInfo)
            }

        }

//        Log.d("test2", deleteDuplicateSizeInfo(sizeInfo).toString())
//        Log.d("test1", (colorInfo+sizeInfo).toString())
        return (deleteDuplicateColorInfo(colorInfo)+deleteDuplicateSizeInfo(sizeInfo))
    }

    private fun deleteDuplicateSizeInfo(spanInfo: MutableList<SizeInfo>): MutableList<SizeInfo> {
        val mergedInfo = mutableListOf<SizeInfo>()
        var currentInfo: SizeInfo? = null
//        Log.d("크기인포 테스트", spanInfo.toString())
        spanInfo.sortedBy { it.startIndex }.forEach { info ->
            if (currentInfo == null) {

                currentInfo = info


            } else if (currentInfo!!.endIndex+ 1 >= info.startIndex && currentInfo!!.size == info.size) {

                currentInfo = currentInfo!!.copy(endIndex = max(currentInfo!!.endIndex, info.endIndex))
            } else {
                if (currentInfo != null) {
                    mergedInfo.add(currentInfo!!)
                }
                currentInfo = info
            }
        }

        if (currentInfo != null) {
            mergedInfo.add(currentInfo!!)
        }

        return mergedInfo
    }

    private fun deleteDuplicateColorInfo(spanInfo:MutableList<ColorInfo>): MutableList<ColorInfo> {
        val mergedInfo = mutableListOf<ColorInfo>()
        var currentInfo: ColorInfo? = null

        spanInfo.sortedBy { it.startIndex }.forEach { info ->

            if (currentInfo == null) {

                currentInfo = info


            } else if (currentInfo!!.endIndex+ 1 >= info.startIndex && currentInfo!!.color == info.color) {

                currentInfo = currentInfo!!.copy(endIndex = max(currentInfo!!.endIndex, info.endIndex))
            } else {
                if (currentInfo != null) {
                    mergedInfo.add(currentInfo!!)
                }
                currentInfo = info
            }
        }

        if (currentInfo != null) {
            mergedInfo.add(currentInfo!!)
        }

        return mergedInfo
    }

    private fun handleScheduleInput(textSize:Float,scheduleDDay:Long,scheduleDate:String) {
        val modifyIntent = Intent(this, MainActivity::class.java)
        val text=binding.scheduleText.text as Editable
        val spanInfos = getSpansInfo(text)
        Log.d("수정된 겟스판인포",spanInfos.toString())

        modifyIntent.putParcelableArrayListExtra("modifySpanInfo", ArrayList(spanInfos))

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