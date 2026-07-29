package com.skellyapps.charts.bar.animation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.ui.geometry.Rect
import com.skellyapps.charts.bar.model.BarChartData
import com.skellyapps.charts.common.animation.ChartAnimation

/**
 * @param growth To enable [Growth] animation
 */
data class HorizontalBarChartAnimations(
    val growth: Growth? = null
) {
    companion object {
        val None = HorizontalBarChartAnimations(null)
    }

    /**
     * Animates bars' width.
     */
    class Growth(
        spec: AnimationSpec<Float>,
        initialValue: Float = 0f
    ): ChartAnimation(spec, initialValue) {
        internal inline fun getRect(offset: BarChartData.OffsetCategory.Offset): Rect {
            val originalWidth = offset.size.width
            val animatedWidth = originalWidth * animatable.value

            val animatedTopLeftX = if (offset.isNegative) {
                val baselineX = offset.topLeft.offset.x + originalWidth
                baselineX - animatedWidth
            } else {
                offset.topLeft.offset.x
            }

            val animatedTopLeft = offset.topLeft.offset.copy(x = animatedTopLeftX)
            val animatedSize = offset.size.copy(width = animatedWidth)

            return Rect(animatedTopLeft, animatedSize)
        }
    }
}