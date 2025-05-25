package com.skellyapps.charts.line.extension

import androidx.compose.ui.geometry.Offset
import com.skellyapps.charts.line.model.LineChartData

internal inline fun Offset.toLineChartPoint(
    canvasWidth: Int,
    canvasHeight: Int,
    xAxisOffset: Offset,
    yAxisOffset: Offset,
    minXValue: Double,
    maxXValue: Double,
    minYValue: Double,
    maxYValue: Double
): LineChartData.Line.Point {
    val minXOffset = xAxisOffset.x
    val maxXOffset = xAxisOffset.y
    val minYOffset = yAxisOffset.x
    val maxYOffset = yAxisOffset.y
    val x = ((x - minXOffset) / (canvasWidth - minXOffset - maxXOffset)) * (maxXValue - minXValue) + minXValue
    val y = ((canvasHeight - y - minYOffset) / (canvasHeight - minYOffset - maxYOffset)) * (maxYValue - minYValue) + minYValue
    return LineChartData.Line.Point(x, y)
}