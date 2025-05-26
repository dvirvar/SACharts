package com.skellyapps.charts.common.extension

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastMap
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.ChartValueCoordinate

internal inline fun List<ChartValue>.toChartPixels(
    canvasSize: IntSize, xOffset: Offset, yOffset: Offset,
    minXValue: ChartValueCoordinate, maxXValue: ChartValueCoordinate,
    minYValue: ChartValueCoordinate, maxYValue: ChartValueCoordinate
) = fastMap { it.toChartPixel(canvasSize, xOffset, yOffset, minXValue, maxXValue, minYValue, maxYValue) }