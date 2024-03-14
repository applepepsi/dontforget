package com.example.dontforget.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule")
data class ScheduleModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? =0,

    @ColumnInfo(name="scheduleInfo")
    var scheduleText: String,

    @ColumnInfo(name="dateInfo")
    var scheduleTime:Long,

    @ColumnInfo(name="textSize")
    var textSize:Float,

    @ColumnInfo(name="scheduleDate")
    var scheduleDate:String,

    @ColumnInfo(name="lineCount")
    var lineCount:Int?,

    @ColumnInfo(name="title")
    var title:String?,

    @ColumnInfo(name="setNotification")
    var setNotification:Int?,

    @ColumnInfo(name="dday")
    var dday:Long?,
)
