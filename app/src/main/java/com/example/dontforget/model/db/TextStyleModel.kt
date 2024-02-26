package com.example.dontforget.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "text_style")
data class TextStyleModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = 0,

    @ColumnInfo(name="scheduleId")
    val scheduleId: Int,
    @ColumnInfo(name="startIndex")
    val startIndex: Int,
    @ColumnInfo(name="endIndex")
    val endIndex: Int,
    @ColumnInfo(name="color")
    val color: Int?,
    @ColumnInfo(name="textSize")
    val textSize: Float?
)