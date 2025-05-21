package com.skellyapps.charts.line.model

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastRoundToInt
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
        val customization: Customization,
        val fillCustomization: FillCustomization? = null,
    ) {
        data class Point(
            val x: Double,
            val y: Double
        )
        sealed interface PointsOrder {
            fun getClosestIndexDistance(offsets: List<Offset>, touchPoint: Offset, isInRange: (Offset, Offset) -> Boolean): Pair<Int, Float>?
            data object Unordered: PointsOrder {
                override fun getClosestIndexDistance(
                    offsets: List<Offset>,
                    touchPoint: Offset,
                    isInRange: (Offset, Offset) -> Boolean,
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
                    isInRange: (Offset, Offset) -> Boolean,
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
            @FloatRange val alpha: Float
            val thickness: Dp
            val miter: Float
            val cap: StrokeCap
            val join: StrokeJoin
            val pathEffect: PathEffect?
            val colorFilter: ColorFilter?
            val blendMode: BlendMode

            constructor(
                brush: Brush,
                @FloatRange alpha: Float = 1f,
                thickness: Dp = 5.dp,
                miter: Float = Stroke.DefaultMiter,
                cap: StrokeCap = Stroke.DefaultCap,
                join: StrokeJoin = Stroke.DefaultJoin,
                pathEffect: PathEffect? = null,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ) {
                this.brush = brush
                this.alpha = alpha
                this.thickness = thickness
                this.miter = miter
                this.cap = cap
                this.join = join
                this.pathEffect = pathEffect
                this.colorFilter = colorFilter
                this.blendMode = blendMode
            }

            constructor(
                color: Color,
                @FloatRange alpha: Float = 1f,
                thickness: Dp = 5.dp,
                miter: Float = Stroke.DefaultMiter,
                cap: StrokeCap = Stroke.DefaultCap,
                join: StrokeJoin = Stroke.DefaultJoin,
                pathEffect: PathEffect? = null,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ): this(SolidColor(color), alpha, thickness, miter, cap, join, pathEffect, colorFilter, blendMode)
        }
        class FillCustomization {
            val brush: Brush
            @FloatRange val alpha: Float
            val colorFilter: ColorFilter?
            val blendMode: BlendMode

            constructor(
                brush: Brush,
                @FloatRange alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ) {
                this.brush = brush
                this.alpha = alpha
                this.colorFilter = colorFilter
                this.blendMode = blendMode
            }

            constructor(
                color: Color,
                @FloatRange alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ): this(SolidColor(color), alpha, colorFilter, blendMode)
        }
    }

    internal data class OffsetLine(
        val offsets: List<Offset>,
        val pointsOrder: Line.PointsOrder,
        val tag: Byte,
        val customization: Line.Customization,
        val fillCustomization: Line.FillCustomization?
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

    class PointClick(
        val isPointInRange: Density.(point: Offset, press: Offset) -> Boolean,
        val viewPosition: Position,
        val viewOffset: DpOffset,
        val viewStayInChartBounds: Boolean,
        val view: @Composable (lineTag: Byte, index: Int) -> Unit,
    ) {
        internal fun getViewOffset(density: Density, canvasWidth: Int, canvasHeight: Int, viewSize: IntSize, viewOffsetInCanvas: Offset): IntOffset {
            val viewOffset = with(density) {
                IntOffset(viewOffset.x.roundToPx(), viewOffset.y.roundToPx())
            }
            var x: Int
            var y: Int
            when (viewPosition) {
                Position.TopLeft -> {
                    x = viewOffsetInCanvas.x.fastRoundToInt() - viewSize.width - viewOffset.x
                    y = viewOffsetInCanvas.y.fastRoundToInt() - viewSize.height - viewOffset.y
                }
                Position.Top -> {
                    x = viewOffsetInCanvas.x.fastRoundToInt() - viewSize.width / 2 + viewOffset.x
                    y = viewOffsetInCanvas.y.fastRoundToInt() - viewSize.height - viewOffset.y
                }
                Position.TopRight -> {
                    x = viewOffsetInCanvas.x.fastRoundToInt() + viewOffset.x
                    y = viewOffsetInCanvas.y.fastRoundToInt() - viewSize.height - viewOffset.y
                }
                Position.MiddleLeft -> {
                    x = viewOffsetInCanvas.x.fastRoundToInt() - viewSize.width - viewOffset.x
                    y = viewOffsetInCanvas.y.fastRoundToInt() - viewSize.height / 2 + viewOffset.y
                }
                Position.Middle -> {
                    x = viewOffsetInCanvas.x.fastRoundToInt() - viewSize.width / 2 + viewOffset.x
                    y = viewOffsetInCanvas.y.fastRoundToInt() - viewSize.height / 2 + viewOffset.y
                }
                Position.MiddleRight -> {
                    x = viewOffsetInCanvas.x.fastRoundToInt() + viewOffset.x
                    y = viewOffsetInCanvas.y.fastRoundToInt() - viewSize.height / 2 + viewOffset.y
                }
                Position.BottomLeft -> {
                    x = viewOffsetInCanvas.x.fastRoundToInt() - viewSize.width - viewOffset.x
                    y = viewOffsetInCanvas.y.fastRoundToInt() + viewOffset.y
                }
                Position.Bottom -> {
                    x = viewOffsetInCanvas.x.fastRoundToInt() - viewSize.width / 2 + viewOffset.x
                    y = viewOffsetInCanvas.y.fastRoundToInt() + viewOffset.y
                }
                Position.BottomRight -> {
                    x = viewOffsetInCanvas.x.fastRoundToInt() + viewOffset.x
                    y = viewOffsetInCanvas.y.fastRoundToInt() + viewOffset.y
                }
            }
            if (viewStayInChartBounds) {
                x = x.coerceIn(0, canvasWidth - viewSize.width)
                y = y.coerceIn(0, canvasHeight - viewSize.height)
            }
            return IntOffset(x, y)
        }
    }

    class PointDrag(
        val isPointInRange: Density.(point: Offset, press: Offset) -> Boolean,
        val pointDragged: (lineTag: Byte, index: Int, newPosition: Line.Point) -> Unit
    )
}