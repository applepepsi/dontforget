package com.example.dontforget.model.db

import androidx.room.*


@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule")
    fun getAll(): List<ScheduleModel>

    @Insert
    fun insertSchedule(schedule:ScheduleModel)

    @Update
    fun updateSchedule(schedule:ScheduleModel)

    @Delete
    fun delete(schedule:ScheduleModel)
}