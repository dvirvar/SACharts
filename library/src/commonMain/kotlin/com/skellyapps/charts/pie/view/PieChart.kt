package com.skellyapps.charts.pie.view

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastForEach
import com.skellyapps.charts.pie.animation.PieChartAnimations
import com.skellyapps.charts.pie.extension.drawBorderInside
import com.skellyapps.charts.pie.model.PieChartData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * @param modifier Mandatory modifier to specify size
 * @param data [PieChartData]
 * @param animations To enable animations
 * @param drawOnEachSlice To draw on each slice
 */
@Composable
fun PieChart(
    modifier: Modifier,
    data: PieChartData,
    animations: PieChartAnimations = PieChartAnimations.None,
    drawOnEachSlice: (DrawScope.(sliceTag: Int, centerX: Float, centerY: Float, outerRadius: Float, innerRadius: Float, middleRad: Double) -> Unit)? = null
) {
    val totalValue by remember(data.slices) {
        derivedStateOf {
            data.slices.sumOf { it.value }
        }
    }
    val textLayouts by remember(data.slices, data.labelCustomization) {
        derivedStateOf {
            if (data.labelCustomization != null) {
                buildMap {
                    data.slices.fastForEach {
                        if (it.label != null) {
                            this[it.label] = data.labelCustomization.textMeasurer.measure(it.label)
                        }
                    }
                }
            } else {
                mapOf()
            }
        }
    }
    val path = remember { Path() }
    Canvas(modifier) {
        val centerX = center.x
        val centerY = center.y
        val outerRadius = minOf(centerX, centerY) * data.outerRadiusPercentage * if (animations.scale != null) animations.scale.value else 1f
        val innerRadius = outerRadius * data.innerRadiusPercentage

        fun drawLabel(label: String, labelCustomization: PieChartData.LabelCustomization, middleRad: Double) {
            val edgePadding = labelCustomization.edgePadding.toPx()
            val linePadding = labelCustomization.linePadding.toPx()
            val lineColor = labelCustomization.lineCustomization.color
            val lineThickness = labelCustomization.lineCustomization.thickness.toPx()
            val extensionLineMaxLength = labelCustomization.lineCustomization.extensionMaxLength.toPx()
            val shoulderLineLength = labelCustomization.lineCustomization.shoulderLength.toPx()

            //Line start point (On the edge of the slice)
            val lineStartX = (centerX + outerRadius * cos(middleRad)).toFloat()
            val lineStartY = (centerY + outerRadius * sin(middleRad)).toFloat()

            val textLayoutResult = textLayouts[label]!!
            val textWidth = textLayoutResult.size.width
            val textHeight = textLayoutResult.size.height

            //Calculate ideal/target positions
            val isRightSide = cos(middleRad) > 0

            val idealLineEndX = (centerX + (outerRadius + extensionLineMaxLength) * cos(middleRad)).toFloat()
            val idealLineEndY = (centerY + (outerRadius + extensionLineMaxLength) * sin(middleRad)).toFloat()
            val idealFinalLineEndX = if (isRightSide) idealLineEndX + shoulderLineLength else idealLineEndX - shoulderLineLength

            //Determine initial target X for the text
            val idealTextX = if (isRightSide) idealFinalLineEndX + linePadding else idealFinalLineEndX - textWidth - linePadding
            val idealTextY = idealLineEndY - (textHeight / 2f)

            //Clamp the text positions so they never exceed canvas bounds
            //Left bound: edgePadding | Right bound: size.width - textWidth - edgePadding
            val clampedTextX = idealTextX.fastCoerceIn(edgePadding, size.width - textWidth - edgePadding)
            val clampedTextY = idealTextY.fastCoerceIn(edgePadding, size.height - textHeight - edgePadding)

            //Adjust the extension line backward from the clamped text position
            val adjustedFinalLineEndX = if (isRightSide) clampedTextX - linePadding else clampedTextX + textWidth + linePadding
            val adjustedLineEndY = clampedTextY + (textHeight / 2f)

            //The inflection (shoulder) point of the line shifts slightly to match the vertical clamp
            val adjustedLineEndX = if (isRightSide) adjustedFinalLineEndX - shoulderLineLength else adjustedFinalLineEndX + shoulderLineLength

            //Combine extension and shoulder lines into a path
            val labelLinePath = path.apply {
                moveTo(lineStartX, lineStartY)
                lineTo(adjustedLineEndX, adjustedLineEndY)
                lineTo(adjustedFinalLineEndX, adjustedLineEndY)
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
            labelLinePath.reset()

            drawText(
                textLayoutResult = textLayoutResult,
                color = labelCustomization.textColor,
                topLeft = Offset(clampedTextX, clampedTextY)
            )
        }

        if (data.slices.size == 1) {
            val slice = data.slices[0]
            val progress = if (animations.growth != null) animations.growth.value else 1f
            val animatedSweepAngle = 360f * progress

            val outerRect = Rect(Offset(centerX - outerRadius, centerY - outerRadius), Size(outerRadius * 2, outerRadius * 2))
            val innerRect = Rect(Offset(centerX - innerRadius, centerY - innerRadius), Size(innerRadius * 2, innerRadius * 2))

            val donutRingPath = path.apply {
                if (animatedSweepAngle < 360f) {
                    val startAngle = data.startAngle
                    arcTo(
                        rect = outerRect,
                        startAngleDegrees = startAngle,
                        sweepAngleDegrees = animatedSweepAngle,
                        forceMoveTo = true
                    )
                    arcTo(
                        rect = innerRect,
                        startAngleDegrees = startAngle + animatedSweepAngle,
                        sweepAngleDegrees = -animatedSweepAngle,
                        forceMoveTo = false
                    )
                } else {
                    addArc(outerRect, 0f, 360f)
                    addArc(innerRect, 0f, -360f)
                }
                close()
            }

            drawPath(donutRingPath, color = slice.color)
            data.sliceBorder?.let {
                drawBorderInside(donutRingPath, it)
            }
            donutRingPath.reset()
            val middleRad = data.startAngle * (PI / 180.0)
            drawOnEachSlice?.let {
                it(this, slice.tag, centerX, centerY, outerRadius, innerRadius, middleRad)
            }
            if (slice.label != null && data.labelCustomization != null) {
                drawLabel(slice.label, data.labelCustomization, middleRad)
            }
        } else {
            var startAngle = data.startAngle + data.sliceSpacingDegrees / 2f
            val animationValue = if (animations.growth != null) animations.growth.value else 1f

            data.slices.fastForEach { slice ->
                val sweepAngle = (((slice.value / totalValue) * 360.0).toFloat() - data.sliceSpacingDegrees) * animationValue

                val startRad = startAngle * (PI / 180.0)
                val endRad = (startAngle + sweepAngle) * (PI / 180.0)

                val innerStartPointX = (centerX + innerRadius * cos(startRad)).toFloat()
                val innerStartPointY = (centerY + innerRadius * sin(startRad)).toFloat()

                val innerEndPointX = (centerX + innerRadius * cos(endRad)).toFloat()
                val innerEndPointY = (centerY + innerRadius * sin(endRad)).toFloat()

                //Create a Path for each slice
                val path = path.apply {
                    //Move to the start point of the inner arc
                    moveTo(innerStartPointX, innerStartPointY)
                    //Draw the outer arc
                    arcTo(
                        rect = Rect(center, outerRadius),
                        startAngleDegrees = startAngle,
                        sweepAngleDegrees = sweepAngle,
                        forceMoveTo = false
                    )
                    //Draw a line from the end of the outer arc to the end of the inner arc
                    lineTo(innerEndPointX, innerEndPointY)

                    //Draw the inner arc
                    //We're drawing it backward (from end to start) to ensure it closes correctly.
                    arcTo(
                        rect = Rect(center, innerRadius),
                        startAngleDegrees = startAngle + sweepAngle, //Start from the end of the sweep
                        sweepAngleDegrees = -sweepAngle, //Sweep backwards to the start
                        forceMoveTo = false
                    )
                    close()
                }

                //Draw the filled path
                drawPath(
                    path = path,
                    color = slice.color
                )

                data.sliceBorder?.let {
                    drawBorderInside(path, it)
                }
                path.reset()
                val middleDeg = (startAngle + sweepAngle / 2f) % 360f
                val middleRad = middleDeg * (PI / 180.0)
                drawOnEachSlice?.let {
                    it(this, slice.tag, centerX, centerY, outerRadius, innerRadius, middleRad)
                }
                if (slice.label != null && data.labelCustomization != null) {
                    drawLabel(slice.label, data.labelCustomization, middleRad)
                }
                startAngle = (startAngle + sweepAngle + data.sliceSpacingDegrees) % 360f
            }
        }
    }
}