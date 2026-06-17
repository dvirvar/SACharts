package com.skellyapps.charts.line.extension

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastMap
import com.skellyapps.charts.common.extension.toChartPixels
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.line.model.LineChartData

internal inline fun LineChartData.Line.getMinX(): ChartValueCoordinate? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.X -> points.first().x
        LineChartData.Line.PointsOrder.Ordered.Y,
        LineChartData.Line.PointsOrder.Unordered -> points.minOf { it.x }
    }
}

internal inline fun LineChartData.Line.getMaxX(): ChartValueCoordinate? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.X -> points.last().x
        LineChartData.Line.PointsOrder.Ordered.Y,
        LineChartData.Line.PointsOrder.Unordered -> points.maxOf { it.x }
    }
}

internal inline fun LineChartData.Line.getMinY(): ChartValueCoordinate? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.Y -> points.first().y
        LineChartData.Line.PointsOrder.Ordered.X,
        LineChartData.Line.PointsOrder.Unordered -> points.minOf { it.y }
    }
}

internal inline fun LineChartData.Line.getMaxY(): ChartValueCoordinate? {
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
    canvasSize: IntSize, xOffset: Offset, yOffset: Offset,
    minXValue: ChartValueCoordinate, maxXValue: ChartValueCoordinate,
    minYValue: ChartValueCoordinate, maxYValue: ChartValueCoordinate,
) =
    LineChartData.OffsetLine(
        points.toChartPixels(canvasSize, xOffset, yOffset, minXValue, maxXValue, minYValue, maxYValue),
        pointsOrder,
        tag,
        customization,
        fillCustomization
    )

internal inline fun LineChartData.Line.toOffsetLine(
    canvasSize: IntSize,
    minXValue: ChartValueCoordinate, maxXValue: ChartValueCoordinate,
    minYValue: ChartValueCoordinate, maxYValue: ChartValueCoordinate,
) =
    LineChartData.OffsetLine(
        points.toChartPixels(canvasSize, minXValue, maxXValue, minYValue, maxYValue),
        pointsOrder,
        tag,
        customization,
        fillCustomization
    )

internal inline fun List<LineChartData.Line>.toOffsetLines(
    canvasSize: IntSize, xOffset: Offset, yOffset: Offset,
    minXValue: ChartValueCoordinate, maxXValue: ChartValueCoordinate,
    minYValue: ChartValueCoordinate, maxYValue: ChartValueCoordinate,
) = fastMap { it.toOffsetLine(canvasSize, xOffset, yOffset, minXValue, maxXValue, minYValue, maxYValue) }

internal inline fun List<LineChartData.Line>.toOffsetLines(
    canvasSize: IntSize,
    minXValue: ChartValueCoordinate, maxXValue: ChartValueCoordinate,
    minYValue: ChartValueCoordinate, maxYValue: ChartValueCoordinate,
) = fastMap { it.toOffsetLine(canvasSize, minXValue, maxXValue, minYValue, maxYValue) }