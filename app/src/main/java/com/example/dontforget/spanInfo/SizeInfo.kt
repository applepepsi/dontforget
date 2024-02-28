package com.example.dontforget.spanInfo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
@Parcelize
data class SizeInfo(
    val start: Int,
    val end: Int,
    var size: Float? = null
): Parcelable

