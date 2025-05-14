package com.skellyapps.charts.line.extension

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.skellyapps.charts.line.model.LineChartData

internal inline fun Offset.toLineChartPoint(
    data: LineChartData,
    isLeftAxis: Boolean,
    canvasSize: IntSize,
    minXValue: Double,
    maxXValue: Double,
    minYValue: Double,
    maxYValue: Double
): LineChartData.Line.Point {
    val minXOffset = data.xAxisLinesOffset.min
    val maxXOffset = data.xAxisLinesOffset.max
    val minYOffset = if (isLeftAxis) data.leftAxis!!.yOffset.min else data.rightAxis!!.yOffset.min
    val maxYOffset = if (isLeftAxis) data.leftAxis!!.yOffset.max else data.rightAxis!!.yOffset.max
    val x = ((x - minXOffset) / (canvasSize.width - minXOffset - maxXOffset)) * (maxXValue - minXValue) + minXValue
    val y = ((canvasSize.height - y - minYOffset) / (canvasSize.height - minYOffset - maxYOffset)) * (maxYValue - minYValue) + minYValue
    return LineChartData.Line.Point(x, y)
}