package com.skellyapps.charts.example

import androidx.compose.ui.util.fastRoundToInt
import com.skellyapps.charts.common.model.ChartValueCoordinate
import kotlin.math.pow

internal fun Double.roundToDecimals(decimals: Int): Double {
    val divider = 10.0.pow(decimals)
    return (this * divider).fastRoundToInt() / divider
}

internal fun ChartValueCoordinate.roundToDecimals(decimals: Int): Double {
    return value.roundToDecimals(decimals)
}