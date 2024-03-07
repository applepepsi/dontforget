package com.example.dontforget.util

import android.os.Parcelable
import android.util.Log
import com.example.dontforget.model.db.ScheduleDao
import com.example.dontforget.model.db.TextStyleDao
import com.example.dontforget.model.db.TextStyleModel
import com.example.dontforget.spanInfo.ColorInfo
import com.example.dontforget.spanInfo.SizeInfo

class SpanInfoProcessor(private val textStyleDao: TextStyleDao) {

    suspend fun processSpanInfoList(spanInfoList: List<Parcelable>?, scheduleId: Int) {
        if (spanInfoList != null) {
            for (spanInfo in spanInfoList) {
                val startIndex: Int
                val endIndex: Int
                val color: Int?
                val size: Float?

                when (spanInfo) {
                    is ColorInfo -> {
                        startIndex = spanInfo.startIndex
                        endIndex = spanInfo.endIndex
                        color = spanInfo.color!!
                        size = null
                    }
                    is SizeInfo -> {
                        startIndex = spanInfo.startIndex
                        endIndex = spanInfo.endIndex
                        color = null
                        size = spanInfo.size!!
                    }
                    else -> {
                        continue
                    }
                }
                val textStyle = TextStyleModel(
                    id = null,
                    scheduleId = scheduleId,
                    startIndex = startIndex,
                    endIndex = endIndex,
                    color = color,
                    textSize = size
                )
                Log.d("삽입직전modifySpanInfo",textStyle.toString())
                textStyleDao.insertTextStyle(textStyle)
            }
        }
    }
}