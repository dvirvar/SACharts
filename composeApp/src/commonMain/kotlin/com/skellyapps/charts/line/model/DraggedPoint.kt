package com.skellyapps.charts.line.model

internal data class DraggedPoint(
    val index: Int,
    val lineTag: Byte,
    val isLeftAxis: Boolean
)