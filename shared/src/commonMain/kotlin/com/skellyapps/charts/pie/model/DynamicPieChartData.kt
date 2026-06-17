package com.skellyapps.charts.pie.model

import androidx.annotation.FloatRange

/**
 * outerRadiusMinPercentage is the smallest size the pie can be in percentage
 * innerRadiusPercentage is relative to outerRadiusPercentage
 */
data class DynamicPieChartData(
    val slices: MutableList<PieChartData.Slice>,
    @param:FloatRange(0.0, 360.0, toInclusive = false) val startAngle: Float = 270f,
    @param:FloatRange(0.0, 1.0, toInclusive = false) val outerRadiusMinPercentage: Float = 0.3f,
    @param:FloatRange(0.0, 1.0, toInclusive = false) val innerRadiusPercentage: Float = 0f,
    val sliceSpacingDegrees: Float = 0f,
    val sliceBorder: PieChartData.Slice.Border? = null,
    val labelCustomization: PieChartData.LabelCustomization? = null
)