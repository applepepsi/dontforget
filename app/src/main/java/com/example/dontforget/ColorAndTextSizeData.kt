package com.example.dontforget

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TextColorData(
    val textIdToColorMap: Map<Int, Int>,
) : Parcelable

@Parcelize
data class TextSizeData(
    val textIdToSizeMap: Map<Int, Float>
): Parcelable