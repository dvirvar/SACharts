package com.skellyapps.charts.pie.extension

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import com.skellyapps.charts.pie.model.PieChartData

internal inline fun DrawScope.drawBorderInside(path: Path, border: PieChartData.Slice.Border) {
    clipPath(path) {
        drawPath(
            path,
            border.color,
            1f,
            Stroke(
                border.thickness.toPx() * 2f
            )
        )
    }
}