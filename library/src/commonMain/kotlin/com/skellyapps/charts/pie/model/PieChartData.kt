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
 * @param slices The slices that build the pie chart
 * @param startAngle The start angle of the pie chart
 * @param outerRadiusPercentage Size of the pie chart relative to canvas size
 * @param innerRadiusPercentage Relative to outerRadiusPercentage
 * @param sliceSpacingDegrees Space between slices in degrees
 * @param sliceBorder Inside border of a slice
 * @param labelCustomization Label customization of a slice
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
    /**
     * Representation of a slice in a pie chart.
     *
     * @param value How much the slice amount to
     * @param color Color of the slice
     * @param tag To distinguish between lines, mainly for users
     * @param label Label of the slice
     */
    data class Slice(val value: Double, val color: Color, val tag: Int, val label: String? = null) {
        /**
         * Representation of a slice border.
         *
         * @param thickness Thickness of the border
         * @param color Color of the border
         */
        data class Border(
            val thickness: Dp,
            val color: Color
        )
    }

    /**
     * @param textMeasurer Text measurer to draw the label
     * @param textColor Text color of the label
     * @param edgePadding Padding of the label to the edge of the pie chart canvas
     * @param linePadding Padding of the label to the line
     * @param lineCustomization [LineCustomization]
     */
    data class LabelCustomization(
        val textMeasurer: TextMeasurer,
        val textColor: Color,
        val edgePadding: Dp,
        val linePadding: Dp,
        val lineCustomization: LineCustomization
    )

    /**
     * Label line(shoulder + extension) customization.
     *
     * @param shoulderLength Shoulder length
     * @param extensionMaxLength Extension max length, will shrink if there is not enough space
     * @param color Color of the line
     * @param thickness Thickness of the line
     * @param join Set's the treatment where shoulder and extension are joined
     * @param cap Treatment applied to the ends of the line
     * @param miter Set the stroke miter value. This is used to control the behavior of miter joins when
     *   the joins angle is sharp. This value must be >= 0
     * @param pathEffect Optional effect or pattern to apply to the line
     */
    data class LineCustomization(
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
