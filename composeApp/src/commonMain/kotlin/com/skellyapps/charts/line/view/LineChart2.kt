package com.skellyapps.charts.line.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.skellyapps.charts.common.extension.detectTransformGestures
import com.skellyapps.charts.common.extension.`if`
import com.skellyapps.charts.common.model.ChartPixel
import com.skellyapps.charts.common.model.ChartPixelCoordinate
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.Zoom
import com.skellyapps.charts.line.extension.getClosestIndexDistance
import com.skellyapps.charts.line.extension.getMaxX
import com.skellyapps.charts.line.extension.getMaxY
import com.skellyapps.charts.line.extension.getMinX
import com.skellyapps.charts.line.extension.getMinY
import com.skellyapps.charts.line.extension.toOffsetLines
import com.skellyapps.charts.line.model.ClickedPoint
import com.skellyapps.charts.line.model.DraggedPoint
import com.skellyapps.charts.line.model.LineChartData

private val gridLinesZIndex = 10f
private val dividersZIndex = 20f
private val linesZIndex = 30f
private val axesZIndex = -1f

@Composable
fun LineChart2(
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
    var canvasZoom by remember { mutableStateOf(Offset(1f,1f)) }
    var canvasOffset by remember { mutableStateOf(Offset.Zero) }
    val leftAxisYOffset by remember(data.leftAxis?.yOffset) {
        derivedStateOf {
            with(density) {
                data.leftAxis?.yOffset?.let { Offset(it.x.toPx(), it.y.toPx()) }
            }
        }
    }
    val rightAxisYOffset by remember(data.rightAxis?.yOffset) {
        derivedStateOf {
            with(density) {
                data.rightAxis?.yOffset?.let { Offset(it.x.toPx(), it.y.toPx()) }
            }
        }
    }
    val xAxisOffset by remember(data.xAxisOffset) {
        derivedStateOf {
            with(density) {
                Offset(data.xAxisOffset.x.toPx(), data.xAxisOffset.y.toPx())
            }
        }
    }
    val minXValue by remember(data.bottomAxis?.minValue, data.leftAxis?.lines, data.rightAxis?.lines) {
        derivedStateOf {
            if (data.bottomAxis?.minValue != null) {
                ChartValueCoordinate(data.bottomAxis.minValue)
            } else {
                val minXLeftAxis = data.leftAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.minOfOrNull { it.getMinX()!! }
                val minXRightAxis = data.rightAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.minOfOrNull { it.getMinX()!! }
                if (minXLeftAxis == null) {
                    minXRightAxis ?: ChartValueCoordinate(0.0)
                } else if (minXRightAxis == null) {
                    minXLeftAxis
                } else {
                    minOf(minXLeftAxis, minXRightAxis)
                }
            }
        }
    }
    val maxXValue by remember(data.bottomAxis?.maxValue, data.leftAxis?.lines, data.rightAxis?.lines) {
        derivedStateOf {
            if (data.bottomAxis?.maxValue != null) {
                ChartValueCoordinate(data.bottomAxis.maxValue)
            } else {
                val maxXLeftAxis = data.leftAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.maxOfOrNull { it.getMaxX()!! }
                val maxXRightAxis = data.rightAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.maxOfOrNull { it.getMaxX()!! }
                val max = if (maxXLeftAxis == null) {
                    maxXRightAxis ?: ChartValueCoordinate(1.0)
                } else if (maxXRightAxis == null) {
                    maxXLeftAxis
                } else {
                    maxOf(maxXLeftAxis, maxXRightAxis)
                }
                if (max == minXValue) {
                    max + ChartValueCoordinate(1.0)
                } else {
                    max
                }
            }
        }
    }
    val xAxisViewPort by remember(xAxisOffset, minXValue, maxXValue) {
        derivedStateOf {
            val x = ChartPixelCoordinate(canvasOffset.x).toChartValueCoordinate(canvasSize.width, xAxisOffset, minXValue, maxXValue, false)
            val maxX = ChartPixelCoordinate(canvasOffset.x + canvasSize.width.toFloat() / canvasZoom.x).toChartValueCoordinate(canvasSize.width, xAxisOffset, minXValue, maxXValue, false)
            ChartValue(x, maxX)
        }
    }
    val leftAxisMinYValue by remember(data.leftAxis?.minValue, data.leftAxis?.lines) {
        derivedStateOf {
            ChartValueCoordinate(
                if (data.leftAxis?.minValue != null) {
                    data.leftAxis.minValue
                } else {
                    data.leftAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.minOfOrNull { it.getMinY()!! }?.value ?: 0.0
                }
            )
        }
    }
    val leftAxisMaxYValue by remember(data.leftAxis?.maxValue, data.leftAxis?.lines) {
        derivedStateOf {
            ChartValueCoordinate(
                if (data.leftAxis?.maxValue != null) {
                    data.leftAxis.maxValue
                } else {
                    val max = data.leftAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.maxOfOrNull { it.getMaxY()!! }?.value ?: 1.0
                    if (max == leftAxisMinYValue.value) {
                        max + 1
                    } else {
                        max
                    }
                }
            )
        }
    }
    val leftAxisYViewPort by remember(leftAxisYOffset, leftAxisMinYValue, leftAxisMaxYValue) {
        derivedStateOf {
            if (leftAxisYOffset == null) {
                ChartValue(1.0, 1.0)
            } else {
                val y = ChartPixelCoordinate(canvasOffset.y + canvasSize.height.toFloat() / canvasZoom.y).toChartValueCoordinate(canvasSize.height, leftAxisYOffset!!, leftAxisMinYValue, leftAxisMaxYValue, true)
                val maxY = ChartPixelCoordinate(canvasOffset.y).toChartValueCoordinate(canvasSize.height, leftAxisYOffset!!, leftAxisMinYValue, leftAxisMaxYValue, true)
                ChartValue(y, maxY)
            }
        }
    }
    val rightAxisMinYValue by remember(data.rightAxis?.minValue, data.rightAxis?.lines) {
        derivedStateOf {
            ChartValueCoordinate(
                if (data.rightAxis?.minValue != null) {
                    data.rightAxis.minValue
                } else {
                    data.rightAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.minOfOrNull { it.getMinY()!! }?.value ?: 0.0
                }
            )
        }
    }
    val rightAxisMaxYValue by remember(data.rightAxis?.maxValue, data.rightAxis?.lines) {
        derivedStateOf {
            ChartValueCoordinate(
                if (data.rightAxis?.maxValue != null) {
                    data.rightAxis.maxValue
                } else {
                    val max = data.rightAxis?.lines?.fastFilter { it.points.isNotEmpty() }?.maxOfOrNull { it.getMaxY()!! }?.value ?: 1.0
                    if (max == leftAxisMinYValue.value) {
                        max + 1
                    } else {
                        max
                    }
                }
            )
        }
    }
    val rightAxisYViewPort by remember(rightAxisYOffset, rightAxisMinYValue, rightAxisMaxYValue) {
        derivedStateOf {
            if (rightAxisYOffset == null) {
                ChartValue(1.0, 1.0)
            } else {
                val y = ChartPixelCoordinate(canvasOffset.y + canvasSize.height.toFloat() / canvasZoom.y).toChartValueCoordinate(canvasSize.height, rightAxisYOffset!!, rightAxisMinYValue, rightAxisMaxYValue, true)
                val maxY = ChartPixelCoordinate(canvasOffset.y).toChartValueCoordinate(canvasSize.height, rightAxisYOffset!!, rightAxisMinYValue, rightAxisMaxYValue, true)
                ChartValue(y, maxY)
            }
        }
    }
    val leftOffsetLines by remember(data.leftAxis?.lines, xAxisViewPort, leftAxisYViewPort) {
        derivedStateOf {
            data.leftAxis?.lines?.toOffsetLines(canvasSize, xAxisViewPort.x, xAxisViewPort.y, leftAxisYViewPort.x, leftAxisYViewPort.y)
        }
    }
    val rightOffsetLines by remember(data.rightAxis?.lines, xAxisViewPort, rightAxisYViewPort) {
        derivedStateOf {
            data.rightAxis?.lines?.toOffsetLines(canvasSize, xAxisViewPort.x, xAxisViewPort.y, rightAxisYViewPort.x, rightAxisYViewPort.y)
        }
    }
    val leftAxisValues by remember(leftAxisMinYValue, leftAxisMaxYValue, data.leftAxis?.value) {
        derivedStateOf {
            data.leftAxis?.value?.getValues(leftAxisMinYValue, leftAxisMaxYValue) ?: listOf()
        }
    }
    val rightAxisValues by remember(rightAxisMinYValue, rightAxisMaxYValue, data.rightAxis?.value) {
        derivedStateOf {
            data.rightAxis?.value?.getValues(rightAxisMinYValue, rightAxisMaxYValue) ?: listOf()
        }
    }
    val bottomAxisValues: List<ChartValueCoordinate> by remember(minXValue, maxXValue, data.bottomAxis?.value) {
        derivedStateOf {
            data.bottomAxis?.value?.getValues(minXValue, maxXValue) ?: listOf()
        }
    }
    val clipToBounds by remember {
        derivedStateOf {
            canvasZoom.x != 1f || canvasZoom.y != 1f
        }
    }
    var clickedPoint by remember { mutableStateOf<ClickedPoint?>(null) }
    val clickedPointOffset by remember(xAxisViewPort, leftAxisYViewPort, rightAxisYViewPort) {
        derivedStateOf {
            clickedPoint?.let { p -> if (p.isLeftAxis) leftOffsetLines?.fastFirstOrNull { it.tag == p.lineTag }?.offsets?.getOrNull(p.index) else rightOffsetLines?.fastFirstOrNull { it.tag == p.lineTag }?.offsets?.getOrNull(p.index) }
        }
    }
    var draggedPoint by remember { mutableStateOf<DraggedPoint?>(null) }

    Row(modifier) {
        data.leftAxis?.let { axis ->
            axis.valueView?.let {
                AxisColumn2(Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex), true, leftAxisValues, leftAxisYViewPort.x, leftAxisYViewPort.y) {
                    leftAxisValues.fastForEach { value ->
                        it(value.value)
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
                    .`if`(zoom != null, Modifier.pointerInput(zoom) {
                        detectTransformGestures(
                            onGesture = { centroid,
                                          pan,
                                          gestureZoom,
                                          orientation,
                                          type,
                                          changes ->
                                if (type == PointerEventType.Scroll || changes.size > 1) {
                                    val newScale = if (type == PointerEventType.Scroll) {
                                        val addition = if (gestureZoom < 0f) zoom!!.scrollJump else -zoom!!.scrollJump
                                        Offset((canvasZoom.x + addition).coerceIn(1f, zoom.max), (canvasZoom.y + addition).coerceIn(1f, zoom.max))
                                    } else {
                                        var x = canvasZoom.x
                                        if (orientation != Orientation.Vertical) {
                                            x = (x * gestureZoom).coerceIn(1f, zoom!!.max)
                                        }
                                        var y = canvasZoom.y
                                        if (orientation != Orientation.Horizontal) {
                                            y = (y * gestureZoom).coerceIn(1f, zoom!!.max)
                                        }
                                        Offset(x, y)
                                    }
                                    val newX = ((canvasOffset.x + centroid.x / canvasZoom.x) - (centroid.x / newScale.x + pan.x / canvasZoom.x)).coerceIn(0f, size.width - size.width / newScale.x)
                                    val newY = ((canvasOffset.y + centroid.y / canvasZoom.y) - (centroid.y / newScale.y + pan.y / canvasZoom.y)).coerceIn(0f, size.height - size.height / newScale.y)
                                    canvasOffset = Offset(newX, newY)
                                    canvasZoom = newScale
                                    changes.forEach { it.consume() }
                                }
                            }
                        )
                    })
                    .`if`(pointClick != null, Modifier.pointerInput(xAxisViewPort, leftAxisYViewPort, rightAxisYViewPort) {
                        detectTapGestures(
                            onTap = { offset ->
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
                    .pointerInput(data.xAxisOffset, data.leftAxis?.yOffset, data.leftAxis?.minValue, data.leftAxis?.maxValue,data.rightAxis?.yOffset, data.rightAxis?.minValue, data.rightAxis?.maxValue, data.bottomAxis?.minValue, data.bottomAxis?.maxValue) {
                        detectDragGestures(onDragStart = { offset ->
                            if (pointDrag == null) {
                                return@detectDragGestures
                            }
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
                                val position = change.position
                                val point = ChartPixel(position).toChartValue(
                                    size,
                                    xAxisViewPort.x,
                                    xAxisViewPort.y,
                                    if (draggedPoint!!.isLeftAxis) leftAxisYViewPort.x else rightAxisYViewPort.x,
                                    if (draggedPoint!!.isLeftAxis) leftAxisYViewPort.y else rightAxisYViewPort.y
                                )
                                pointDrag.pointDragged(draggedPoint!!.lineTag, draggedPoint!!.index, point)
                            } else {
                                canvasOffset = canvasZoom.run {
                                    Offset((canvasOffset - offset / x).x.coerceIn(0f, size.width - size.width / canvasZoom.x), (canvasOffset - offset / y).y.coerceIn(0f, size.height - size.height / canvasZoom.y))
                                }
                            }
                        }
                    }
                    .`if`(pointDragAfterLongPress != null, Modifier.pointerInput(data.xAxisOffset, data.leftAxis?.minValue, data.leftAxis?.maxValue, data.rightAxis?.minValue, data.rightAxis?.maxValue, data.bottomAxis?.minValue, data.bottomAxis?.maxValue) {
                        detectDragGesturesAfterLongPress(onDragStart = { offset ->
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
                                val position = change.position
                                val point = ChartPixel(position).toChartValue(
                                    size,
                                    xAxisViewPort.x,
                                    xAxisViewPort.y,
                                    if (draggedPoint!!.isLeftAxis) leftAxisYViewPort.x else rightAxisYViewPort.x,
                                    if (draggedPoint!!.isLeftAxis) leftAxisYViewPort.y else rightAxisYViewPort.y
                                )
                                pointDragAfterLongPress!!.pointDragged(draggedPoint!!.lineTag, draggedPoint!!.index, point)
                            }
                        }
                    }),
                data,
                leftAxisYViewPort.x,
                leftAxisYViewPort.y,
                rightAxisYViewPort.x,
                rightAxisYViewPort.y,
                xAxisViewPort.x,
                xAxisViewPort.y,
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
                    AxisRow2(Modifier.fillMaxWidth().zIndex(axesZIndex), bottomAxisValues, xAxisViewPort.x, xAxisViewPort.y) {
                        bottomAxisValues.fastForEach { value ->
                            it(value.value)
                        }
                    }
                }
            }
        }
        data.rightAxis?.let { axis ->
            axis.valueView?.let {
                AxisColumn2(Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex), false, rightAxisValues, rightAxisYViewPort.x, rightAxisYViewPort.y) {
                    rightAxisValues.fastForEach { value ->
                        it(value.value)
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
    leftAxisMinYValue: ChartValueCoordinate,
    leftAxisMaxYValue: ChartValueCoordinate,
    rightAxisMinYValue: ChartValueCoordinate,
    rightAxisMaxYValue: ChartValueCoordinate,
    minXValue: ChartValueCoordinate,
    maxXValue: ChartValueCoordinate,
    leftAxisValues: List<ChartValueCoordinate>,
    rightAxisValues: List<ChartValueCoordinate>,
    bottomAxisValues: List<ChartValueCoordinate>,
    leftOffsetLines: List<LineChartData.OffsetLine>?,
    rightOffsetLines: List<LineChartData.OffsetLine>?,
    clickedPoint: ClickedPoint?,
    clickedPointOffset: ChartPixel?,
    onEachPoint: (DrawScope.(canvasSize: Size, lineTag: Byte, index: Int, offset: Offset) -> Unit)?,
    pointClick: LineChartData.PointClick?,
) {
    BoxWithConstraints(modifier) {
        //Grid lines canvas
        Canvas(Modifier.fillMaxSize().zIndex(gridLinesZIndex).clipToBounds()) {
            data.leftAxis?.let { axis ->
                //Draw left axis grid lines
                axis.gridLines?.let {
                    val thickness = it.customization.thickness.toPx()
                    val startIndex = if (it.showFirstLine) 0 else 1
                    val endIndex = if (it.showLastLine) leftAxisValues.size - 1 else leftAxisValues.size - 2
                    for (i in startIndex..endIndex) {
                        val yOffset = leftAxisValues[i].toChartPixelCoordinate(size.height, leftAxisMinYValue, leftAxisMaxYValue, true).value
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
                        val yOffset = leftAxisValues[i].toChartPixelCoordinate(size.height, rightAxisMinYValue, rightAxisMaxYValue, true).value
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
                        val xOffset = bottomAxisValues[i].toChartPixelCoordinate(size.width, minXValue, maxXValue, false).value
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
            .drawWithContent {
                leftOffsetLines?.fastForEach { line ->
                    val path = Path().apply {
                        line.offsets.fastForEachIndexed { index, chartPixel ->
                            if (index == 0) {
                                moveTo(chartPixel.x.value, chartPixel.y.value)
                            } else {
                                lineTo(chartPixel.x.value, chartPixel.y.value)
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
                        path.lineTo(line.offsets.last().x.value, size.height)
                        path.lineTo(line.offsets.first().x.value, size.height)
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
                        line.offsets.fastForEachIndexed { index, chartPixel ->
                            onEachPoint(this, size, line.tag, index, chartPixel.offset)
                        }
                    }
                }
                //Draw the lines connecting the points
                rightOffsetLines?.fastForEach { line ->
                    val path = Path().apply {
                        line.offsets.fastForEachIndexed { index, chartPixel ->
                            if (index == 0) {
                                moveTo(chartPixel.x.value, chartPixel.y.value)
                            } else {
                                lineTo(chartPixel.x.value, chartPixel.y.value)
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
                        path.lineTo(line.offsets.last().x.value, size.height)
                        path.lineTo(line.offsets.first().x.value, size.height)
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
                        line.offsets.fastForEachIndexed { index, chartPixel ->
                            onEachPoint(this, size, line.tag, index, chartPixel.offset)
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
private fun AxisRow2(
    modifier: Modifier,
    values: List<ChartValueCoordinate>,
    minXValue: ChartValueCoordinate,
    maxXValue: ChartValueCoordinate,
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
        val maxWidthTolerance = constraints.maxWidth + 0.01
        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, maxHeight) {
            // Place children in the parent layout
            placeables.fastForEachIndexed { index, placeable ->
                // Position item on the screen
                val xOffset = values[index].toChartPixelCoordinate(constraints.maxWidth, minXValue, maxXValue, false).value
                if (xOffset >= 0.0 && xOffset <= maxWidthTolerance) {
                    placeable.placeRelative(x = (xOffset - (placeable.width / 2.0)).fastRoundToInt(), y = 0)
                }
            }
        }
    }
}

@Composable
private fun AxisColumn2(
    modifier: Modifier,
    leftAxis: Boolean,
    values: List<ChartValueCoordinate>,
    minYValue: ChartValueCoordinate,
    maxYValue: ChartValueCoordinate,
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
        val maxHeightTolerance = constraints.maxHeight + 0.01
        // Set the size of the layout as big as it can
        layout(maxWidth, constraints.maxHeight) {
            // Place children in the parent layout
            placeables.fastForEachIndexed { index, placeable ->
                // Position item on the screen
                val yOffset = values[index].toChartPixelCoordinate(constraints.maxHeight, minYValue, maxYValue, true).value
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