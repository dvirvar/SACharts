package com.skellyapps.charts.common.extension

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset

context(d: Density)
internal fun DpOffset.toOffset(): Offset = with(d) { Offset(x.toPx(), y.toPx()) }