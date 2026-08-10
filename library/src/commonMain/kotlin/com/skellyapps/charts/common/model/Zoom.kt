package com.skellyapps.charts.common.model

import androidx.compose.foundation.gestures.Orientation

/**
 * Zoom settings
 *
 * @param scrollJump Zoom in/out jump by the mouse wheel
 * @param max Maximum zoom
 * @param orientation In which direction the zoom is enabled, null means all directions
 */
data class Zoom(
    val scrollJump: Float,
    val max: Float,
    val orientation: Orientation? = null
)