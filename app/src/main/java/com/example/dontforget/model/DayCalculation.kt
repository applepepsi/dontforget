package com.example.dontforget.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class DayCalculation {

    fun getCurrentDateMillis(): Long {
//        val currentDate = Calendar.getInstance()
//        val year = currentDate.get(Calendar.YEAR)
//        val month = currentDate.get(Calendar.MONTH)
//        val day = currentDate.get(Calendar.DAY_OF_MONTH)
//
//
//        val calendar = Calendar.getInstance()
//        calendar.set(year, month, day)
//
//        return calendar.timeInMillis
//        return Calendar.getInstance().getTimeInMillis()
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyyMMdd")
        val formattedDate = dateFormat.format(currentDate)

        return dateFormat.parse(formattedDate).time
    }


    fun calculationDday(scheduleDate: Long?, currentDate: Long): Long? {
        var DdayCalculation: Long? = null
        if (scheduleDate != null) {
            DdayCalculation = (scheduleDate - currentDate) / (24 * 60 * 60 * 1000)
        }
        Log.d("타임계산결과", DdayCalculation.toString())
        return DdayCalculation
    }

//    val DdayCalculation = ((scheduleDate!!.toLong()) - currentDate) / (24*60*60*1000)

}