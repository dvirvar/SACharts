package com.skellyapps.charts.pie.model

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

data class PieChartData(
    val slices: MutableList<Slice>,
    @FloatRange(0.0, 360.0, toInclusive = false) val startAngle: Float = 270f,
    @FloatRange(0.0, 1.0, toInclusive = false) val innerRadiusPercentage: Float = 0f,
    val sliceSpacingDegrees: Float = 0f,
    val sliceBorder: Slice.Border? = null
) {
    data class Slice(val value: Double, val color: Color) {
        data class Border(
            val thickness: Dp,
            val color: Color
        )
    }
}
