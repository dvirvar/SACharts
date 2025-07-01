package com.skellyapps.charts.pie.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

data class PieChartData(
    val slices: MutableList<Slice>,
    val startAngle: Float = 270f,
    val customization: Customization? = null
) {
    data class Slice(val value: Double, val color: Color)

    data class Customization(
        val divider: Line,
        val outerBorder: Line?
    ) {
        data class Line(
            val thickness: Dp,
            val color: Brush,
        )
    }
}
