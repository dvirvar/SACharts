package com.skellyapps.charts.bar.model

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import com.skellyapps.charts.common.model.ChartPixel
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.GridChartData

data class BarChartData(
    val yAxis: YAxis,
    val isLeftYAxis: Boolean,
    val bottomAxis: XAxis? = null,
    val xAxisOffset: DpOffset = DpOffset.Zero
) {
    data class Category(
        val values: MutableList<ChartValueCoordinate>,
        val tag: Int,
        val customization: Customization
    ) {
        data class Customization(
            val brush: Brush,
            @param:FloatRange val alpha: Float = 1f,
            val colorFilter: ColorFilter? = null,
            val blendMode: BlendMode = DefaultBlendMode,
            val topLeftCornerRadius: CornerRadius = CornerRadius.Zero,
            val topRightCornerRadius: CornerRadius = CornerRadius.Zero,
            val bottomRightCornerRadius: CornerRadius = CornerRadius.Zero,
            val bottomLeftCornerRadius: CornerRadius = CornerRadius.Zero,
        ) {
            constructor(
                color: Color,
                @FloatRange alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
                topLeftCornerRadius: CornerRadius = CornerRadius.Zero,
                topRightCornerRadius: CornerRadius = CornerRadius.Zero,
                bottomRightCornerRadius: CornerRadius = CornerRadius.Zero,
                bottomLeftCornerRadius: CornerRadius = CornerRadius.Zero,
            ): this(SolidColor(color), alpha, colorFilter, blendMode, topLeftCornerRadius, topRightCornerRadius, bottomRightCornerRadius, bottomLeftCornerRadius)
        }
    }

    sealed interface Type {
        val categoriesSpace: Dp
        data class Grouped(
            val barsSpace: Dp,
            override val categoriesSpace: Dp
        ): Type
        data class Stacked(
            override val categoriesSpace: Dp
        ): Type
    }

    internal data class OffsetCategory(
        val offsets: List<Offset>,
        val tag: Int,
        val customization: Category.Customization
    ) {
        internal data class Offset(
            val topLeft: ChartPixel,
            val size: Size,
            val isNegative: Boolean
        )
    }

    data class XAxis(
        override val gridLines: GridChartData.Axis.GridLines? = null,
        override val dividerCustomization: GridChartData.Axis.DividerCustomization? = null,
        val valueView: @Composable ((index: Int) -> Unit)? = null
    ): GridChartData.Axis.XAxis

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
}