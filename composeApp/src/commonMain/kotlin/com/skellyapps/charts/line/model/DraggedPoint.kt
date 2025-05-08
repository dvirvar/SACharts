package com.skellyapps.charts.line.model

data class DraggedPoint(
    val index: Int,
    val lineTag: Byte
)

data class DraggedPointDistance(
    val draggedPoint: DraggedPoint,
    val distance: Float,
    val isLeftAxis: Boolean
)