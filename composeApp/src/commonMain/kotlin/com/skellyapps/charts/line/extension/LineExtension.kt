package com.skellyapps.charts.line.extension

import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastMap
import com.skellyapps.charts.line.model.LineChartData

internal inline fun LineChartData.Line.getMinX(): Double? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.X -> points.first().x
        LineChartData.Line.PointsOrder.Ordered.Y,
        LineChartData.Line.PointsOrder.Unordered -> points.minOf { it.x }
    }
}

internal inline fun LineChartData.Line.getMaxX(): Double? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.X -> points.last().x
        LineChartData.Line.PointsOrder.Ordered.Y,
        LineChartData.Line.PointsOrder.Unordered -> points.maxOf { it.x }
    }
}

internal inline fun LineChartData.Line.getMinY(): Double? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.Y -> points.first().y
        LineChartData.Line.PointsOrder.Ordered.X,
        LineChartData.Line.PointsOrder.Unordered -> points.minOf { it.y }
    }
}

internal inline fun LineChartData.Line.getMaxY(): Double? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.Y -> points.last().y
        LineChartData.Line.PointsOrder.Ordered.X,
        LineChartData.Line.PointsOrder.Unordered -> points.maxOf { it.y }
    }
}

internal inline fun LineChartData.Line.toOffsetLine(
    canvasSize: IntSize, minXValue: Double, maxXValue: Double, xOffset: LineChartData.AxisOffset,
    minYValue: Double, maxYValue: Double, yOffset: LineChartData.AxisOffset
) =
    LineChartData.OffsetLine(
        points.toCanvasOffsets(canvasSize, minXValue, maxXValue, xOffset, minYValue, maxYValue, yOffset),
        pointsOrder,
        tag,
        customization
    )

internal inline fun List<LineChartData.Line>.toOffsetLines(
    canvasSize: IntSize, minXValue: Double, maxXValue: Double, xOffset: LineChartData.AxisOffset,
    minYValue: Double, maxYValue: Double, yOffset: LineChartData.AxisOffset
) = fastMap { it.toOffsetLine(canvasSize, minXValue, maxXValue, xOffset, minYValue, maxYValue, yOffset) }