package com.skellyapps.charts.pie.model

import androidx.annotation.FloatRange

/**
 * @param slices The slices that build the pie chart
 * @param startAngle The start angle of the pie chart
 * @param outerRadiusMinPercentage The smallest size the pie can be in percentage relative to canvas size
 * @param innerRadiusPercentage Relative to the actual outer radius percentage
 * @param sliceSpacingDegrees Space between slices in degrees
 * @param sliceBorder Inside border of a slice
 * @param labelCustomization Label customization of a slice
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