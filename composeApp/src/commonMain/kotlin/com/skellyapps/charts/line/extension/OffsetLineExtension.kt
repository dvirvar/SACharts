package com.skellyapps.charts.line.extension

import androidx.compose.ui.geometry.Offset
import com.skellyapps.charts.line.model.LineChartData.OffsetLine

internal fun OffsetLine.getClosestIndexDistance(touchPoint: Offset, isInRange: Offset.(Offset) -> Boolean): Pair<Int, Float>? {
    if (offsets.isEmpty()) {
        return null
    }
    return pointsOrder.getClosestIndexDistance(offsets, touchPoint, isInRange)
}