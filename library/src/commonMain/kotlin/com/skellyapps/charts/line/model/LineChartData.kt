package com.skellyapps.charts.line.model

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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

/**
 * @param leftAxis Left axis of the line chart
 * @param rightAxis Right axis of the line chart
 * @param bottomAxis Bottom axis of the line chart
 * @param xAxisOffset Padding of the values from the start(x) and end(y) of the x-axis' viewport
 */
data class LineChartData(
    val leftAxis: YAxis? = null,
    val rightAxis: YAxis? = null,
    val bottomAxis: XAxis? = null,
    val xAxisOffset: DpOffset = DpOffset.Zero
) {
    /**
     * Representation of a line in a line chart.
     *
     * @param points Mutable list of the [points] that make a line
     * @param pointsOrder Tells the view how the points are ordered for an efficient look at values
     * @param tag To distinguish between lines, mainly for users
     * @param customization Line customization
     * @param fillCustomization Fill customization of the area between the line and the x-axis
     */
    data class Line(
        /**
         * If you want to move points on a line you should make points list as a SnapshotListState.
         *
         * For example:
         * ```
         * (0..12).map { ChartValue(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableStateList()
         * ```
         *
         * For more information about moving points see [PointDrag].
         */
        val points: MutableList<ChartValue>,
        val pointsOrder: PointsOrder,
        val tag: Int,
        val customization: Customization,
        val fillCustomization: FillCustomization? = null,
    ) {
        /**
         * How the points are ordered in a line.
         */
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

                /**
                 * Ordered on x-axis
                 */
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
                /**
                 * Ordered on y-axis
                 */
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
        /**
         * Line customization.
         *
         * @param brush The color or fill to be applied to the line
         * @param alpha Alpha to be applied to the [brush] from 0.0f to 1.0f representing fully transparent to fully opaque respectively
         * @param thickness Thickness of the line
         * @param miter Set the stroke miter value. This is used to control the behavior of miter joins when
         *   the joins angle is sharp. This value must be >= 0
         * @param cap Return the paint's Cap, controlling how the start and end of a point on a line are treated
         * @param join Set's the treatment where points located on a line
         * @param pathEffect Optional effect or pattern to apply to the divider
         * @param colorFilter ColorFilter to apply to the [brush]
         * @param blendMode The blending algorithm to apply to the [brush]
         */
        data class Customization(
            val brush: Brush,
            @param:FloatRange(0.0, 1.0) val alpha: Float = 1f,
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
                @FloatRange(0.0, 1.0) alpha: Float = 1f,
                thickness: Dp = 5.dp,
                miter: Float = Stroke.DefaultMiter,
                cap: StrokeCap = Stroke.DefaultCap,
                join: StrokeJoin = Stroke.DefaultJoin,
                pathEffect: PathEffect? = null,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ): this(SolidColor(color), alpha, thickness, miter, cap, join, pathEffect, colorFilter, blendMode)
        }
        /**
         * @param brush The color or fill to be applied to the fill
         * @param alpha Alpha to be applied to the [brush] from 0.0f to 1.0f representing fully transparent to fully opaque respectively
         * @param colorFilter ColorFilter to apply to the [brush]
         * @param blendMode The blending algorithm to apply to the [brush]
         */
        data class FillCustomization(
            val brush: Brush,
            @param:FloatRange(0.0, 1.0) val alpha: Float = 1f,
            val colorFilter: ColorFilter? = null,
            val blendMode: BlendMode = DefaultBlendMode
        ) {
            constructor(
                color: Color,
                @FloatRange(0.0, 1.0) alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ): this(SolidColor(color), alpha, colorFilter, blendMode)
        }
    }
    @Immutable
    internal data class OffsetLine(
        val offsets: List<ChartPixel>,
        val pointsOrder: Line.PointsOrder,
        val tag: Int,
        val customization: Line.Customization,
        val fillCustomization: Line.FillCustomization?
    )
    /**
     * Representation of an x-axis in a line chart.
     *
     * @param minValue Minimum value of x-axis, if null it will be calculated from the lines on both y-axes
     * @param maxValue Maximum value of x-axis, if null it will be calculated from the lines on both y-axes
     * @param gridLines Grid lines settings
     * @param dividerCustomization Axis divider customization
     * @param value Axis labels configuration
     * @param valueView The label view
     */
     data class XAxis(
         val minValue: Double? = null,
         val maxValue: Double? = null,
         override val gridLines: GridChartData.Axis.GridLines? = null,
         override val dividerCustomization: GridChartData.Axis.DividerCustomization? = null,
         val value: GridChartData.Axis.Value,
         val valueView: @Composable ((value: Double) -> Unit)? = null,
     ): GridChartData.Axis.XAxis
    /**
     * Representation of a y-axis in a line chart.
     *
     * @param lines The lines that connected to this y-axis
     * @param offset Padding of the lines from the start(x) and end(y) of this y-axis' viewport
     * @param minValue Minimum value of this y-axis, if null it will be calculated from the lines on this axis
     * @param maxValue Maximum value of this y-axis, if null it will be calculated from the lines on this axis
     * @param gridLines Grid lines settings
     * @param dividerCustomization Axis divider customization
     * @param value Axis labels configuration
     * @param valueView The label view
     */
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
    /**
     * @param isPointInRange Determines if the click was close enough to a point on a line
     * @param viewPosition How to position the view anchored to the point
     * @param viewOffset Offset from the point
     * @param viewStayInChartBounds True will keep the view inside chart bounds, false will keep the view in its original position
     * @param view The view to show
     */
    class PointClick(
        /**
         * For example a distance of 15DP considered as in range:
         * ```
         * { point, press ->
         *     (press - point).getDistance() / this.density <= 15.0
         * }
         * ```
         */
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
            var y: Int = viewOffsetInCanvas.y.value.fastRoundToInt() + when {
                Position.Top in viewPosition -> -viewSize.height - viewOffset.y
                Position.Bottom in viewPosition -> viewOffset.y
                else -> -viewSize.height / 2 + viewOffset.y
            }
            var x: Int = viewOffsetInCanvas.x.value.fastRoundToInt() + when {
                Position.Left in viewPosition -> -viewSize.width - viewOffset.x
                Position.Right in viewPosition -> viewOffset.x
                else -> -viewSize.width / 2 + viewOffset.x
            }
            if (viewStayInChartBounds) {
                x = x.coerceIn(0, canvasSize.width - viewSize.width)
                y = y.coerceIn(0, canvasSize.height - viewSize.height)
            }
            return IntOffset(x, y)
        }
    }
    /**
     * @param isPointInRange Determines if the first press was close enough to a point on a line
     * @param pointDragged Callback on point dragged
     */
    class PointDrag(
        /**
         * For example a distance of 15DP considered as in range:
         * ```
         * { point, press ->
         *     (press - point).getDistance() / this.density <= 15.0
         * }
         * ```
         */
        val isPointInRange: Density.(point: Offset, press: Offset) -> Boolean,
        val pointDragged: (lineTag: Int, index: Int, newPosition: ChartValue) -> Unit
    )
}