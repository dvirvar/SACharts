package com.skellyapps.charts.common.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.jvm.JvmInline

@Immutable
@JvmInline
internal value class ChartPixel(val offset: Offset) {
    constructor(x: ChartPixelCoordinate, y: ChartPixelCoordinate): this(Offset(x.value, y.value))
    @Stable
    inline val x get() = ChartPixelCoordinate(offset.x)
    @Stable
    inline val y get() = ChartPixelCoordinate(offset.y)

    internal fun toChartValue(
        chartSize: IntSize,
        xAxisOffset: Offset,
        yAxisOffset: Offset,
        minXValue: ChartValueCoordinate,
        maxXValue: ChartValueCoordinate,
        minYValue: ChartValueCoordinate,
        maxYValue: ChartValueCoordinate
    ) = ChartValue(
        x.toChartValueCoordinate(chartSize.width, xAxisOffset, minXValue, maxXValue, false),
        y.toChartValueCoordinate(chartSize.height, yAxisOffset, minYValue, maxYValue, true),
    )

    internal fun toChartValue(
        chartSize: IntSize,
        minXValue: ChartValueCoordinate,
        maxXValue: ChartValueCoordinate,
        minYValue: ChartValueCoordinate,
        maxYValue: ChartValueCoordinate
    ) = ChartValue(
        x.toChartValueCoordinate(chartSize.width, minXValue, maxXValue, false),
        y.toChartValueCoordinate(chartSize.height, minYValue, maxYValue, true),
    )
}

@Immutable
@JvmInline
internal value class ChartPixelCoordinate(val value: Float) {
    internal fun toChartValueCoordinate(
        chartSize: Int,
        offset: Offset,
        minCoordinate: ChartValueCoordinate,
        maxCoordinate: ChartValueCoordinate,
        inverted: Boolean
    ) = if (inverted) {
        ChartValueCoordinate(((chartSize - value - offset.x) / (chartSize - offset.x - offset.y)) * (maxCoordinate - minCoordinate).value + minCoordinate.value)
    } else {
        ChartValueCoordinate(((value - offset.x) / (chartSize - offset.x - offset.y)) * (maxCoordinate - minCoordinate).value + minCoordinate.value)
    }

    internal fun toChartValueCoordinate(
        chartSize: Int,
        minCoordinate: ChartValueCoordinate,
        maxCoordinate: ChartValueCoordinate,
        inverted: Boolean
    ) = if (inverted) {
        ChartValueCoordinate(((chartSize - value) / chartSize) * (maxCoordinate - minCoordinate).value + minCoordinate.value)
    } else {
        ChartValueCoordinate((value / chartSize) * (maxCoordinate - minCoordinate).value + minCoordinate.value)
    }
}