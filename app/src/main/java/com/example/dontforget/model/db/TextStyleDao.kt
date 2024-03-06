package com.example.dontforget.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.dontforget.spanInfo.TextStyleInfo
import java.time.format.TextStyle

@Dao
interface TextStyleDao {
    @Insert
    suspend fun insertTextStyle(textStyle: TextStyleModel)

    @Update
    suspend fun updateTextStyle(textStyle: TextStyleModel)

    @Query("SELECT * FROM text_style WHERE scheduleId = :scheduleId")
    suspend fun getTextStylesByScheduleId(scheduleId: Int): List<TextStyleModel>

    @Query("SELECT startIndex, endIndex, color, textSize FROM text_style WHERE scheduleId = :scheduleId")
    suspend fun getTextStyleInfo(scheduleId: Int): List<TextStyleInfo>

    @Query("DELETE FROM text_style WHERE scheduleId = :scheduleId")
    suspend fun deleteTextStylesByScheduleId(scheduleId: Int)
}