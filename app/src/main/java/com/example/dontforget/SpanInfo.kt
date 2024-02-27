package com.example.dontforget

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SpanInfo(
    val start: Int,
    val end: Int,
    var color: Int? = null,
    var size: Float? = null
): Parcelable
