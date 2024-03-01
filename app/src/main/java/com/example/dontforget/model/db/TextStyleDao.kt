package com.example.dontforget.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.format.TextStyle

@Dao
interface TextStyleDao {
    @Insert
    suspend fun insertTextStyle(textStyle: TextStyleModel)

    @Query("SELECT * FROM text_style WHERE scheduleId = :scheduleId")
    suspend fun getTextStylesByScheduleId(scheduleId: Int): List<TextStyleModel>

    @Query("DELETE FROM text_style WHERE scheduleId = :scheduleId")
    suspend fun deleteTextStylesByScheduleId(scheduleId: Int)
}