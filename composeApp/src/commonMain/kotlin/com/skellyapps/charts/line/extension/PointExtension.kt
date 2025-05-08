package com.skellyapps.charts.line.extension

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastMap
import com.skellyapps.charts.line.model.LineChartData

internal inline fun LineChartData.Line.Point.toCanvasOffset(
    canvasSize: IntSize, minXValue: Double, maxXValue: Double, xOffset: LineChartData.AxisOffset,
    minYValue: Double, maxYValue: Double, yOffset: LineChartData.AxisOffset
) =
    Offset(
        (((x - minXValue) / (maxXValue - minXValue)) * (canvasSize.width - xOffset.min - xOffset.max) + xOffset.min).toFloat(),
        canvasSize.height - (((y - minYValue) / (maxYValue - minYValue)) * (canvasSize.height - yOffset.min - yOffset.max) + yOffset.min).toFloat()
    )

internal inline fun List<LineChartData.Line.Point>.toCanvasOffsets(
    canvasSize: IntSize, minXValue: Double, maxXValue: Double, xOffset: LineChartData.AxisOffset,
    minYValue: Double, maxYValue: Double, yOffset: LineChartData.AxisOffset
) = fastMap { it.toCanvasOffset(canvasSize, minXValue, maxXValue, xOffset, minYValue, maxYValue, yOffset) }