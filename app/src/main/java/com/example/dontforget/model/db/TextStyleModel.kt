package com.example.dontforget.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "text_style")
data class TextStyleModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val scheduleId: Int,
    val startIndex: Int,
    val endIndex: Int,
    val color: Int,
    val textSize: Float
)