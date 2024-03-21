package com.example.dontforget.model.db

import androidx.room.*


@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule")
    suspend fun getAll(): List<ScheduleModel>

    @Query("SELECT * FROM schedule WHERE scheduleInfo LIKE :searchText OR title LIKE :searchText")
    suspend fun findSchedulesByText(searchText: String): List<ScheduleModel>

    @Query("SELECT * FROM schedule WHERE setNotification=1 AND dateInfo > :currentDayMilli")
    suspend fun findSwitchOnData(currentDayMilli: Long): List<ScheduleModel>

    @Query("SELECT * FROM schedule WHERE dateInfo != 0")
    suspend fun findHaveDday(): List<ScheduleModel>
//
    @Query("SELECT * FROM schedule WHERE dateInfo = 0")
    suspend fun findWithOutDday(): List<ScheduleModel>
//
    @Query("SELECT * FROM schedule WHERE dday <= -1")
    suspend fun findHExpiredDday(): List<ScheduleModel>
//
    @Query("SELECT * FROM schedule WHERE dday BETWEEN 0 AND 3")
    suspend fun findImminentDday(): List<ScheduleModel>

    @Query("SELECT * FROM schedule WHERE setNotification = 1")
    suspend fun findSetNotification(): List<ScheduleModel>

    @Insert
    suspend fun insertSchedule(schedule: ScheduleModel): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleModel)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleModel)


}