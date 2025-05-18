package com.skellyapps.charts.line.model

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.jvm.JvmInline
import kotlin.math.abs

data class LineChartData(
    val leftAxis: Axis.YAxis? = null,
    val rightAxis: Axis.YAxis? = null,
    val bottomAxis: Axis.XAxis? = null,
    val xAxisLinesOffset: AxisOffset = AxisOffset(0, 0)
) {
    data class Line(
        val points: MutableList<Point>,
        val pointsOrder: PointsOrder,
        val tag: Byte,
        val customization: Customization
    ) {
        data class Point(
            val x: Double,
            val y: Double
        )
        sealed interface PointsOrder {
            fun getClosestIndexDistance(offsets: List<Offset>, touchPoint: Offset, isInRange: Offset.(Offset) -> Boolean): Pair<Int, Float>?
            data object Unordered: PointsOrder {
                override fun getClosestIndexDistance(
                    offsets: List<Offset>,
                    touchPoint: Offset,
                    isInRange: Offset.(Offset) -> Boolean,
                ): Pair<Int, Float>? {
                    var closestIndexDistance: Pair<Int, Float>? = null
                    offsets.fastForEachIndexed { index, offset ->
                        if (isInRange(offset, touchPoint)) {
                            val distance = (offset - touchPoint).getDistanceSquared()
                            if (closestIndexDistance == null || closestIndexDistance.second > distance) {
                                closestIndexDistance = Pair(index, distance)
                            }
                        }
                    }
                    return closestIndexDistance
                }
            }

            sealed interface Ordered: PointsOrder {
                fun getClosestByIndex(
                    index: Int,
                    offsets: List<Offset>,
                    touchPoint: Offset,
                    isInRange: Offset.(Offset) -> Boolean,
                ): Pair<Int, Float>? {
                    var closestIndexDistance: Pair<Int, Float>? = null
                    if (index < 0) {
                        var absIndex = abs(index + 1)
                        if (absIndex < offsets.size) {
                            if (isInRange(offsets[absIndex], touchPoint)) {
                                val distance = (offsets[absIndex] - touchPoint).getDistanceSquared()
                                if (closestIndexDistance == null || closestIndexDistance.second > distance) {
                                    closestIndexDistance = Pair(absIndex, distance)
                                }
                            }
                        }
                        if (absIndex != 0) {
                            absIndex -= 1
                            if (isInRange(offsets[absIndex], touchPoint)) {
                                val distance = (offsets[absIndex] - touchPoint).getDistanceSquared()
                                if (closestIndexDistance == null || closestIndexDistance.second > distance) {
                                    closestIndexDistance = Pair(absIndex, distance)
                                }
                            }
                        }
                    } else {
                        closestIndexDistance = Pair(index, 0f)
                    }
                    return closestIndexDistance
                }
                data object X: Ordered {
                    override fun getClosestIndexDistance(
                        offsets: List<Offset>,
                        touchPoint: Offset,
                        isInRange: Offset.(Offset) -> Boolean,
                    ): Pair<Int, Float>? {
                        val index = offsets.binarySearchBy(touchPoint.x) {
                            it.x
                        }
                        return getClosestByIndex(index, offsets, touchPoint, isInRange)
                    }
                }

                data object Y: Ordered {
                    override fun getClosestIndexDistance(
                        offsets: List<Offset>,
                        touchPoint: Offset,
                        isInRange: Offset.(Offset) -> Boolean,
                    ): Pair<Int, Float>? {
                        val index = offsets.binarySearchBy(touchPoint.y) {
                            it.y
                        }
                        return getClosestByIndex(index, offsets, touchPoint, isInRange)
                    }
                }
            }
        }
        class Customization {
            val brush: Brush
            val pointMode: PointMode
            val thickness: Dp
            val cap: StrokeCap
            val pathEffect: PathEffect?
            @FloatRange val alpha: Float
            val colorFilter: ColorFilter?
            val blendMode: BlendMode

            constructor(
                brush: Brush,
                pointMode: PointMode = PointMode.Polygon,
                thickness: Dp = 5.dp,
                cap: StrokeCap = Stroke.DefaultCap,
                pathEffect: PathEffect? = null,
                @FloatRange alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ) {
                this.brush = brush
                this.pointMode = pointMode
                this.thickness = thickness
                this.cap = cap
                this.pathEffect = pathEffect
                this.alpha = alpha
                this.colorFilter = colorFilter
                this.blendMode = blendMode
            }

            constructor(
                color: Color,
                pointMode: PointMode = PointMode.Polygon,
                thickness: Dp = 5.dp,
                cap: StrokeCap = Stroke.DefaultCap,
                pathEffect: PathEffect? = null,
                @FloatRange alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ): this(SolidColor(color), pointMode, thickness, cap, pathEffect, alpha, colorFilter, blendMode)
        }
    }

    internal data class OffsetLine(
        val offsets: List<Offset>,
        val pointsOrder: Line.PointsOrder,
        val tag: Byte,
        val customization: Line.Customization
    )

    sealed interface Axis {
        val minValue: Double?
        val maxValue: Double?
        val value: Value
        val gridLines: GridLines?
        val dividerCustomization: DividerCustomization?
        val valueView: @Composable ((value: Double) -> Unit)?

        data class XAxis(
            override val minValue: Double? = null,
            override val maxValue: Double? = null,
            override val value: Value,
            override val gridLines: GridLines? = null,
            override val dividerCustomization: DividerCustomization? = null,
            override val valueView: @Composable ((value: Double) -> Unit)? = null
        ): Axis

        data class YAxis(
            val yOffset: AxisOffset = AxisOffset(0, 0),
            val lines: List<Line>,
            override val minValue: Double? = null,
            override val maxValue: Double? = null,
            override val value: Value,
            override val gridLines: GridLines? = null,
            override val dividerCustomization: DividerCustomization? = null,
            override val valueView: @Composable ((value: Double) -> Unit)? = null,
        ): Axis

        sealed interface Value {
            fun getValues(minValue: Double, maxValue: Double): List<Double>
            @JvmInline
            value class Step(val step: Double): Value {
                init {
                    if (step <= 0) {
                        throw IllegalArgumentException("Step must be greater than 0")
                    }
                }
                override fun getValues(minValue: Double, maxValue: Double): List<Double> {
                    val values = mutableListOf<Double>()
                    var value = minValue
                    while (value <= maxValue) {
                        values.add(value)
                        value += step
                    }
                    return values
                }
            }
            @JvmInline
            value class Fixed(val values: Int): Value {
                init {
                    if (values < 1) {
                        throw IllegalArgumentException("Values must be greater than 0")
                    }
                }
                override fun getValues(minValue: Double, maxValue: Double) = when (values) {
                    1 -> listOf(minValue)
                    else -> (0..<values).map { minValue + (maxValue - minValue) * it / (values - 1).toDouble() }
                }
            }
        }

        data class GridLines(
            val showFirstLine: Boolean = true,
            val showLastLine: Boolean = true,
            val customization: DividerCustomization
        )

        class DividerCustomization {
            val brush: Brush
            val thickness: Dp
            val cap: StrokeCap
            val pathEffect: PathEffect?
            @FloatRange val alpha: Float
            val colorFilter: ColorFilter?
            val blendMode: BlendMode

            constructor(
                brush: Brush,
                thickness: Dp = 2.dp,
                cap: StrokeCap = Stroke.DefaultCap,
                pathEffect: PathEffect? = null,
                @FloatRange alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ) {
                this.brush = brush
                this.thickness = thickness
                this.cap = cap
                this.pathEffect = pathEffect
                this.alpha = alpha
                this.colorFilter = colorFilter
                this.blendMode = blendMode
            }

            constructor(
                color: Color,
                thickness: Dp = 2.dp,
                cap: StrokeCap = Stroke.DefaultCap,
                pathEffect: PathEffect? = null,
                @FloatRange alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ): this(SolidColor(color), thickness, cap, pathEffect, alpha, colorFilter, blendMode)
        }
    }

    data class AxisOffset(
        val min: Int,
        val max: Int
    )

    class DragCallback(
        val isInRangePx: Offset.(Offset) -> Boolean,
        val pointDragged: (lineTag: Byte, index: Int, newPosition: Line.Point) -> Unit
    )
}