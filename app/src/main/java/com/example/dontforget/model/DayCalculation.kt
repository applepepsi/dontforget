package com.example.dontforget.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar

class DayCalculation {

    fun getCurrentDateMillis(): Long {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0)

        return calendar.timeInMillis
    }

    fun calculationDday(scheduleDate:Long?,currentDate:Long):Long?{
        var DdayCalculation: Long? = null
        if(scheduleDate!=null){
            DdayCalculation = ((scheduleDate.toLong()) - currentDate) / (24*60*60*1000)
        }
        return DdayCalculation
    }
//    val DdayCalculation = ((scheduleDate!!.toLong()) - currentDate) / (24*60*60*1000)

}