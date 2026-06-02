package com.skellyapps.charts.pie.view

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastRoundToInt
import com.skellyapps.charts.pie.model.PieChartData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PieChart(
    modifier: Modifier,
    data: PieChartData,
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier) {
        val totalValue = data.slices.sumOf { it.value }
        val centerX = center.x
        val centerY = center.y
        val outerRadius = minOf(centerX, centerY)
        val innerRadius = outerRadius * data.innerRadiusPercentage // Calculate inner radius

        if (data.slices.size == 1) {
            val donutRingPath = Path().apply {
                // Outer circle (clockwise)
                addArc(
                    Rect(
                        Offset(centerX - outerRadius, centerY - outerRadius),
                        Size(outerRadius * 2, outerRadius * 2)
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 360f
                )

                // Inner circle (counter-clockwise to create the hole effect when filled)
                addArc(
                    Rect(
                        Offset(centerX - innerRadius, centerY - innerRadius),
                        Size(innerRadius * 2, innerRadius * 2)
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = -360f // Negative sweep for counter-clockwise
                )
            }
            drawPath(donutRingPath, color = data.slices.first().color)
        } else {
            var startAngle = data.startAngle + data.sliceSpacingDegrees / 2f
            // Draw connecting lines and top slices
            data.slices.forEach { slice ->
                val sweepAngle = ((slice.value / totalValue) * 360.0).toFloat() - data.sliceSpacingDegrees

                val startRad = startAngle * (PI / 180.0)
                val endRad = (startAngle + sweepAngle) * (PI / 180.0)

                val innerStartPointX = (centerX + innerRadius * cos(startRad)).toFloat()
                val innerStartPointY = (centerY + innerRadius * sin(startRad)).toFloat()

                val innerEndPointX = (centerX + innerRadius * cos(endRad)).toFloat()
                val innerEndPointY = (centerY + innerRadius * sin(endRad)).toFloat()

                // Create a Path for each slice
                val path = Path().apply {
                    // Move to the start point of the inner arc
                    moveTo(innerStartPointX, innerStartPointY)
                    // Draw the outer arc
                    arcTo(
                        rect = Rect(center, outerRadius),
                        startAngleDegrees = startAngle,
                        sweepAngleDegrees = sweepAngle,
                        forceMoveTo = false
                    )
                    // Draw a line from the end of the outer arc to the end of the inner arc
                    lineTo(innerEndPointX, innerEndPointY)

                    // Draw the inner arc (clockwise or counter-clockwise depending on your preference
                    // relative to the outer arc, to close the path)
                    // We're drawing it backward (from end to start) to ensure it closes correctly.
                    arcTo(
                        rect = Rect(center, innerRadius),
                        startAngleDegrees = startAngle + sweepAngle, // Start from the end of the sweep
                        sweepAngleDegrees = -sweepAngle, // Sweep backwards to the start
                        forceMoveTo = false
                    )

                    // Close the path
                    close()
                }

                // Draw the filled path
                drawPath(
                    path = path,
                    color = slice.color
                )
                //TODO: Border inside path
                data.sliceBorder?.let {
                    drawPath(
                        path,
                        it.color,
                        1f,
                        Stroke(
                            it.thickness.toPx()
                        )
                    )
                }
                val middleDeg = (startAngle + sweepAngle / 2f) % 360f
                val middleRad = middleDeg * (PI / 180.0)
                val middleRadius = (outerRadius + innerRadius) / 2f
                val middlePointX = (centerX + middleRadius * cos(middleRad)).toFloat()
                val middlePointY = (centerY + middleRadius * sin(middleRad)).toFloat()
                val layout = textMeasurer.measure(
                    middleDeg.fastRoundToInt().toString()
                )
                val x = middlePointX - layout.size.width / 2f
                val y = middlePointY - layout.size.height / 2f
                drawText(layout, Color.White, Offset(x, y))
                val outerMiddlePointX = (centerX + outerRadius * cos(middleRad)).toFloat()
                val outerMiddlePointY = (centerY + outerRadius * sin(middleRad)).toFloat()
                drawCircle(Color.Black, 5f, Offset(outerMiddlePointX, outerMiddlePointY))
                val innerMiddlePointX = (centerX + innerRadius * cos(middleRad)).toFloat()
                val innerMiddlePointY = (centerY + innerRadius * sin(middleRad)).toFloat()
                drawCircle(Color.Black, 5f, Offset(innerMiddlePointX, innerMiddlePointY))

                startAngle = (startAngle + sweepAngle + data.sliceSpacingDegrees) % 360f
            }
        }
    }
}