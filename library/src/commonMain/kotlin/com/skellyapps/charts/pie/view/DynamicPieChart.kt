package com.skellyapps.charts.pie.view

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import com.skellyapps.charts.pie.extension.drawBorderInside
import com.skellyapps.charts.pie.graphics.PieChartDrawScope
import com.skellyapps.charts.pie.graphics.PieChartDrawScopeImpl
import com.skellyapps.charts.pie.model.DynamicPieChartData
import com.skellyapps.charts.pie.model.PieChartData
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dynamic pie chart.
 *
 * A pie chart that resizes by the available space(canvas size - labels and lines length)
 *
 * @param modifier Mandatory modifier to specify size
 * @param data [DynamicPieChartData]
 * @param drawOnEachSlice To draw on each slice
 */
@Composable
fun DynamicPieChart(
    modifier: Modifier,
    data: DynamicPieChartData,
    drawOnEachSlice: (PieChartDrawScope.(sliceTag: Int, centerX: Float, centerY: Float, outerRadius: Float, innerRadius: Float, middleRad: Double) -> Unit)? = null
) {
    Canvas(modifier) {
        val totalValue = data.slices.sumOf { it.value }
        val centerX = center.x
        val centerY = center.y

        var outerRadius = minOf(centerX, centerY)

        val hasLabels = data.labelCustomization != null && data.slices.any { it.label != null }

        if (hasLabels) {
            val lc = data.labelCustomization
            val edgePadding = lc.edgePadding.toPx()
            val linePadding = lc.linePadding.toPx()
            val extensionMax = lc.lineCustomization.extensionMaxLength.toPx()
            val shoulder = lc.lineCustomization.shoulderLength.toPx()

            var currentStartAngle = data.startAngle + (if (data.slices.size > 1) data.sliceSpacingDegrees / 2f else 0f)

            data.slices.forEach { slice ->
                val sweepAngle = if (data.slices.size == 1) 360f else {
                    ((slice.value / totalValue) * 360.0).toFloat() - data.sliceSpacingDegrees
                }

                if (slice.label != null) {
                    val middleDeg = (currentStartAngle + sweepAngle / 2f) % 360f
                    val middleRad = middleDeg * (PI / 180.0)

                    val cosVal = cos(middleRad).toFloat()
                    val sinVal = sin(middleRad).toFloat()
                    val isRightSide = cosVal > 0

                    val textLayoutResult = lc.textMeasurer.measure(slice.label)
                    val textWidth = textLayoutResult.size.width
                    val textHeight = textLayoutResult.size.height

                    val isBottomSide = sinVal > 0
                    val availableHeight = if (isBottomSide) size.height - centerY - edgePadding else centerY - edgePadding

                    //Vertical components:
                    //1. The radius itself projects vertically by: radius * sinVal
                    //2. The extension line projects vertically by: extensionMax * sinVal
                    //3. Half the text height sits above/below the line anchor
                    //Formula: (radius + extensionMax) * abs(sinVal) + (textHeight / 2) <= availableHeight
                    val verticalOverhead = (extensionMax * abs(sinVal)) + (textHeight / 2f)
                    val allowedRadiusByY = if (abs(sinVal) > 0.001f) {
                        (availableHeight - verticalOverhead) / abs(sinVal)
                    } else {
                        Float.MAX_VALUE
                    }

                    val availableWidth = if (isRightSide) size.width - centerX - edgePadding else centerX - edgePadding

                    //Horizontal components:
                    //1. The radius itself projects horizontally by: radius * cosVal
                    //2. The extension line projects horizontally by: extensionMax * cosVal
                    //3. The shoulder, text padding, and text width are strictly horizontal (independent of angle!)
                    //Formula: (radius + extensionMax) * abs(cosVal) + shoulder + textPadding + textWidth <= availableWidth
                    val horizontalOverhead = shoulder + linePadding + textWidth + (extensionMax * abs(cosVal))
                    val allowedRadiusByX = if (abs(cosVal) > 0.001f) {
                        (availableWidth - horizontalOverhead) / abs(cosVal)
                    } else {
                        Float.MAX_VALUE
                    }

                    //The radius must respect both bottlenecks for this specific label
                    val labelMaxRadius = minOf(allowedRadiusByX, allowedRadiusByY)

                    outerRadius = minOf(outerRadius, labelMaxRadius)
                }

                currentStartAngle = (currentStartAngle + sweepAngle + data.sliceSpacingDegrees) % 360f
            }
            outerRadius = maxOf(outerRadius, minOf(centerX, centerY) * data.outerRadiusMinPercentage)
        }

        val innerRadius = outerRadius * data.innerRadiusPercentage

        fun drawLabel(label: String, labelCustomization: PieChartData.LabelCustomization, middleRad: Double) {
            val linePadding = labelCustomization.linePadding.toPx()
            val extensionLineMaxLength = labelCustomization.lineCustomization.extensionMaxLength.toPx()
            val shoulderLineLength = labelCustomization.lineCustomization.shoulderLength.toPx()
            val lineThickness = labelCustomization.lineCustomization.thickness.toPx()
            val lineColor = labelCustomization.lineCustomization.color

            val lineStartX = (centerX + outerRadius * cos(middleRad)).toFloat()
            val lineStartY = (centerY + outerRadius * sin(middleRad)).toFloat()

            val textLayoutResult = labelCustomization.textMeasurer.measure(label)
            val textWidth = textLayoutResult.size.width
            val textHeight = textLayoutResult.size.height

            val isRightSide = cos(middleRad) > 0

            val lineEndX = (centerX + (outerRadius + extensionLineMaxLength) * cos(middleRad)).toFloat()
            val lineEndY = (centerY + (outerRadius + extensionLineMaxLength) * sin(middleRad)).toFloat()
            val finalLineEndX = if (isRightSide) lineEndX + shoulderLineLength else lineEndX - shoulderLineLength

            val textX = if (isRightSide) finalLineEndX + linePadding else finalLineEndX - textWidth - linePadding
            val textY = lineEndY - (textHeight / 2f)

            val labelLinePath = Path().apply {
                moveTo(lineStartX, lineStartY)
                lineTo(lineEndX, lineEndY)
                lineTo(finalLineEndX, lineEndY)
            }

            drawPath(
                path = labelLinePath,
                color = lineColor,
                style = Stroke(
                    width = lineThickness,
                    join = labelCustomization.lineCustomization.join,
                    cap = labelCustomization.lineCustomization.cap,
                    miter = labelCustomization.lineCustomization.miter,
                    pathEffect = labelCustomization.lineCustomization.pathEffect
                )
            )

            drawText(
                textLayoutResult = textLayoutResult,
                color = labelCustomization.textColor,
                topLeft = Offset(textX, textY)
            )
        }

        if (data.slices.size == 1) {
            val slice = data.slices.first()
            val donutRingPath = Path().apply {
                addArc(Rect(Offset(centerX - outerRadius, centerY - outerRadius), Size(outerRadius * 2, outerRadius * 2)), 0f, 360f)
                addArc(Rect(Offset(centerX - innerRadius, centerY - innerRadius), Size(innerRadius * 2, innerRadius * 2)), 0f, -360f)
            }
            drawPath(donutRingPath, color = slice.color)
            data.sliceBorder?.let {
                drawBorderInside(donutRingPath, it)
            }

            val middleRad = data.startAngle * (PI / 180.0)
            drawOnEachSlice?.let {
                with(PieChartDrawScopeImpl(this)) {
                    it(this, slice.tag, centerX, centerY, outerRadius, innerRadius, middleRad)
                }
            }
            if (slice.label != null && data.labelCustomization != null) {
                drawLabel(slice.label, data.labelCustomization, middleRad)
            }
        } else {
            var startAngle = data.startAngle + data.sliceSpacingDegrees / 2f

            data.slices.forEach { slice ->
                val sweepAngle = ((slice.value / totalValue) * 360.0).toFloat() - data.sliceSpacingDegrees

                val startRad = startAngle * (PI / 180.0)
                val endRad = (startAngle + sweepAngle) * (PI / 180.0)

                val innerStartPointX = (centerX + innerRadius * cos(startRad)).toFloat()
                val innerStartPointY = (centerY + innerRadius * sin(startRad)).toFloat()
                val innerEndPointX = (centerX + innerRadius * cos(endRad)).toFloat()
                val innerEndPointY = (centerY + innerRadius * sin(endRad)).toFloat()

                val path = Path().apply {
                    moveTo(innerStartPointX, innerStartPointY)
                    arcTo(Rect(center, outerRadius), startAngle, sweepAngle, false)
                    lineTo(innerEndPointX, innerEndPointY)
                    arcTo(Rect(center, innerRadius), startAngle + sweepAngle, -sweepAngle, false)
                    close()
                }

                drawPath(path = path, color = slice.color)
                data.sliceBorder?.let {
                    drawBorderInside(path,it)
                }

                val middleDeg = (startAngle + sweepAngle / 2f) % 360f
                val middleRad = middleDeg * (PI / 180.0)

                drawOnEachSlice?.let {
                    with(PieChartDrawScopeImpl(this)) {
                        it(this, slice.tag, centerX, centerY, outerRadius, innerRadius, middleRad)
                    }
                }
                if (slice.label != null && data.labelCustomization != null) {
                    drawLabel(slice.label, data.labelCustomization, middleRad)
                }
                startAngle = (startAngle + sweepAngle + data.sliceSpacingDegrees) % 360f
            }
        }
    }
}