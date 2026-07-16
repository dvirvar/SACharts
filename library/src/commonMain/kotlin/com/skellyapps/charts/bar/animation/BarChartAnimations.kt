package com.skellyapps.charts.bar.animation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.ui.geometry.Rect
import com.skellyapps.charts.bar.model.BarChartData
import com.skellyapps.charts.common.animation.ChartAnimation

/**
 * @param growth To enable [Growth] animation
 */
data class BarChartAnimations(
    val growth: Growth? = null
) {
    companion object {
        val none = BarChartAnimations(null)
    }

    /**
     * Animates bars' height.
     *
     * (Works only with BarChartData.Type.Grouped for now)
     */
    class Growth(
        spec: AnimationSpec<Float>,
        initialValue: Float = 0f
    ): ChartAnimation(spec, initialValue) {
        internal inline fun getRect(offset: BarChartData.OffsetCategory.Offset): Rect {
            val originalHeight = offset.size.height
            val animatedHeight = originalHeight * animatable.value

            val animatedTopLeftY = if (offset.isNegative) {
                offset.topLeft.offset.y
            } else {
                val baselineY = offset.topLeft.offset.y + originalHeight
                baselineY - animatedHeight
            }

            val animatedTopLeft = offset.topLeft.offset.copy(y = animatedTopLeftY)
            val animatedSize = offset.size.copy(height = animatedHeight)
            return Rect(animatedTopLeft, animatedSize)
        }
    }
}