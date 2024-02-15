package com.example.dontforget.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.format.TextStyle

@Dao
interface TextStyleDao {
    @Insert
    suspend fun insert(textStyle: TextStyle)

    @Query("SELECT * FROM text_style WHERE scheduleId = :scheduleId")
    suspend fun getTextStylesByScheduleId(scheduleId: Int): List<TextStyle>
}