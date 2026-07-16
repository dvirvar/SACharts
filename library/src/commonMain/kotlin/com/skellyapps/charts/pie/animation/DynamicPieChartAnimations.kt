package com.skellyapps.charts.pie.animation

import androidx.compose.animation.core.AnimationSpec
import com.skellyapps.charts.common.animation.ChartAnimation

/**
 * @param growth To enable [Growth] animation
 */
data class DynamicPieChartAnimations(
    val growth: Growth? = null
) {
    companion object {
        val none = DynamicPieChartAnimations(null)
    }

    /**
     * Animated pie slices' size
     */
    class Growth(
        spec: AnimationSpec<Float>,
        initialValue: Float = 0f
    ): ChartAnimation(spec, initialValue)
}