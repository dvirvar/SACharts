package com.skellyapps.charts.bar.model

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastRoundToInt
import com.skellyapps.charts.common.model.ChartPixel
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.DpCornerRadius
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.common.model.Position

/**
 * @param yAxis Y-axis of the bar chart
 * @param isLeftYAxis True will place y-axis on the left side, false will place on the right side
 * @param bottomAxis Bottom axis of the bar chart
 * @param xAxisOffset Padding of the categories from the start(x) and end(y) of the x-axis' viewport
 */
data class BarChartData(
    val yAxis: YAxis,
    val isLeftYAxis: Boolean,
    val bottomAxis: XAxis? = null,
    val xAxisOffset: DpOffset = DpOffset.Zero
) {
    /**
     * Representation of a category in a bar chart
     *
     * @param values Mutable list of the [values] that make a category
     * @param tag To distinguish between categories, mainly for users
     * @param customization Category customization
     */
    data class Category(
        val values: MutableList<ChartValueCoordinate>,
        val tag: Int,
        val customization: Customization
    ) {
        /**
         * Category customization.
         *
         * Corner radius is by the direction of the positive values, which makes the negative values look mirrored as it should be.
         *
         * @param brush The color or fill to be applied to the category
         * @param alpha Alpha to be applied to the [brush] from 0.0f to 1.0f representing fully transparent to fully opaque respectively
         * @param colorFilter ColorFilter to apply to the [brush]
         * @param blendMode The blending algorithm to apply to the [brush]
         * @param topLeftCornerRadius Top left corner radius, if value is negative it will be the bottom left corner radius
         * @param topRightCornerRadius Top right corner radius, if value is negative it will be the bottom right corner radius
         * @param bottomRightCornerRadius Bottom right corner radius, if value is negative it will be the top right corner radius
         * @param bottomLeftCornerRadius Bottom left corner radius, if value is negative it will be the top left corner radius
         */
        data class Customization(
            val brush: Brush,
            @param:FloatRange(0.0, 1.0) val alpha: Float = 1f,
            val colorFilter: ColorFilter? = null,
            val blendMode: BlendMode = DefaultBlendMode,
            val topLeftCornerRadius: DpCornerRadius = DpCornerRadius.Zero,
            val topRightCornerRadius: DpCornerRadius = DpCornerRadius.Zero,
            val bottomRightCornerRadius: DpCornerRadius = DpCornerRadius.Zero,
            val bottomLeftCornerRadius: DpCornerRadius = DpCornerRadius.Zero,
        ) {
            constructor(
                color: Color,
                @FloatRange(0.0, 1.0) alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
                topLeftCornerRadius: DpCornerRadius = DpCornerRadius.Zero,
                topRightCornerRadius: DpCornerRadius = DpCornerRadius.Zero,
                bottomRightCornerRadius: DpCornerRadius = DpCornerRadius.Zero,
                bottomLeftCornerRadius: DpCornerRadius = DpCornerRadius.Zero,
            ): this(SolidColor(color), alpha, colorFilter, blendMode, topLeftCornerRadius, topRightCornerRadius, bottomRightCornerRadius, bottomLeftCornerRadius)
        }
    }

    /** Category type, [Grouped] or [Stacked].*/
    sealed interface Type {
        val categoriesSpace: Dp

        /**
         * Categories placed next to each other.
         *
         * @param barsSpace Space between bars in a group
         * @param categoriesSpace Space between categories
         */
        data class Grouped(
            val barsSpace: Dp,
            override val categoriesSpace: Dp
        ): Type

        /**
         * Categories placed on each other.
         *
         * @param categoriesSpace Space between categories
         */
        data class Stacked(
            override val categoriesSpace: Dp
        ): Type
    }
    @Immutable
    internal data class OffsetCategory(
        val offsets: List<Offset>,
        val tag: Int,
        val customization: Category.Customization
    ) {
        @Immutable
        internal data class Offset(
            val topLeft: ChartPixel,
            val size: Size,
            val isNegative: Boolean
        )
    }
    /**
     * Representation of an x-axis in a bar chart.
     *
     * @param gridLines Grid lines settings
     * @param dividerCustomization Axis divider customization
     * @param valueView The label view
     */
    data class XAxis(
        override val gridLines: GridChartData.Axis.GridLines? = null,
        override val dividerCustomization: GridChartData.Axis.DividerCustomization? = null,
        val valueView: @Composable ((index: Int) -> Unit)? = null
    ): GridChartData.Axis.XAxis
    /**
     * Representation of a y-axis in a bar chart.
     *
     * @param categories The categories that connected to this y-axis
     * @param type Type of the categories
     * @param minValue Minimum value of this y-axis, if null it will be calculated from the categories on this axis
     * @param maxValue Maximum value of this y-axis, if null it will be calculated from the categories on this axis
     * @param gridLines Grid lines settings
     * @param dividerCustomization Axis divider customization
     * @param value Axis labels configuration
     * @param valueView The label view
     */
    data class YAxis(
        val categories: MutableList<Category>,
        val type: Type,
        override val minValue: Double? = null,
        override val maxValue: Double? = null,
        override val gridLines: GridChartData.Axis.GridLines? = null,
        override val dividerCustomization: GridChartData.Axis.DividerCustomization? = null,
        val value: GridChartData.Axis.Value,
        val valueView: @Composable ((Double) -> Unit)? = null
    ): GridChartData.Axis.YAxis {
        override val offset: DpOffset = DpOffset.Zero
    }

    /**
     * @param viewPosition How to position the view anchored to the bar
     * @param viewPositionMirrored On negative values the position will be mirrored
     * @param viewOffset Offset from the position
     * @param viewStayInChartBounds True will keep the view inside chart bounds, false will keep the view in its original position
     * @param view The view to show
     */
    class BarHover(
        val viewPosition: Position,
        val viewPositionMirrored: Boolean,
        val viewOffset: DpOffset,
        val viewStayInChartBounds: Boolean,
        val view: @Composable (categoryTag: Int, index: Int) -> Unit
    ) {
        internal fun getViewOffset(density: Density, canvasSize: IntSize, viewSize: IntSize, viewOffsetInCanvas: OffsetCategory.Offset): IntOffset {
            val viewOffset = with(density) {
                IntOffset(viewOffset.x.roundToPx(), viewOffset.y.roundToPx())
            }
            var y: Int = viewOffsetInCanvas.topLeft.y.value.fastRoundToInt() + when {
                Position.Top in viewPosition -> if (viewPositionMirrored && viewOffsetInCanvas.isNegative) {
                    viewOffsetInCanvas.size.height.fastRoundToInt() + viewOffset.y
                } else {
                    -viewSize.height - viewOffset.y
                }
                Position.Bottom in viewPosition -> if (viewPositionMirrored && viewOffsetInCanvas.isNegative) {
                    -viewSize.height - viewOffset.y
                } else {
                    viewOffsetInCanvas.size.height.fastRoundToInt() + viewOffset.y
                }
                else -> viewOffsetInCanvas.size.height.fastRoundToInt() / 2 - viewSize.height / 2 + viewOffset.y
            }
            var x: Int = viewOffsetInCanvas.topLeft.x.value.fastRoundToInt() + when {
                Position.Left in viewPosition -> -viewSize.width - viewOffset.x
                Position.Right in viewPosition -> viewOffsetInCanvas.size.width.fastRoundToInt() + viewOffset.x
                else -> viewOffsetInCanvas.size.width.fastRoundToInt() / 2 - viewSize.width / 2 + viewOffset.x
            }
            if (viewStayInChartBounds) {
                x = x.coerceIn(0, canvasSize.width - viewSize.width)
                y = y.coerceIn(0, canvasSize.height - viewSize.height)
            }
            return IntOffset(x, y)
        }

        internal fun getViewOffsetHorizontal(density: Density, canvasSize: IntSize, viewSize: IntSize, viewOffsetInCanvas: OffsetCategory.Offset): IntOffset {
            val viewOffset = with(density) {
                IntOffset(viewOffset.x.roundToPx(), viewOffset.y.roundToPx())
            }
            var y: Int = viewOffsetInCanvas.topLeft.y.value.fastRoundToInt() + when {
                Position.Top in viewPosition -> -viewSize.height - viewOffset.y
                Position.Bottom in viewPosition -> viewOffsetInCanvas.size.height.fastRoundToInt() + viewOffset.y
                else -> viewOffsetInCanvas.size.height.fastRoundToInt() / 2 - viewSize.height / 2 + viewOffset.y
            }
            var x: Int = viewOffsetInCanvas.topLeft.x.value.fastRoundToInt() + when {
                Position.Left in viewPosition -> if (viewPositionMirrored && viewOffsetInCanvas.isNegative) {
                    viewOffsetInCanvas.size.width.fastRoundToInt() + viewOffset.x
                } else {
                    -viewSize.width - viewOffset.x
                }
                Position.Right in viewPosition -> if (viewPositionMirrored && viewOffsetInCanvas.isNegative) {
                    -viewSize.width - viewOffset.x
                } else {
                    viewOffsetInCanvas.size.width.fastRoundToInt() + viewOffset.x
                }
                else -> viewOffsetInCanvas.size.width.fastRoundToInt() / 2 - viewSize.width / 2 + viewOffset.x
            }
            if (viewStayInChartBounds) {
                x = x.coerceIn(0, canvasSize.width - viewSize.width)
                y = y.coerceIn(0, canvasSize.height - viewSize.height)
            }
            return IntOffset(x, y)
        }
    }
}