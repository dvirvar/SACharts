package com.skellyapps.charts.pie.model

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Dp

/**
 * innerRadiusPercentage is relative to outerRadiusPercentage
 */
data class PieChartData(
    val slices: MutableList<Slice>,
    @param:FloatRange(0.0, 360.0, toInclusive = false) val startAngle: Float = 270f,
    @param:FloatRange(0.0, 1.0, fromInclusive = false) val outerRadiusPercentage: Float = 1f,
    @param:FloatRange(0.0, 1.0, toInclusive = false) val innerRadiusPercentage: Float = 0f,
    val sliceSpacingDegrees: Float = 0f,
    val sliceBorder: Slice.Border? = null,
    val labelCustomization: LabelCustomization? = null
) {
    data class Slice(val value: Double, val color: Color, val tag: Int, val label: String? = null) {
        data class Border(
            val thickness: Dp,
            val color: Color
        )
    }
    data class LabelCustomization(
        val textMeasurer: TextMeasurer,
        val textColor: Color,
        val lineCustomization: LineCustomization
    )
    data class LineCustomization(
        val edgePadding: Dp,
        val lineToTextPadding: Dp,
        val shoulderLength: Dp,
        val extensionMaxLength: Dp,
        val color: Color,
        val thickness: Dp,
        val join: StrokeJoin = Stroke.DefaultJoin,
        val cap: StrokeCap = Stroke.DefaultCap,
        val miter: Float = Stroke.DefaultMiter,
        val pathEffect: PathEffect? = null
    )
}
