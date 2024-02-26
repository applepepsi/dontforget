package com.example.dontforget

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SpanInfo(
    val start: Int,
    val end: Int,
    val spanType: String,
    val color: Int? = null,
    val size: Float? = null
): Parcelable
