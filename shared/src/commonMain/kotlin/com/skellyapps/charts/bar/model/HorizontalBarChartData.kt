package com.skellyapps.charts.bar.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset
import com.skellyapps.charts.common.model.GridChartData

data class HorizontalBarChartData(
    val bottomAxis: XAxis,
    val isLeftYAxis: Boolean,
    val yAxis: YAxis? = null
) {
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
