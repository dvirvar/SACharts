package com.skellyapps.charts.pie.animation

import androidx.compose.animation.core.AnimationSpec
import com.skellyapps.charts.common.animation.ChartAnimation

/**
 * @param scale To enable [Scale] animation
 * @param growth To enable [Growth] animation
 */
data class PieChartAnimations(
    val scale: Scale? = null,
    val growth: Growth? = null
) {
    companion object {
        val None = PieChartAnimations(null, null)
    }

    /**
     * Animated pie's outer radius
     */
    class Scale(
        spec: AnimationSpec<Float>,
        initialValue: Float = 0f
    ): ChartAnimation(spec, initialValue)

    /**
     * Animated pie slices' size
     */
    class Growth(
        spec: AnimationSpec<Float>,
        initialValue: Float = 0f
    ): ChartAnimation(spec, initialValue)
}