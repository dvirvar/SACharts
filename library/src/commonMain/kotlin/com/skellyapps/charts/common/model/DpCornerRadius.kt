package com.skellyapps.charts.common.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class DpCornerRadius(
    val x: Dp,
    val y: Dp = x
) {
    companion object {
        val Zero = DpCornerRadius(0.dp)
    }

    context(d: Density)
    internal fun toCornerRadius() = with(d) {
        CornerRadius(x.toPx(), y.toPx())
    }
}
