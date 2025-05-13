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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrDefault
import androidx.compose.ui.util.fastMinByOrNull
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.zIndex
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
    onEachPoint: (DrawScope.(canvasSize: Size, lineTag: Byte, index: Int, offset: Offset) -> Unit)? = null,
    drag: LineChartData.DragCallback? = null,
    dragAfterLongPress: LineChartData.DragCallback? = null
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize(0,0)) }
    val minXValue by remember {
        derivedStateOf {
            if (data.bottomAxis?.minValue != null) {
                data.bottomAxis.minValue
            } else {
                val minXLeftAxis = data.leftAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.minOfOrNull { it.getMinX()!! }
                val minXRightAxis = data.rightAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.minOfOrNull { it.getMinX()!! }
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
                val maxXLeftAxis = data.leftAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.maxOfOrNull { it.getMaxX()!! }
                val maxXRightAxis = data.rightAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.maxOfOrNull { it.getMaxX()!! }
                val max = if (maxXLeftAxis == null) {
                    maxXRightAxis ?: 1.0
                } else if (maxXRightAxis == null) {
                    maxXLeftAxis
                } else {
                    max(maxXLeftAxis, maxXRightAxis)
                }
                if (max == minXValue) {
                    max + 1
                } else {
                    max
                }
            }
        }
    }
    val leftAxisMinYValue by remember {
        derivedStateOf {
            if (data.leftAxis?.minValue != null) {
                data.leftAxis.minValue
            } else {
                data.leftAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.minOfOrNull { it.getMinY()!! } ?: 0.0
            }
        }
    }
    val leftAxisMaxYValue by remember {
        derivedStateOf {
            if (data.leftAxis?.maxValue != null) {
                data.leftAxis.maxValue
            } else {
                val max = data.leftAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.maxOfOrNull { it.getMaxY()!! } ?: 1.0
                if (max == leftAxisMinYValue) {
                    max + 1
                } else {
                    max
                }
            }
        }
    }
    val rightAxisMinYValue by remember {
        derivedStateOf {
            if (data.rightAxis?.minValue != null) {
                data.rightAxis.minValue
            } else {
                data.rightAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.minOfOrNull { it.getMinY()!! } ?: 0.0
            }
        }
    }
    val rightAxisMaxYValue by remember {
        derivedStateOf {
            if (data.rightAxis?.maxValue != null) {
                data.rightAxis.maxValue
            } else {
                val max = data.rightAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.maxOfOrNull { it.getMaxY()!! } ?: 1.0
                if (max == leftAxisMinYValue) {
                    max + 1
                } else {
                    max
                }
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
    val leftAxisValues by remember(leftAxisMinYValue, leftAxisMaxYValue, data.leftAxis) {
        mutableStateOf(data.leftAxis?.value?.getValues(leftAxisMinYValue, leftAxisMaxYValue) ?: listOf())
    }
    val rightAxisValues by remember(rightAxisMinYValue, rightAxisMaxYValue, data.rightAxis) {
        mutableStateOf(data.rightAxis?.value?.getValues(rightAxisMinYValue, rightAxisMaxYValue) ?: listOf())
    }
    val bottomAxisValues: List<Double> by remember(minXValue, maxXValue, data.bottomAxis) {
        mutableStateOf(data.bottomAxis?.value?.getValues(minXValue, maxXValue) ?: listOf())
    }
    var zoom by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var draggedPointDistance by remember { mutableStateOf<DraggedPointDistance?>(null) }

    Row(modifier) {
        data.leftAxis?.let { axis ->
            Box(Modifier.height(with(density) { canvasSize.height.toDp() })) {
                axis.valueView?.let {
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
                .onSizeChanged {
                    canvasSize = it
                }
//                .pointerInput(Unit) {
//                    detectTransformGestures { centroid, pan, gestureZoom, gestureRotate ->
//                        val oldScale = zoom
//                        val newScale = (zoom * gestureZoom).coerceIn(1f, 5f)
//                        val newOffset = (offset + centroid / oldScale) - (centroid / newScale + pan / oldScale)
//                        offset = newOffset.copy(newOffset.x.coerceIn(0f, canvasSize.width - canvasSize.width / newScale), newOffset.y.coerceIn(0f, canvasSize.height - canvasSize.height / newScale))
//                        zoom = newScale
//                    }
//                }
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
                }
                .pointerInput(Unit) {
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
                }
                    .graphicsLayer {
                    translationX = -offset.x * zoom
                    translationY = -offset.y * zoom
                    scaleX = zoom
                    scaleY = zoom
                    transformOrigin = TransformOrigin(0f, 0f)
                },
                data,
                leftAxisMinYValue,
                leftAxisMaxYValue,
                rightAxisMinYValue,
                rightAxisMaxYValue,
                minXValue,
                maxXValue,
                leftAxisValues,
                rightAxisValues,
                bottomAxisValues,
                onEachPoint,
                leftOffsetLines,
                rightOffsetLines
            )
            data.bottomAxis?.let { axis ->
                axis.valueView?.let {
                    AxisRow(Modifier.fillMaxWidth().zIndex(-1f), data.xAxisLinesOffset, bottomAxisValues, minXValue, maxXValue) {
                        bottomAxisValues.fastForEach { value ->
                            it(value)
                        }
                    }
                }
            }
        }
        data.rightAxis?.let { axis ->
            Box(Modifier.height(with(density) { canvasSize.height.toDp() })) {
                axis.valueView?.let {
                    AxisColumn(Modifier.fillMaxHeight().zIndex(-1f), axis.yOffset, false, rightAxisValues, rightAxisMinYValue, rightAxisMaxYValue) {
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
    data: LineChartData,
    leftAxisMinYValue: Double,
    leftAxisMaxYValue: Double,
    rightAxisMinYValue: Double,
    rightAxisMaxYValue: Double,
    minXValue: Double,
    maxXValue: Double,
    leftAxisValues: List<Double>,
    rightAxisValues: List<Double>,
    bottomAxisValues: List<Double>,
    onEachPoint: (DrawScope.(canvasSize: Size, lineTag: Byte, index: Int, offset: Offset) -> Unit)?,
    leftOffsetLines: List<LineChartData.OffsetLine>?,
    rightOffsetLines: List<LineChartData.OffsetLine>?
) {
    Canvas(modifier) {
        data.leftAxis?.let { axis ->
            axis.gridLines?.let {
                val thickness = it.customization.thickness.toPx()
                val startIndex = if (it.showFirstLine) 0 else 1
                val endIndex = if (it.showLastLine) leftAxisValues.size - 1 else leftAxisValues.size - 2
                for (i in startIndex..endIndex) {
                    val value = leftAxisValues[i]
                    val yOffset = (size.height - (((value - leftAxisMinYValue) / (leftAxisMaxYValue - leftAxisMinYValue)) * (size.height - axis.yOffset.min - axis.yOffset.max) + axis.yOffset.min)).toFloat()
                    drawLine(
                        it.customization.brush,
                        Offset(0f, yOffset),
                        Offset(size.width, yOffset),
                        thickness,
                        it.customization.cap,
                        it.customization.pathEffect,
                        it.customization.alpha,
                        it.customization.colorFilter,
                        it.customization.blendMode
                    )
                }
            }
        }
        data.rightAxis?.let { axis ->
            axis.gridLines?.let {
                val thickness = it.customization.thickness.toPx()
                val startIndex = if (it.showFirstLine) 0 else 1
                val endIndex = if (it.showLastLine) rightAxisValues.size - 1 else rightAxisValues.size - 2
                for (i in startIndex..endIndex) {
                    val value = rightAxisValues[i]
                    val yOffset = (size.height - (((value - rightAxisMinYValue) / (rightAxisMaxYValue - rightAxisMinYValue)) * (size.height - axis.yOffset.min - axis.yOffset.max) + axis.yOffset.min)).toFloat()
                    drawLine(
                        it.customization.brush,
                        Offset(size.width, yOffset),
                        Offset(0f, yOffset),
                        thickness,
                        it.customization.cap,
                        it.customization.pathEffect,
                        it.customization.alpha,
                        it.customization.colorFilter,
                        it.customization.blendMode
                    )
                }
            }
        }
        data.bottomAxis?.let { axis ->
            axis.gridLines?.let {
                val thickness = it.customization.thickness.toPx()
                val startIndex = if (it.showFirstLine) 0 else 1
                val endIndex = if (it.showLastLine) bottomAxisValues.size - 1 else bottomAxisValues.size - 2
                for (i in startIndex..endIndex) {
                    val value = bottomAxisValues[i]
                    val xOffset = (((value - minXValue) / (maxXValue - minXValue)) * (size.width - data.xAxisLinesOffset.min - data.xAxisLinesOffset.max) + data.xAxisLinesOffset.min).toFloat()
                    drawLine(
                        it.customization.brush,
                        Offset(xOffset, 0f),
                        Offset(xOffset, size.height),
                        thickness,
                        it.customization.cap,
                        it.customization.pathEffect,
                        it.customization.alpha,
                        it.customization.colorFilter,
                        it.customization.blendMode
                    )
                }
            }
        }
        //Draw left axis divider
        data.leftAxis?.dividerCustomization?.let {
            val thickness = it.thickness.toPx()
            drawLine(
                it.brush,
                Offset(0f , 0f),
                Offset(0f, size.height),
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
                Offset(size.width, 0f),
                Offset(size.width, size.height),
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
                Offset(0f, size.height),
                Offset(size.width, size.height),
                thickness,
                StrokeCap.Square,
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
                    onEachPoint(this, size, line.tag, index, offset)
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
                    onEachPoint(this, size, line.tag, index, offset)
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
                val xOffset = ((((values[index] - minXValue) / (maxXValue - minXValue)) * (constraints.maxWidth - axisOffset.min - axisOffset.max) + axisOffset.min) - (placeable.width / 2.0)).fastRoundToInt()
                placeable.placeRelative(x = xOffset, y = 0)
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
                val yOffset = ((constraints.maxHeight - (((values[index] - minYValue) / (maxYValue - minYValue)) * (constraints.maxHeight - axisOffset.min - axisOffset.max) + axisOffset.min)) - (placeable.height / 2.0)).fastRoundToInt()
                if (leftAxis) {
                    placeable.placeRelative(x = maxWidth - placeable.width, y = yOffset)
                } else {
                    placeable.placeRelative(x = 0, y = yOffset)
                }
            }
        }
    }
}