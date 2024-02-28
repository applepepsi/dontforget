package com.example.dontforget.spanInfo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ColorInfo(
    val start: Int,
    val end: Int,
    var color: Int? = null
): Parcelable


