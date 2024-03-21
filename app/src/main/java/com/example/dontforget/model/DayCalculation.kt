package com.example.dontforget.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DayCalculation {

    fun getCurrentDateMillis(): Long {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyyMMdd")
        val formattedDate = dateFormat.format(currentDate)
        return dateFormat.parse(formattedDate).time
    }


    fun calculationDday(scheduleDate: Long?, currentDate: Long): Long? {
        var DdayCalculation: Long? = null
        if (scheduleDate != 0L) {
            if (scheduleDate != null) {
                DdayCalculation = (scheduleDate - currentDate) / (24 * 60 * 60 * 1000)
            }
        }
        Log.d("타임계산결과", DdayCalculation.toString())
        return DdayCalculation
    }

    fun replaceDday(scheduleTime: Long): String? {
        val currentTime = getCurrentDateMillis()
        Log.d("Dday", currentTime.toString())
        val Dday = calculationDday(scheduleTime, currentTime)
        Log.d("Dday", Dday.toString())
        return when {
            Dday!! > 0L -> "D - ${Dday.toString()}"
            Dday == 0L -> "D - Day"
            Dday <=(-1L)->"만료"
            else -> ""
        }
    }
//    val DdayCalculation = ((scheduleDate!!.toLong()) - currentDate) / (24*60*60*1000)

}