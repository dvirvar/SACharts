package com.skellyapps.charts.line.animation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skellyapps.charts.common.animation.ChartAnimation
import com.skellyapps.charts.common.model.ChartPixel

/**
 * @param growth To enable [Growth] animation
 * @param reveal To enable [Reveal] animation
 */
data class LineChartAnimations(
    val growth: Growth? = null,
    val reveal: Reveal? = null
) {
    companion object {
        val none = LineChartAnimations(null, null)
    }
    /**
     * Animates the lines by their y-axis from the height of the x-axis.
     */
    class Growth(
        spec: AnimationSpec<Float>,
        initialValue: Float = 0f
    ): ChartAnimation(spec, initialValue) {
        internal inline fun getYPixel(size: Size, chartPixel: ChartPixel) = size.height + (chartPixel.y.value - size.height) * animatable.value
    }

    /**
     * Reveals the lines from start to end.
     *
     * Because Reveal works with clipRect,
     * if you draw on points you should give some padding,
     * so the drawings will not get clipped.
     *
     * @param rectHorizontalPadding The horizontal padding to not get clipped
     * @param rectVerticalPadding The vertical padding to not get clipped
     */
    class Reveal(
        spec: AnimationSpec<Float>,
        initialValue: Float = 0f,
        val rectHorizontalPadding: Dp = 0.dp,
        val rectVerticalPadding: Dp = 0.dp
    ): ChartAnimation(spec, initialValue)
}