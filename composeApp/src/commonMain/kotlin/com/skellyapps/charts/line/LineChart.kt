package com.skellyapps.charts.line

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrDefault
import androidx.compose.ui.util.fastMinByOrNull
import androidx.compose.ui.util.fastRoundToInt
import com.skellyapps.charts.line.extension.getClosestPointIfInRange
import com.skellyapps.charts.line.extension.getMaxX
import com.skellyapps.charts.line.extension.getMaxY
import com.skellyapps.charts.line.extension.getMinX
import com.skellyapps.charts.line.extension.getMinY
import com.skellyapps.charts.line.extension.toLineChartPoint
import com.skellyapps.charts.line.extension.toOffsetLines
import com.skellyapps.charts.line.model.DraggedPointDistance
import com.skellyapps.charts.line.model.LineChartData
import kotlin.math.max
import kotlin.math.min

@Composable
fun LineChart(
    modifier: Modifier,
    data: LineChartData,
    onEachPoint: (DrawScope.(canvasSize: IntSize, lineTag: Byte, index: Int, offset: Offset) -> Unit)? = null,
    drag: LineChartData.DragCallback? = null,
    dragAfterLongPress: LineChartData.DragCallback? = null
) {
    var canvasSize by remember { mutableStateOf(IntSize(0,0)) }
    val minXValue by remember {
        derivedStateOf {
            if (data.bottomAxis?.minValue != null) {
                data.bottomAxis.minValue
            } else {
                val minXLeftAxis = data.leftAxis?.lines?.minOfOrNull { it.getMinX() ?: 0.0 }
                val minXRightAxis = data.rightAxis?.lines?.minOfOrNull { it.getMinX() ?: 0.0 }
                if (minXLeftAxis == null) {
                    minXRightAxis ?: 0.0
                } else if (minXRightAxis == null) {
                    minXLeftAxis
                } else {
                    min(minXLeftAxis, minXRightAxis)
                }
            }
        }
    }
    val maxXValue by remember {
        derivedStateOf {
            if (data.bottomAxis?.maxValue != null) {
                data.bottomAxis.maxValue
            } else {
                val maxXLeftAxis = data.leftAxis?.lines?.maxOfOrNull { it.getMaxX() ?: 1.0 }
                val maxXRightAxis = data.rightAxis?.lines?.maxOfOrNull { it.getMaxX() ?: 1.0 }
                if (maxXLeftAxis == null) {
                    maxXRightAxis ?: 1.0
                } else if (maxXRightAxis == null) {
                    maxXLeftAxis
                } else {
                    max(maxXLeftAxis, maxXRightAxis)
                }
            }
        }
    }
    val leftAxisMinYValue by remember {
        derivedStateOf {
            if (data.leftAxis?.minValue != null) {
                data.leftAxis.minValue
            } else {
                data.leftAxis?.lines?.minOfOrNull { it.getMinY()!! } ?: 0.0
            }
        }
    }
    val leftAxisMaxYValue by remember {
        derivedStateOf {
            if (data.leftAxis?.maxValue != null) {
                data.leftAxis.maxValue
            } else {
                data.leftAxis?.lines?.maxOfOrNull { it.getMaxY()!! } ?: 1.0
            }
        }
    }
    val rightAxisMinYValue by remember {
        derivedStateOf {
            if (data.rightAxis?.minValue != null) {
                data.rightAxis.minValue
            } else {
                data.rightAxis?.lines?.minOfOrNull { it.getMinY()!! } ?: 0.0
            }
        }
    }
    val rightAxisMaxYValue by remember {
        derivedStateOf {
            if (data.rightAxis?.maxValue != null) {
                data.rightAxis.maxValue
            } else {
                data.rightAxis?.lines?.maxOfOrNull { it.getMaxY()!! } ?: 1.0
            }
        }
    }
    val leftOffsetLines by remember {
        derivedStateOf {
            data.leftAxis?.lines?.toOffsetLines(canvasSize, minXValue, maxXValue, data.xAxisLinesOffset, leftAxisMinYValue, leftAxisMaxYValue, data.leftAxis.yOffset)
        }
    }
    val rightOffsetLines by remember {
        derivedStateOf {
            data.rightAxis?.lines?.toOffsetLines(canvasSize, minXValue, maxXValue, data.xAxisLinesOffset, rightAxisMinYValue, rightAxisMaxYValue, data.rightAxis.yOffset)
        }
    }
    var draggedPointDistance by remember { mutableStateOf<DraggedPointDistance?>(null) }

    Row(modifier) {
        data.leftAxis?.let { axis ->
            Box(Modifier.height(canvasSize.height.dp)) {
                axis.valueView?.let {
                    val leftAxisValues by remember(leftAxisMinYValue, leftAxisMaxYValue, axis.step) {
                        val values = mutableListOf<Double>()
                        var value = leftAxisMinYValue
                        while (value <= leftAxisMaxYValue) {
                            values.add(value)
                            value += axis.step
                        }
                        mutableStateOf(values)
                    }
                    AxisColumn(Modifier.fillMaxHeight(), axis.yOffset, true, leftAxisValues, leftAxisMinYValue, leftAxisMaxYValue) {
                        leftAxisValues.fastForEach { value ->
                            it(value)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f)) {
            LineChartCanvas(Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .onSizeChanged {
                    canvasSize = it
                }
                .pointerInput(Unit) {
                    if (drag != null) {
                        detectDragGestures(onDragStart = { offset ->
                            val draggedPointsDistance = mutableListOf<DraggedPointDistance>()
                            if (leftOffsetLines != null) {
                                leftOffsetLines!!.fastForEach {
                                    it.getClosestPointIfInRange(offset, drag.isInRangePx, true)?.let {
                                        draggedPointsDistance.add(it)
                                    }
                                }
                            }
                            if (rightOffsetLines != null) {
                                rightOffsetLines!!.fastForEach {
                                    it.getClosestPointIfInRange(offset, drag.isInRangePx, false)?.let {
                                        draggedPointsDistance.add(it)
                                    }
                                }
                            }
                            draggedPointDistance = draggedPointsDistance.fastMinByOrNull { it.distance }
                        }, onDragEnd = {draggedPointDistance = null}, onDragCancel = {draggedPointDistance = null}) { change, offset ->
                            if (draggedPointDistance != null) {
                                val draggedPoint = draggedPointDistance!!.draggedPoint
                                val point = change.position.toLineChartPoint(
                                    data,
                                    draggedPointDistance!!,
                                    canvasSize,
                                    minXValue,
                                    maxXValue,
                                    if (draggedPointDistance!!.isLeftAxis) leftAxisMinYValue else rightAxisMinYValue,
                                    if (draggedPointDistance!!.isLeftAxis) leftAxisMaxYValue else rightAxisMaxYValue
                                )
                                drag.pointDragged(draggedPoint.lineTag, draggedPoint.index, point)
                            }
                        }
                    }
                    if (dragAfterLongPress != null) {
                        detectDragGesturesAfterLongPress(onDragStart = { offset ->
                            val draggedPointsDistance = mutableListOf<DraggedPointDistance>()
                            if (leftOffsetLines != null) {
                                leftOffsetLines!!.fastForEach {
                                    it.getClosestPointIfInRange(offset, dragAfterLongPress.isInRangePx, true)?.let {
                                        draggedPointsDistance.add(it)
                                    }
                                }
                            }
                            if (rightOffsetLines != null) {
                                rightOffsetLines!!.fastForEach {
                                    it.getClosestPointIfInRange(offset, dragAfterLongPress.isInRangePx, false)?.let {
                                        draggedPointsDistance.add(it)
                                    }
                                }
                            }
                            draggedPointDistance = draggedPointsDistance.fastMinByOrNull { it.distance }
                        }, onDragEnd = {draggedPointDistance = null}, onDragCancel = {draggedPointDistance = null}) { change, offset ->
                            if (draggedPointDistance != null) {
                                val draggedPoint = draggedPointDistance!!.draggedPoint
                                val point = change.position.toLineChartPoint(
                                    data,
                                    draggedPointDistance!!,
                                    canvasSize,
                                    minXValue,
                                    maxXValue,
                                    if (draggedPointDistance!!.isLeftAxis) leftAxisMinYValue else rightAxisMinYValue,
                                    if (draggedPointDistance!!.isLeftAxis) leftAxisMaxYValue else rightAxisMaxYValue
                                )
                                dragAfterLongPress.pointDragged(draggedPoint.lineTag, draggedPoint.index, point)
                            }
                        }
                    }
                },
                canvasSize,
                data,
                onEachPoint,
                leftOffsetLines,
                rightOffsetLines
            )
            data.bottomAxis?.let { axis ->
                axis.valueView?.let {
                    val bottomAxisValues by remember(minXValue, maxXValue, axis.step) {
                        val values = mutableListOf<Double>()
                        var value = minXValue
                        while (value <= maxXValue) {
                            values.add(value)
                            value += axis.step
                        }
                        mutableStateOf(values)
                    }
                    AxisRow(Modifier.fillMaxWidth(), data.xAxisLinesOffset, bottomAxisValues, minXValue, maxXValue) {
                        bottomAxisValues.fastForEach { value ->
                            it(value)
                        }
                    }
                }
            }
        }
        data.rightAxis?.let { axis ->
            Box(Modifier.height(canvasSize.height.dp)) {
                axis.valueView?.let {
                    val rightAxisValues by remember(rightAxisMinYValue, rightAxisMaxYValue, axis.step) {
                        val values = mutableListOf<Double>()
                        var value = rightAxisMinYValue
                        while (value <= rightAxisMaxYValue) {
                            values.add(value)
                            value += axis.step
                        }
                        mutableStateOf(values)
                    }
                    AxisColumn(Modifier.fillMaxHeight(), axis.yOffset, false, rightAxisValues, rightAxisMinYValue, rightAxisMaxYValue) {
                        rightAxisValues.fastForEach { value ->
                            it(value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LineChartCanvas(
    modifier: Modifier,
    canvasSize: IntSize,
    data: LineChartData,
    onEachPoint: (DrawScope.(canvasSize: IntSize, lineTag: Byte, index: Int, offset: Offset) -> Unit)?,
    leftOffsetLines: List<LineChartData.OffsetLine>?,
    rightOffsetLines: List<LineChartData.OffsetLine>?
) {
    Canvas(modifier) {
        //Draw left axis divider
        data.leftAxis?.dividerCustomization?.let {
            val thickness = it.thickness.toPx()
            drawLine(
                it.brush,
                Offset(thickness / 2f , thickness / 2f),
                Offset(thickness / 2f, canvasSize.height.toFloat() - thickness / 2f),
                thickness,
                it.cap,
                it.pathEffect,
                it.alpha,
                it.colorFilter,
                it.blendMode
            )
        }
        //Draw right axis divider
        data.rightAxis?.dividerCustomization?.let {
            val thickness = it.thickness.toPx()
            drawLine(
                it.brush,
                Offset(canvasSize.width.toFloat() - thickness / 2f, thickness / 2f),
                Offset(canvasSize.width.toFloat() - thickness / 2f, canvasSize.height.toFloat() - thickness / 2f),
                thickness,
                it.cap,
                it.pathEffect,
                it.alpha,
                it.colorFilter,
                it.blendMode
            )
        }
        //Draw bottom axis divider
        data.bottomAxis?.dividerCustomization?.let {
            val thickness = it.thickness.toPx()
            drawLine(
                it.brush,
                Offset(thickness / 2f, canvasSize.height.toFloat() - thickness / 2f),
                Offset(canvasSize.width.toFloat() - thickness / 2f, canvasSize.height.toFloat() - thickness / 2f),
                thickness,
                it.cap,
                it.pathEffect,
                it.alpha,
                it.colorFilter,
                it.blendMode
            )
        }
        //Draw the lines connecting the points
        leftOffsetLines?.fastForEach { line ->
            drawPoints(
                line.offsets,
                line.customization.pointMode,
                line.customization.brush,
                line.customization.thickness,
                line.customization.cap,
                line.customization.pathEffect,
                line.customization.alpha,
                line.customization.colorFilter,
                line.customization.blendMode
            )
            //Let the users config what they wants on the point
            onEachPoint?.let {
                line.offsets.fastForEachIndexed { index, offset ->
                    onEachPoint(this, canvasSize, line.tag, index, offset)
                }
            }
        }
        //Draw the lines connecting the points
        rightOffsetLines?.fastForEach { line ->
            drawPoints(
                line.offsets,
                line.customization.pointMode,
                line.customization.brush,
                line.customization.thickness,
                line.customization.cap,
                line.customization.pathEffect,
                line.customization.alpha,
                line.customization.colorFilter,
                line.customization.blendMode
            )
            //Let the users config what they wants on the point
            onEachPoint?.let {
                line.offsets.fastForEachIndexed { index, offset ->
                    onEachPoint(this, canvasSize, line.tag, index, offset)
                }
            }
        }
    }
}

@Composable
private fun AxisRow(
    modifier: Modifier = Modifier,
    axisOffset:  LineChartData.AxisOffset,
    values: List<Double>,
    minXValue: Double,
    maxXValue: Double,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.map { measurable ->
            // Measure each children
            measurable.measure(constraints)
        }
        val maxHeight = placeables.fastMaxOfOrDefault(0) { it.height }
        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, maxHeight) {
            // Place children in the parent layout
            placeables.fastForEachIndexed { index, placeable ->
                // Position item on the screen
                val xOffset = (((values[index] - minXValue) / (maxXValue - minXValue)) * (constraints.maxWidth - axisOffset.min - axisOffset.max) + axisOffset.min).fastRoundToInt() - (placeable.width / 2)
                placeable.placeRelative(x = xOffset , y = 0)
            }
        }
    }
}

@Composable
private fun AxisColumn(
    modifier: Modifier = Modifier,
    axisOffset: LineChartData.AxisOffset,
    leftAxis: Boolean,
    values: List<Double>,
    minYValue: Double,
    maxYValue: Double,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.map { measurable ->
            // Measure each children
            measurable.measure(constraints)
        }
        val maxWidth = placeables.fastMaxOfOrDefault(0) { it.width }
        // Set the size of the layout as big as it can
        layout(maxWidth, constraints.maxHeight) {
            // Place children in the parent layout
            placeables.fastForEachIndexed { index, placeable ->
                // Position item on the screen
                val yOffset = (constraints.maxHeight - (((values[index] - minYValue) / (maxYValue - minYValue)) * (constraints.maxHeight - axisOffset.min - axisOffset.max) + axisOffset.min)).fastRoundToInt() - (placeable.height / 2)
                if (leftAxis) {
                    placeable.placeRelative(x = maxWidth - placeable.width, y = yOffset)
                } else {
                    placeable.placeRelative(x = 0, y = yOffset)
                }
            }
        }
    }
}