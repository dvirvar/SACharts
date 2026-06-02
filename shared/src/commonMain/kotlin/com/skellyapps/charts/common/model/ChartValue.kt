package com.skellyapps.charts.common.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.jvm.JvmInline

@Stable inline fun ChartValue(x: Double, y: Double) = ChartValue(ChartValueCoordinate(x), ChartValueCoordinate(y))

data class ChartValue(
    val x: ChartValueCoordinate,
    val y: ChartValueCoordinate,
) {
    internal fun toChartPixel(
        chartSize: IntSize,
        xAxisOffset: Offset,
        yAxisOffset: Offset,
        minXValue: ChartValueCoordinate,
        maxXValue: ChartValueCoordinate,
        minYValue: ChartValueCoordinate,
        maxYValue: ChartValueCoordinate
    ) = ChartPixel(
        x.toChartPixelCoordinate(chartSize.width, xAxisOffset, minXValue, maxXValue, false),
        y.toChartPixelCoordinate(chartSize.height, yAxisOffset, minYValue, maxYValue, true)
    )

    internal fun toChartPixel(
        chartSize: IntSize,
        minXValue: ChartValueCoordinate,
        maxXValue: ChartValueCoordinate,
        minYValue: ChartValueCoordinate,
        maxYValue: ChartValueCoordinate
    ) = ChartPixel(
        x.toChartPixelCoordinate(chartSize.width, minXValue, maxXValue, false),
        y.toChartPixelCoordinate(chartSize.height, minYValue, maxYValue, true)
    )
}

@Immutable
@JvmInline
value class ChartValueCoordinate(val value: Double): Comparable<ChartValueCoordinate> {
    internal inline fun toChartPixelCoordinate(
        chartSize: Int,
        offset: Offset,
        minCoordinate: ChartValueCoordinate,
        maxCoordinate: ChartValueCoordinate,
        inverted: Boolean
    ) = toChartPixelCoordinate(chartSize.toFloat(), offset, minCoordinate, maxCoordinate, inverted)

    internal inline fun toChartPixelCoordinate(
        chartSize: Int,
        minCoordinate: ChartValueCoordinate,
        maxCoordinate: ChartValueCoordinate,
        inverted: Boolean
    ) = toChartPixelCoordinate(chartSize.toFloat(), minCoordinate, maxCoordinate, inverted)

    internal fun toChartPixelCoordinate(
        chartSize: Float,
        offset: Offset,
        minCoordinate: ChartValueCoordinate,
        maxCoordinate: ChartValueCoordinate,
        inverted: Boolean
    ): ChartPixelCoordinate {
        val pixelCoordinate = (((value - minCoordinate.value) / (maxCoordinate - minCoordinate).value) * (chartSize - offset.x - offset.y) + offset.x).toFloat()
        return if (inverted) {
            ChartPixelCoordinate(chartSize - pixelCoordinate)
        } else {
            ChartPixelCoordinate(pixelCoordinate)
        }
    }

    internal fun toChartPixelCoordinate(
        chartSize: Float,
        minCoordinate: ChartValueCoordinate,
        maxCoordinate: ChartValueCoordinate,
        inverted: Boolean
    ): ChartPixelCoordinate {
        val pixelCoordinate = (((value - minCoordinate.value) / (maxCoordinate - minCoordinate).value) * chartSize).toFloat()
        return if (inverted) {
            ChartPixelCoordinate(chartSize - pixelCoordinate)
        } else {
            ChartPixelCoordinate(pixelCoordinate)
        }
    }

    @Stable
    inline operator fun plus(other: ChartValueCoordinate) = ChartValueCoordinate(value + other.value)
    @Stable
    inline operator fun minus(other: ChartValueCoordinate) = ChartValueCoordinate(value - other.value)
    override fun compareTo(other: ChartValueCoordinate) = value.compareTo(other.value)
}