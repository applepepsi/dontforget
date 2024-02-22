package com.example.dontforget

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ColorAndTextSizeData(
    val textIdToColorMap: Map<Int, Int>,
    val textIdToSizeMap: Map<Int, Float>
) : Parcelable
