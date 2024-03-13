package com.example.dontforget.Notification

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationData(

    val id: Int? ,

    var scheduleText: String,

    var scheduleTime:Long,

    var scheduleDate:String,

    var title:String?,

): Parcelable
