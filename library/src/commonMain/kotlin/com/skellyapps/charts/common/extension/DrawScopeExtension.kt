package com.skellyapps.charts.common.extension

import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect

internal inline fun DrawScope.clipRect(
    enabled: Boolean,
    left: Float = 0.0f,
    top: Float = 0.0f,
    right: Float = size.width,
    bottom: Float = size.height,
    clipOp: ClipOp = ClipOp.Intersect,
    block: DrawScope.() -> Unit,
) {
    if (enabled) {
        clipRect(left, top, right, bottom, clipOp, block)
    } else {
        block()
    }
}