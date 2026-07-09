package com.skellyapps.charts.line.model

internal data class DraggedPoint(
    val index: Int,
    val lineTag: Int,
    val isLeftAxis: Boolean
)