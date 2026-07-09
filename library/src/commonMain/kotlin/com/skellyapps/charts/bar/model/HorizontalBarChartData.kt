package com.skellyapps.charts.bar.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset
import com.skellyapps.charts.common.model.GridChartData

/**
 * @param bottomAxis Bottom axis of the horizontal bar chart
 * @param isLeftYAxis True will place y-axis on the left side, false will place on the right side
 * @param yAxis Y-axis of the horizontal bar chart
 */
data class HorizontalBarChartData(
    val bottomAxis: XAxis,
    val isLeftYAxis: Boolean,
    val yAxis: YAxis? = null
) {
    /**
     * Representation of a x-axis in a horizontal bar chart.
     *
     * @param categories The categories that connected to this x-axis
     * @param type Type of the categories
     * @param minValue Minimum value of this x-axis, if null it will be calculated from the categories on this axis
     * @param maxValue Maximum value of this x-axis, if null it will be calculated from the categories on this axis
     * @param gridLines Grid lines settings
     * @param dividerCustomization Axis divider customization
     * @param value Axis labels configuration
     * @param valueView The label view
     */
    data class XAxis(
        val categories: MutableList<BarChartData.Category>,
        val type: BarChartData.Type,
        val minValue: Double? = null,
        val maxValue: Double? = null,
        override val gridLines: GridChartData.Axis.GridLines? = null,
        override val dividerCustomization: GridChartData.Axis.DividerCustomization? = null,
        val value: GridChartData.Axis.Value,
        val valueView: @Composable ((Double) -> Unit)? = null
    ): GridChartData.Axis.XAxis
    /**
     * Representation of an y-axis in a horizontal bar chart.
     *
     * @param offset Padding of the categories from the start(x) and end(y) of the axis' viewport
     * @param gridLines Grid lines settings
     * @param dividerCustomization Axis divider customization
     * @param valueView The label view
     */
    data class YAxis(
        override val offset: DpOffset = DpOffset.Zero,
        override val gridLines: GridChartData.Axis.GridLines? = null,
        override val dividerCustomization: GridChartData.Axis.DividerCustomization? = null,
        val valueView: @Composable ((index: Int) -> Unit)? = null
    ): GridChartData.Axis.YAxis {
        override val minValue: Double? = null
        override val maxValue: Double? = null
    }
}
