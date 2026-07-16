package com.skellyapps.charts.common.animation

import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec


/**
 * @param spec How to animate
 * @param initialValue Initial value of the animation
 */
abstract class ChartAnimation(
    val spec: AnimationSpec<Float>,
    initialValue: Float
) {
    internal val animatable = Animatable(initialValue)
    val value get() = animatable.value
    val isRunning get() = animatable.isRunning

    suspend fun animate() = animatable.animateTo(
        1f,
        spec
    )
    suspend fun snapTo(@FloatRange(0.0, 1.0) value: Float) = animatable.snapTo(value)
    suspend fun stop() = animatable.stop()
}