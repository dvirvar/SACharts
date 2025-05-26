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
import com.skellyapps.charts.common.model.ChartPixel
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.Position
import kotlin.jvm.JvmInline
import kotlin.math.abs

data class LineChartData(
    val leftAxis: Axis.YAxis? = null,
    val rightAxis: Axis.YAxis? = null,
    val bottomAxis: Axis.XAxis? = null,
    val xAxisOffset: DpOffset = DpOffset.Zero
) {
    data class Line(
        val points: MutableList<ChartValue>,
        val pointsOrder: PointsOrder,
        val tag: Byte,
        val customization: Customization,
        val fillCustomization: FillCustomization? = null,
    ) {
        sealed class PointsOrder {
            internal abstract fun getClosestIndexDistance(offsets: List<ChartPixel>, touchPoint: Offset, isInRange: (Offset, Offset) -> Boolean): Pair<Int, Float>?
            data object Unordered: PointsOrder() {
                override fun getClosestIndexDistance(
                    offsets: List<ChartPixel>,
                    touchPoint: Offset,
                    isInRange: (Offset, Offset) -> Boolean,
                ): Pair<Int, Float>? {
                    var closestIndexDistance: Pair<Int, Float>? = null
                    offsets.fastForEachIndexed { index, offset ->
                        val offset = offset.offset
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

            sealed class Ordered: PointsOrder() {
                internal fun getClosestByIndex(
                    index: Int,
                    offsets: List<ChartPixel>,
                    touchPoint: Offset,
                    isInRange: (Offset, Offset) -> Boolean,
                ): Pair<Int, Float>? {
                    var closestIndexDistance: Pair<Int, Float>? = null
                    if (index < 0) {
                        var absIndex = abs(index + 1)
                        if (absIndex < offsets.size) {
                            if (isInRange(offsets[absIndex].offset, touchPoint)) {
                                val distance = (offsets[absIndex].offset - touchPoint).getDistanceSquared()
                                if (closestIndexDistance == null || closestIndexDistance.second > distance) {
                                    closestIndexDistance = Pair(absIndex, distance)
                                }
                            }
                        }
                        if (absIndex != 0) {
                            absIndex -= 1
                            if (isInRange(offsets[absIndex].offset, touchPoint)) {
                                val distance = (offsets[absIndex].offset - touchPoint).getDistanceSquared()
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
                data object X: Ordered() {
                    override fun getClosestIndexDistance(
                        offsets: List<ChartPixel>,
                        touchPoint: Offset,
                        isInRange: (Offset, Offset) -> Boolean,
                    ): Pair<Int, Float>? {
                        val index = offsets.binarySearchBy(touchPoint.x) {
                            it.x.value
                        }
                        return getClosestByIndex(index, offsets, touchPoint, isInRange)
                    }
                }

                data object Y: Ordered() {
                    override fun getClosestIndexDistance(
                        offsets: List<ChartPixel>,
                        touchPoint: Offset,
                        isInRange: (Offset, Offset) -> Boolean,
                    ): Pair<Int, Float>? {
                        val index = offsets.binarySearchBy(touchPoint.y) {
                            it.y.value
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
        val offsets: List<ChartPixel>,
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
            val yOffset: DpOffset = DpOffset.Zero,
            val lines: List<Line>,
            override val minValue: Double? = null,
            override val maxValue: Double? = null,
            override val value: Value,
            override val gridLines: GridLines? = null,
            override val dividerCustomization: DividerCustomization? = null,
            override val valueView: @Composable ((value: Double) -> Unit)? = null,
        ): Axis

        sealed interface Value {
            fun getValues(minValue: ChartValueCoordinate, maxValue: ChartValueCoordinate): List<ChartValueCoordinate>
            @JvmInline
            value class Step(val step: Double): Value {
                init {
                    if (step <= 0) {
                        throw IllegalArgumentException("Step must be greater than 0")
                    }
                }
                override fun getValues(minValue: ChartValueCoordinate, maxValue: ChartValueCoordinate): List<ChartValueCoordinate> {
                    val values = mutableListOf<ChartValueCoordinate>()
                    var value = minValue
                    while (value <= maxValue) {
                        values.add(value)
                        value += ChartValueCoordinate(step)
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
                override fun getValues(minValue: ChartValueCoordinate, maxValue: ChartValueCoordinate) = when (values) {
                    1 -> listOf(minValue)
                    else -> (0..<values).map { ChartValueCoordinate(minValue.value + (maxValue - minValue).value * it / (values - 1).toDouble()) }
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

    class PointClick(
        val isPointInRange: Density.(point: Offset, press: Offset) -> Boolean,
        val viewPosition: Position,
        val viewOffset: DpOffset,
        val viewStayInChartBounds: Boolean,
        val view: @Composable (lineTag: Byte, index: Int) -> Unit,
    ) {
        internal fun getViewOffset(density: Density, canvasWidth: Int, canvasHeight: Int, viewSize: IntSize, viewOffsetInCanvas: ChartPixel): IntOffset {
            val viewOffset = with(density) {
                IntOffset(viewOffset.x.roundToPx(), viewOffset.y.roundToPx())
            }
            var x: Int
            var y: Int
            when (viewPosition) {
                Position.TopLeft -> {
                    x = viewOffsetInCanvas.x.value.fastRoundToInt() - viewSize.width - viewOffset.x
                    y = viewOffsetInCanvas.y.value.fastRoundToInt() - viewSize.height - viewOffset.y
                }
                Position.Top -> {
                    x = viewOffsetInCanvas.x.value.fastRoundToInt() - viewSize.width / 2 + viewOffset.x
                    y = viewOffsetInCanvas.y.value.fastRoundToInt() - viewSize.height - viewOffset.y
                }
                Position.TopRight -> {
                    x = viewOffsetInCanvas.x.value.fastRoundToInt() + viewOffset.x
                    y = viewOffsetInCanvas.y.value.fastRoundToInt() - viewSize.height - viewOffset.y
                }
                Position.MiddleLeft -> {
                    x = viewOffsetInCanvas.x.value.fastRoundToInt() - viewSize.width - viewOffset.x
                    y = viewOffsetInCanvas.y.value.fastRoundToInt() - viewSize.height / 2 + viewOffset.y
                }
                Position.Middle -> {
                    x = viewOffsetInCanvas.x.value.fastRoundToInt() - viewSize.width / 2 + viewOffset.x
                    y = viewOffsetInCanvas.y.value.fastRoundToInt() - viewSize.height / 2 + viewOffset.y
                }
                Position.MiddleRight -> {
                    x = viewOffsetInCanvas.x.value.fastRoundToInt() + viewOffset.x
                    y = viewOffsetInCanvas.y.value.fastRoundToInt() - viewSize.height / 2 + viewOffset.y
                }
                Position.BottomLeft -> {
                    x = viewOffsetInCanvas.x.value.fastRoundToInt() - viewSize.width - viewOffset.x
                    y = viewOffsetInCanvas.y.value.fastRoundToInt() + viewOffset.y
                }
                Position.Bottom -> {
                    x = viewOffsetInCanvas.x.value.fastRoundToInt() - viewSize.width / 2 + viewOffset.x
                    y = viewOffsetInCanvas.y.value.fastRoundToInt() + viewOffset.y
                }
                Position.BottomRight -> {
                    x = viewOffsetInCanvas.x.value.fastRoundToInt() + viewOffset.x
                    y = viewOffsetInCanvas.y.value.fastRoundToInt() + viewOffset.y
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
        val pointDragged: (lineTag: Byte, index: Int, newPosition: ChartValue) -> Unit
    )
}