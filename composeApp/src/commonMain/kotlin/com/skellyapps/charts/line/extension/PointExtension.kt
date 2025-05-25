package com.skellyapps.charts.line.extension

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastMap
import com.skellyapps.charts.line.model.LineChartData

internal inline fun LineChartData.Line.Point.toCanvasOffset(
    canvasSize: IntSize, minXValue: Double, maxXValue: Double, xOffset: Offset,
    minYValue: Double, maxYValue: Double, yOffset: Offset
) =
    Offset(
        (((x - minXValue) / (maxXValue - minXValue)) * (canvasSize.width - xOffset.x - xOffset.y) + xOffset.x).toFloat(),
        canvasSize.height - (((y - minYValue) / (maxYValue - minYValue)) * (canvasSize.height - yOffset.x - yOffset.y) + yOffset.x).toFloat()
    )

internal inline fun List<LineChartData.Line.Point>.toCanvasOffsets(
    canvasSize: IntSize, minXValue: Double, maxXValue: Double, xOffset: Offset,
    minYValue: Double, maxYValue: Double, yOffset: Offset
) = fastMap { it.toCanvasOffset(canvasSize, minXValue, maxXValue, xOffset, minYValue, maxYValue, yOffset) }