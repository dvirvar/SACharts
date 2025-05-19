package com.skellyapps.charts.line.model

/**
 * Zoom settings
 *
 * @param scrollJump Zoom in/out jump by the mouse wheel
 * @param max Maximum zoom
 */
data class Zoom(
    val scrollJump: Float,
    val max: Float
)