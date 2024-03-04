package com.example.dontforget.spanInfo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TextStyleInfo(
    val startIndex: Int,
    val endIndex: Int,
    val color: Int?,
    val textSize: Float?
) : Parcelable
