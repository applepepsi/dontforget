package com.example.dontforget.spanInfo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ColorInfo(
    val startIndex: Int,
    val endIndex: Int,
    var color: Int? = null
): Parcelable


