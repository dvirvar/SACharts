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
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.common.model.Position
import kotlin.math.abs

data class LineChartData(
    val leftAxis: YAxis? = null,
    val rightAxis: YAxis? = null,
    val bottomAxis: XAxis? = null,
    val xAxisOffset: DpOffset = DpOffset.Zero
) {
    data class Line(
        val points: MutableList<ChartValue>,
        val pointsOrder: PointsOrder,
        val tag: Int,
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
        data class Customization(
            val brush: Brush,
            @FloatRange val alpha: Float = 1f,
            val thickness: Dp = 5.dp,
            val miter: Float = Stroke.DefaultMiter,
            val cap: StrokeCap = Stroke.DefaultCap,
            val join: StrokeJoin = Stroke.DefaultJoin,
            val pathEffect: PathEffect? = null,
            val colorFilter: ColorFilter? = null,
            val blendMode: BlendMode = DefaultBlendMode,
        ) {
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
        data class FillCustomization(
            val brush: Brush,
            @FloatRange val alpha: Float = 1f,
            val colorFilter: ColorFilter? = null,
            val blendMode: BlendMode = DefaultBlendMode
        ) {
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
        val tag: Int,
        val customization: Line.Customization,
        val fillCustomization: Line.FillCustomization?
    )

     data class XAxis(
         val minValue: Double? = null,
         val maxValue: Double? = null,
         override val gridLines: GridChartData.Axis.GridLines? = null,
         override val dividerCustomization: GridChartData.Axis.DividerCustomization? = null,
         val value: GridChartData.Axis.Value,
         val valueView: @Composable ((value: Double) -> Unit)? = null,
     ): GridChartData.Axis.XAxis

    data class YAxis(
        val lines: MutableList<Line>,
        override val offset: DpOffset = DpOffset.Zero,
        override val minValue: Double? = null,
        override val maxValue: Double? = null,
        override val gridLines: GridChartData.Axis.GridLines? = null,
        override val dividerCustomization: GridChartData.Axis.DividerCustomization? = null,
        val value: GridChartData.Axis.Value,
        val valueView: @Composable ((value: Double) -> Unit)? = null,
    ): GridChartData.Axis.YAxis

    class PointClick(
        val isPointInRange: Density.(point: Offset, press: Offset) -> Boolean,
        val viewPosition: Position,
        val viewOffset: DpOffset,
        val viewStayInChartBounds: Boolean,
        val view: @Composable (lineTag: Int, index: Int) -> Unit,
    ) {
        internal fun getViewOffset(density: Density, canvasSize: IntSize, viewSize: IntSize, viewOffsetInCanvas: ChartPixel): IntOffset {
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
                x = x.coerceIn(0, canvasSize.width - viewSize.width)
                y = y.coerceIn(0, canvasSize.height - viewSize.height)
            }
            return IntOffset(x, y)
        }
    }

    class PointDrag(
        val isPointInRange: Density.(point: Offset, press: Offset) -> Boolean,
        val pointDragged: (lineTag: Int, index: Int, newPosition: ChartValue) -> Unit
    )
}