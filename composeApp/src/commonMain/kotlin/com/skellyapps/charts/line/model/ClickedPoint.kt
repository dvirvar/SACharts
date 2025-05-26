package com.skellyapps.charts.line.model

internal data class ClickedPoint(
    val index: Int,
    val lineTag: Byte,
    val isLeftAxis: Boolean
)
