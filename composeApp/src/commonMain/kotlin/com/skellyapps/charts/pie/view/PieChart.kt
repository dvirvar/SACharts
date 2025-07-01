package com.skellyapps.charts.pie.view

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEach
import com.skellyapps.charts.pie.model.PieChartData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    data: PieChartData,
) {
    Canvas(modifier) {
        val totalValue = data.slices.sumOf { it.value }
        var startAngle = data.startAngle

        val centerX = size.width / 2
        val centerY = size.height / 2
        val outerRadius = minOf(centerX, centerY)
        val innerRadius = outerRadius * .5f // Calculate inner radius

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
            // Draw connecting lines and top slices
            data.slices.forEach { slice ->
                val sweepAngle = ((slice.value / totalValue) * 360.0).toFloat()

                // Create a Path for each donut segment
                val path = Path().apply {
                    // Move to the start point of the inner arc
                    val startRad = startAngle * (PI / 180.0)
                    val endRad = (startAngle + sweepAngle) * (PI / 180.0)

                    val innerStartPointX = (centerX + innerRadius * cos(startRad)).toFloat()
                    val innerStartPointY = (centerY + innerRadius * sin(startRad)).toFloat()
                    moveTo(innerStartPointX, innerStartPointY)

                    // Draw the outer arc
                    arcTo(
                        rect = Rect(Offset(centerX - outerRadius, centerY - outerRadius), Size(outerRadius * 2, outerRadius * 2)),
                        startAngleDegrees = startAngle,
                        sweepAngleDegrees = sweepAngle,
                        forceMoveTo = false
                    )
                    // Draw a line from the end of the outer arc to the end of the inner arc
                    val innerEndPointX = (centerX + innerRadius * cos(endRad)).toFloat()
                    val innerEndPointY = (centerY + innerRadius * sin(endRad)).toFloat()
                    lineTo(innerEndPointX, innerEndPointY)

                    // Draw the inner arc (clockwise or counter-clockwise depending on your preference
                    // relative to the outer arc, to close the path)
                    // We're drawing it backward (from end to start) to ensure it closes correctly.
                    arcTo(
                        rect = Rect(Offset(centerX - innerRadius, centerY - innerRadius), Size(innerRadius * 2, innerRadius * 2)),
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

                startAngle += sweepAngle
            }

            // Draw dividers
            data.customization?.let { customization ->
                val dividerThickness = customization.divider.thickness.toPx()
                var dividerDrawAngle = data.startAngle
                data.slices.fastForEach { slice ->
                    // Only draw a divider BEFORE each slice (except the very first one, or the "start" of the chart)
                    // Or, draw for all slices, but be careful with the last one to avoid double drawing at 360/0
                    // A simple way is to draw a divider at the 'startAngle' of each slice,
                    // effectively drawing lines between them.

                    // Calculate the angle for the divider line
                    val dividerRad = dividerDrawAngle * (PI / 180.0)

                    // Calculate start and end points for the divider line
                    val dividerStartX = (centerX + innerRadius * cos(dividerRad)).toFloat()
                    val dividerStartY = (centerY + innerRadius * sin(dividerRad)).toFloat()

                    val dividerEndX = (centerX + outerRadius * cos(dividerRad)).toFloat()
                    val dividerEndY = (centerY + outerRadius * sin(dividerRad)).toFloat()

                    drawLine(
                        brush = customization.divider.color,
                        start = Offset(dividerStartX, dividerStartY),
                        end = Offset(dividerEndX, dividerEndY),
                        strokeWidth = dividerThickness,
                        cap = StrokeCap.Butt // Or Round, Square
                    )

                    dividerDrawAngle += ((slice.value / totalValue) * 360.0).toFloat()
                }

                customization.outerBorder?.let {
                    val thickness = it.thickness.toPx()
                    drawCircle(
                        it.color,
                        outerRadius - thickness / 2f,
                        style = Stroke(
                            thickness
                        )
                    )
                }
            }
        }
    }
}

//val radius = minOf(centerX, centerY)
//
//// Draw connecting lines and top slices
//data.slices.forEach { slice ->
//    val sweepAngle = ((slice.value / totalValue) * 360.0).toFloat()
//
//    val topRect = Offset(centerX - radius, centerY - radius)
//    val topSize = Size(radius * 2, radius * 2)
//    // Calculate start and end points for "side" lines
//    val startRad = startAngle * (PI / 180.0)
//    val endRad = (startAngle + sweepAngle)* (PI / 180.0)
//
//    val startPointX = (centerX + radius * cos(startRad)).toFloat()
//    val startPointY = (centerY + radius * sin(startRad)).toFloat()
//
//    val endPointX = (centerX + radius * cos(endRad)).toFloat()
//    val endPointY = (centerY + radius * sin(endRad)).toFloat()
//
//    // Draw connecting lines from top to bottom (for visual depth)
//    // This part is very simplified; a true 3D would involve more complex geometry
//    drawLine(
//        color = slice.color,
//        start = Offset(startPointX, startPointY),
//        end = Offset(startPointX, startPointY),
//        strokeWidth = 2f
//    )
//    drawLine(
//        color = slice.color,
//        start = Offset(endPointX, endPointY),
//        end = Offset(endPointX, endPointY),
//        strokeWidth = 2f
//    )
//
//    // Draw the main (top) pie slices
//    drawArc(
//        color = slice.color,
//        startAngle = startAngle,
//        sweepAngle = sweepAngle,
//        useCenter = true,
//        topLeft = topRect,
//        size = topSize
//    )
//
//    startAngle += sweepAngle
//}