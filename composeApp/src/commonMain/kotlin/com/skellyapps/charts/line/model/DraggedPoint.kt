package com.skellyapps.charts.line.model

data class DraggedPoint(
    val index: Int,
    val lineTag: Byte,
    val isLeftAxis: Boolean
)