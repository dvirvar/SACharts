package com.skellyapps.charts.line

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrDefault
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.zIndex
import com.skellyapps.charts.line.extension.detectTransformGestures
import com.skellyapps.charts.line.extension.getClosestIndexDistance
import com.skellyapps.charts.line.extension.getMaxX
import com.skellyapps.charts.line.extension.getMaxY
import com.skellyapps.charts.line.extension.getMinX
import com.skellyapps.charts.line.extension.getMinY
import com.skellyapps.charts.line.extension.`if`
import com.skellyapps.charts.line.extension.toLineChartPoint
import com.skellyapps.charts.line.extension.toOffsetLines
import com.skellyapps.charts.line.model.ClickedPoint
import com.skellyapps.charts.line.model.DraggedPoint
import com.skellyapps.charts.line.model.LineChartData
import com.skellyapps.charts.line.model.Zoom
import kotlin.math.max
import kotlin.math.min

private val gridLinesZIndex = 10f
private val dividersZIndex = 20f
private val linesZIndex = 30f
private val axesZIndex = -1f

@Composable
fun LineChart(
    modifier: Modifier,
    data: LineChartData,
    zoom: Zoom? = null,
    onEachPoint: (DrawScope.(canvasSize: Size, lineTag: Byte, index: Int, offset: Offset) -> Unit)? = null,
    pointClick: LineChartData.PointClick? = null,
    pointDrag: LineChartData.PointDrag? = null,
    pointDragAfterLongPress: LineChartData.PointDrag? = null,
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize(0,0)) }
    var canvasZoom by remember { mutableStateOf(1f) }
    var canvasOffset by remember { mutableStateOf(Offset.Zero) }
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
    val clipToBounds by remember {
        derivedStateOf {
            canvasZoom != 1f
        }
    }
    var clickedPoint by remember { mutableStateOf<ClickedPoint?>(null) }
    val clickedPointOffset by remember {
        derivedStateOf {
            clickedPoint?.let { p -> if (p.isLeftAxis) leftOffsetLines?.fastFirstOrNull { it.tag == p.lineTag }?.offsets[p.index] else rightOffsetLines?.fastFirstOrNull { it.tag == p.lineTag }?.offsets[p.index] }
        }
    }
    var draggedPoint by remember { mutableStateOf<DraggedPoint?>(null) }

    Row(modifier) {
        data.leftAxis?.let { axis ->
            axis.valueView?.let {
                AxisColumn(Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex), canvasZoom, canvasOffset.y, axis.yOffset, true, leftAxisValues, leftAxisMinYValue, leftAxisMaxYValue) {
                    leftAxisValues.fastForEach { value ->
                        it(value)
                    }
                }
            }
        }
        Column(Modifier.weight(1f)) {
            LineChartCanvas(
                Modifier.fillMaxWidth().weight(1f).onSizeChanged {
                    canvasSize = it
                },
                Modifier
                    .`if`(clipToBounds, Modifier.clipToBounds())
                    .`if`(zoom != null, Modifier.pointerInput(Unit) {
                        detectTransformGestures(
                            onGesture = { centroid,
                                          pan,
                                          gestureZoom,
                                          type,
                                          changes ->
                                if (type == PointerEventType.Scroll || changes.size > 1) {
                                    val newScale = if (type == PointerEventType.Scroll) {
                                        val addition = if (gestureZoom < 0) zoom!!.scrollJump else -zoom!!.scrollJump
                                        (canvasZoom + addition).coerceIn(1f, zoom.max)
                                    } else {
                                        (canvasZoom * gestureZoom).coerceIn(1f, zoom!!.max)
                                    }
                                    val newOffset = (canvasOffset + centroid / canvasZoom) - (centroid / newScale + pan / canvasZoom)
                                    canvasOffset = newOffset.copy(newOffset.x.coerceIn(0f, size.width - size.width / newScale), newOffset.y.coerceIn(0f, size.height - size.height / newScale))
                                    canvasZoom = newScale
                                    changes.forEach { it.consume() }
                                }
                            }
                        )
                    })
                    .`if`(pointClick != null, Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val offset = offset / canvasZoom + canvasOffset
                                var minDistance = -1f
                                var minDistanceClickedPoint: ClickedPoint? = null
                                leftOffsetLines?.fastForEach { line ->
                                    line.getClosestIndexDistance(offset) { point, press ->
                                        pointClick!!.isPointInRange(
                                            density,
                                            point,
                                            press
                                        )
                                    }?.let {
                                        if (minDistance < 0f || it.second < minDistance) {
                                            minDistance = it.second
                                            minDistanceClickedPoint = ClickedPoint(it.first, line.tag, true)
                                        }
                                    }
                                }
                                rightOffsetLines?.fastForEach { line ->
                                    line.getClosestIndexDistance(offset) { point, press ->
                                        pointClick!!.isPointInRange(
                                            density,
                                            point,
                                            press
                                        )
                                    }?.let {
                                        if (minDistance < 0f || it.second < minDistance) {
                                            minDistance = it.second
                                            minDistanceClickedPoint = ClickedPoint(it.first, line.tag, false)
                                        }
                                    }
                                }
                                clickedPoint = minDistanceClickedPoint
                            }
                        )
                    })
                    .pointerInput(Unit) {
                        detectDragGestures(onDragStart = { offset ->
                            if (pointDrag == null) {
                                return@detectDragGestures
                            }
                            val offset = offset / canvasZoom + canvasOffset
                            var minDistance = -1f
                            var minDistanceDraggedPoint: DraggedPoint? = null
                            leftOffsetLines?.fastForEach { line ->
                                line.getClosestIndexDistance(offset) { point, press ->
                                    pointDrag.isPointInRange(
                                        density,
                                        point,
                                        press
                                    )
                                }?.let {
                                    if (minDistance < 0f || it.second < minDistance) {
                                        minDistance = it.second
                                        minDistanceDraggedPoint = DraggedPoint(it.first, line.tag, true)
                                    }
                                }
                            }
                            rightOffsetLines?.fastForEach { line ->
                                line.getClosestIndexDistance(offset) { point, press ->
                                    pointDrag.isPointInRange(
                                        density,
                                        point,
                                        press
                                    )
                                }?.let {
                                    if (minDistance < 0f || it.second < minDistance) {
                                        minDistance = it.second
                                        minDistanceDraggedPoint = DraggedPoint(it.first, line.tag, false)
                                    }
                                }
                            }
                            draggedPoint = minDistanceDraggedPoint
                        }, onDragEnd = {draggedPoint = null}, onDragCancel = {draggedPoint = null}) { change, offset ->
                            if (pointDrag != null && draggedPoint != null) {
                                val position = change.position / canvasZoom + canvasOffset
                                val point = position.toLineChartPoint(
                                    data,
                                    draggedPoint!!.isLeftAxis,
                                    size.width,
                                    size.height,
                                    minXValue,
                                    maxXValue,
                                    if (draggedPoint!!.isLeftAxis) leftAxisMinYValue else rightAxisMinYValue,
                                    if (draggedPoint!!.isLeftAxis) leftAxisMaxYValue else rightAxisMaxYValue
                                )
                                pointDrag.pointDragged(draggedPoint!!.lineTag, draggedPoint!!.index, point)
                            } else {
                                canvasOffset = (canvasOffset - offset / canvasZoom).run {
                                    Offset(x.coerceIn(0f, size.width - size.width / canvasZoom), y.coerceIn(0f, size.height - size.height / canvasZoom))
                                }
                            }
                        }
                    }
                    .`if`(pointDragAfterLongPress != null, Modifier.pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(onDragStart = { offset ->
                            val offset = offset / canvasZoom + canvasOffset
                            var minDistance = -1f
                            var minDistanceDraggedPoint: DraggedPoint? = null
                            leftOffsetLines?.fastForEach { line ->
                                line.getClosestIndexDistance(offset) { point, press ->
                                    pointDragAfterLongPress!!.isPointInRange(
                                        density,
                                        point,
                                        press
                                    )
                                }?.let {
                                    if (minDistance < 0f || it.second < minDistance) {
                                        minDistance = it.second
                                        minDistanceDraggedPoint = DraggedPoint(it.first, line.tag, true)
                                    }
                                }
                            }
                            rightOffsetLines?.fastForEach { line ->
                                line.getClosestIndexDistance(offset) { point, press ->
                                    pointDragAfterLongPress!!.isPointInRange(
                                        density,
                                        point,
                                        press
                                    )
                                }?.let {
                                    if (minDistance < 0f || it.second < minDistance) {
                                        minDistance = it.second
                                        minDistanceDraggedPoint = DraggedPoint(it.first, line.tag, false)
                                    }
                                }
                            }
                            draggedPoint = minDistanceDraggedPoint
                        }, onDragEnd = {draggedPoint = null}, onDragCancel = {draggedPoint = null}) { change, offset ->
                            if (draggedPoint != null) {
                                val position = change.position / canvasZoom + canvasOffset
                                val point = position.toLineChartPoint(
                                    data,
                                    draggedPoint!!.isLeftAxis,
                                    size.width,
                                    size.height,
                                    minXValue,
                                    maxXValue,
                                    if (draggedPoint!!.isLeftAxis) leftAxisMinYValue else rightAxisMinYValue,
                                    if (draggedPoint!!.isLeftAxis) leftAxisMaxYValue else rightAxisMaxYValue
                                )
                                pointDragAfterLongPress!!.pointDragged(draggedPoint!!.lineTag, draggedPoint!!.index, point)
                            }
                        }
                    }),
                data,
                canvasZoom,
                canvasOffset,
                leftAxisMinYValue,
                leftAxisMaxYValue,
                rightAxisMinYValue,
                rightAxisMaxYValue,
                minXValue,
                maxXValue,
                leftAxisValues,
                rightAxisValues,
                bottomAxisValues,
                leftOffsetLines,
                rightOffsetLines,
                clickedPoint,
                clickedPointOffset,
                onEachPoint,
                pointClick
            )
            data.bottomAxis?.let { axis ->
                axis.valueView?.let {
                    AxisRow(Modifier.fillMaxWidth().zIndex(axesZIndex), canvasZoom, canvasOffset.x, data.xAxisLinesOffset, bottomAxisValues, minXValue, maxXValue) {
                        bottomAxisValues.fastForEach { value ->
                            it(value)
                        }
                    }
                }
            }
        }
        data.rightAxis?.let { axis ->
            axis.valueView?.let {
                AxisColumn(Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex), canvasZoom, canvasOffset.y, axis.yOffset, false, rightAxisValues, rightAxisMinYValue, rightAxisMaxYValue) {
                    rightAxisValues.fastForEach { value ->
                        it(value)
                    }
                }
            }
        }
    }
}

