package com.example.dontforget.model.db

import androidx.room.*


@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule")
    suspend fun getAll(): List<ScheduleModel>

    @Insert
    suspend fun insertSchedule(schedule: ScheduleModel): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleModel)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleModel)


}