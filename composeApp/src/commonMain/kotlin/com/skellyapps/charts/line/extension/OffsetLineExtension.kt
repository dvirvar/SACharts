package com.skellyapps.charts.line.extension

import androidx.compose.ui.geometry.Offset
import com.skellyapps.charts.line.model.DraggedPoint
import com.skellyapps.charts.line.model.DraggedPointDistance
import com.skellyapps.charts.line.model.LineChartData.OffsetLine

internal fun OffsetLine.getClosestPointIfInRange(touchPoint: Offset, isInRange: Offset.(Offset) -> Boolean, isLeftAxis: Boolean): DraggedPointDistance? {
    if (offsets.isEmpty()) {
        return null
    }
    val closestIndexDistance = pointsOrder.getClosestIndexDistance(offsets, touchPoint, isInRange)
    if (closestIndexDistance != null) {
        return DraggedPointDistance(DraggedPoint(closestIndexDistance.first, tag), closestIndexDistance.second, isLeftAxis)
    }
    return null
}