@Composable
private fun LineChartCanvas(
    modifier: Modifier,
    lineCanvasModifier: Modifier,
    data: LineChartData,
    canvasZoom: Float,
    canvasOffset: Offset,
    leftAxisMinYValue: Double,
    leftAxisMaxYValue: Double,
    rightAxisMinYValue: Double,
    rightAxisMaxYValue: Double,
    minXValue: Double,
    maxXValue: Double,
    leftAxisValues: List<Double>,
    rightAxisValues: List<Double>,
    bottomAxisValues: List<Double>,
    leftOffsetLines: List<LineChartData.OffsetLine>?,
    rightOffsetLines: List<LineChartData.OffsetLine>?,
    clickedPoint: ClickedPoint?,
    clickedPointOffset: Offset?,
    onEachPoint: (DrawScope.(canvasSize: Size, lineTag: Byte, index: Int, offset: Offset) -> Unit)?,
    pointClick: LineChartData.PointClick?,
) {
    BoxWithConstraints(modifier) {
        //Grid lines canvas
        Canvas(Modifier.fillMaxSize().zIndex(gridLinesZIndex).clipToBounds().graphicsLayer {
            translationX = -canvasOffset.x * canvasZoom
            translationY = -canvasOffset.y * canvasZoom
            scaleX = canvasZoom
            scaleY = canvasZoom
            transformOrigin = TransformOrigin(0f, 0f)
        }) {
            data.leftAxis?.let { axis ->
                //Draw left axis grid lines
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
                //Draw right axis grid lines
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
                //Draw bottom axis grid lines
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
        }
        //Dividers canvas
        Canvas(Modifier.fillMaxSize().zIndex(dividersZIndex)) {
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
        }
        //Lines canvas
        Box(Modifier
            .fillMaxSize()
            .zIndex(linesZIndex)
            .then(lineCanvasModifier)
            .graphicsLayer {
                translationX = -canvasOffset.x * canvasZoom
                translationY = -canvasOffset.y * canvasZoom
                scaleX = canvasZoom
                scaleY = canvasZoom
                transformOrigin = TransformOrigin(0f, 0f)
            }.drawWithContent {
                leftOffsetLines?.fastForEach { line ->
                    val path = Path().apply {
                        line.offsets.fastForEachIndexed { index, offset ->
                            if (index == 0) {
                                moveTo(offset.x, offset.y)
                            } else {
                                lineTo(offset.x, offset.y)
                            }
                        }
                    }
                    drawPath(
                        path,
                        line.customization.brush,
                        line.customization.alpha,
                        Stroke(line.customization.thickness.toPx(), line.customization.miter, line.customization.cap, line.customization.join, line.customization.pathEffect),
                        line.customization.colorFilter,
                        line.customization.blendMode
                    )
                    if (line.fillCustomization != null && line.offsets.isNotEmpty()) {
                        path.lineTo(line.offsets.last().x, size.height)
                        path.lineTo(line.offsets.first().x, size.height)
                        path.close()
                        drawPath(
                            path,
                            line.fillCustomization.brush,
                            line.fillCustomization.alpha,
                            Fill,
                            line.fillCustomization.colorFilter,
                            line.fillCustomization.blendMode
                        )
                    }
                    //Let the users config what they want on the point
                    onEachPoint?.let {
                        line.offsets.fastForEachIndexed { index, offset ->
                            onEachPoint(this, size, line.tag, index, offset)
                        }
                    }
                }
                //Draw the lines connecting the points
                rightOffsetLines?.fastForEach { line ->
                    val path = Path().apply {
                        line.offsets.fastForEachIndexed { index, offset ->
                            if (index == 0) {
                                moveTo(offset.x, offset.y)
                            } else {
                                lineTo(offset.x, offset.y)
                            }
                        }
                    }
                    drawPath(
                        path,
                        line.customization.brush,
                        line.customization.alpha,
                        Stroke(line.customization.thickness.toPx(), line.customization.miter, line.customization.cap, line.customization.join, line.customization.pathEffect),
                        line.customization.colorFilter,
                        line.customization.blendMode
                    )
                    if (line.fillCustomization != null && line.offsets.isNotEmpty()) {
                        path.lineTo(line.offsets.last().x, size.height)
                        path.lineTo(line.offsets.first().x, size.height)
                        path.close()
                        drawPath(
                            path,
                            line.fillCustomization.brush,
                            line.fillCustomization.alpha,
                            Fill,
                            line.fillCustomization.colorFilter,
                            line.fillCustomization.blendMode
                        )
                    }
                    //Let the users config what they want on the point
                    onEachPoint?.let {
                        line.offsets.fastForEachIndexed { index, offset ->
                            onEachPoint(this, size, line.tag, index, offset)
                        }
                    }
                }
            drawContent()
        }) {
            if (pointClick != null && clickedPoint != null && clickedPointOffset != null) {
                var viewSize by remember { mutableStateOf(IntSize.Zero) }
                Box(Modifier
                    .onSizeChanged { viewSize = it }
                    .offset {
                        pointClick.getViewOffset(this, this@BoxWithConstraints.constraints.maxWidth, this@BoxWithConstraints.constraints.maxHeight, viewSize, clickedPointOffset)
                    }
                ) {
                    pointClick.view(clickedPoint.lineTag, clickedPoint.index)
                }
            }
        }
    }
}

@Composable
private fun AxisRow(
    modifier: Modifier = Modifier,
    canvasScale: Float,
    canvasXOffset: Float,
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
        val xRange = maxXValue - minXValue
        val scaledCanvasWidth = canvasScale * (constraints.maxWidth - axisOffset.min - axisOffset.max)
        val scaledCanvasOffset = (axisOffset.min - canvasXOffset) * canvasScale
        val maxWidthTolerance = constraints.maxWidth + 0.01
        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, maxHeight) {
            // Place children in the parent layout
            placeables.fastForEachIndexed { index, placeable ->
                // Position item on the screen
                val xOffset = (((values[index] - minXValue) / xRange) * scaledCanvasWidth + scaledCanvasOffset)
                if (xOffset >= 0.0 && xOffset <= maxWidthTolerance) {
                    placeable.placeRelative(x = (xOffset - (placeable.width / 2.0)).fastRoundToInt(), y = 0)
                }
            }
        }
    }
}

@Composable
private fun AxisColumn(
    modifier: Modifier = Modifier,
    canvasScale: Float,
    canvasYOffset: Float,
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
        val yRange = maxYValue - minYValue
        val scaledCanvasHeight = canvasScale * (constraints.maxHeight - axisOffset.min - axisOffset.max)
        val scaledCanvasOffset = (axisOffset.min + canvasYOffset) * canvasScale
        val maxHeightTolerance = constraints.maxHeight + 0.01
        // Set the size of the layout as big as it can
        layout(maxWidth, constraints.maxHeight) {
            // Place children in the parent layout
            placeables.fastForEachIndexed { index, placeable ->
                // Position item on the screen
                val yOffset = (constraints.maxHeight * canvasScale - (((values[index] - minYValue) / yRange) * scaledCanvasHeight + scaledCanvasOffset))
                if (yOffset >= 0.0 && yOffset <= maxHeightTolerance) {
                    val y = (yOffset - (placeable.height / 2.0)).fastRoundToInt()
                    if (leftAxis) {
                        placeable.placeRelative(x = maxWidth - placeable.width, y = y)
                    } else {
                        placeable.placeRelative(x = 0, y = y)
                    }
                }
            }
        }
    }
}