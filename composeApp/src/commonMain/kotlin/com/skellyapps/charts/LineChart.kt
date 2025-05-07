package com.skellyapps.charts

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxOfOrNull
import androidx.compose.ui.util.fastMinByOrNull
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun LineChart(
    modifier: Modifier,
    data: LineChartData,
    onEachPoint: (DrawScope.(index: Int, offset: Offset, lineTag: Byte) -> Unit)? = null,
    drag: LineChartData.DragCallback? = null,
    dragAfterLongPress: LineChartData.DragCallback? = null
) {
    var canvasSize by remember { mutableStateOf(IntSize(0,0)) }
    val minXValue by derivedStateOf {
        min(data.leftAxis?.lines?.minOfOrNull { it.getMinX() ?: 0.0 } ?: 0.0, data.rightAxis?.lines?.minOfOrNull { it.getMinX() ?: 0.0 } ?: 0.0)
    }
    val minYValue by derivedStateOf {
        min(data.leftAxis?.lines?.minOfOrNull { it.getMinY() ?: 0.0 } ?: 0.0, data.rightAxis?.lines?.minOfOrNull { it.getMinY() ?: 0.0 } ?: 0.0)
    }
    val maxXValue by derivedStateOf {
        max(data.leftAxis?.lines?.fastMaxOfOrNull { it.getMaxX() ?: 1.0 } ?: 1.0, data.rightAxis?.lines?.fastMaxOfOrNull { it.getMaxX() ?: 1.0 } ?: 1.0)
    }
    val maxYValue by derivedStateOf {
        max(data.leftAxis?.lines?.fastMaxOfOrNull { it.getMaxY() ?: 1.0 } ?: 1.0, data.rightAxis?.lines?.fastMaxOfOrNull { it.getMaxY() ?: 1.0 } ?: 1.0)
    }
    val leftOffsetLines by derivedStateOf {
        data.leftAxis?.lines?.toOffsetLines(canvasSize, minXValue, maxXValue, data.xAxisLinesOffset, minYValue, maxYValue, data.leftAxis.yOffset)
    }
    val rightOffsetLines by derivedStateOf {
        data.rightAxis?.lines?.toOffsetLines(canvasSize, minXValue, maxXValue, data.xAxisLinesOffset, minYValue, maxYValue, data.rightAxis.yOffset)
    }
    var draggedPointDistance by remember { mutableStateOf<DraggedPointDistance?>(null) }
    Row(modifier) {
        VerticalDivider()
        Column(Modifier.fillMaxSize()) {
            LineChartCanvas(Modifier
                .fillMaxSize()
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
                                    it.getClosestPointIfInRange(offset, drag.isInRangePx, true)?.let {
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
                                    minYValue,
                                    maxYValue
                                )
                                drag.pointDragged(draggedPoint.index, draggedPoint.lineTag, point)
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
                                    it.getClosestPointIfInRange(offset, dragAfterLongPress.isInRangePx, true)?.let {
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
                                    minYValue,
                                    maxYValue
                                )
                                dragAfterLongPress.pointDragged(draggedPoint.index, draggedPoint.lineTag, point)
                            }
                        }
                    }
                },
                onEachPoint,
                leftOffsetLines,
                rightOffsetLines
            )
            HorizontalDivider()
        }
        VerticalDivider()
    }
}

@Composable
private fun LineChartCanvas(
    modifier: Modifier,
    onEachPoint: (DrawScope.(index: Int, offset: Offset, lineTag: Byte) -> Unit)?,
    leftOffsetLines: List<LineChartData.OffsetLine>?,
    rightOffsetLines: List<LineChartData.OffsetLine>?
) {
    Canvas(modifier) {
        // Draw the lines connecting the points
        leftOffsetLines?.fastForEach { line ->
            drawPoints(
                line.offsets,
                PointMode.Polygon,
                line.customization.brush,
                line.customization.strokeWidth,
                line.customization.cap,
                line.customization.pathEffect,
                line.customization.alpha,
                line.customization.colorFilter,
                line.customization.blendMode
            )
            //Let the user config what he wants on the point
            onEachPoint?.let {
                line.offsets.fastForEachIndexed { index, offset ->
                    onEachPoint(this, index, offset, line.tag)
                }
            }
        }
        //Draw the lines connecting the points
        rightOffsetLines?.fastForEach { line ->
            drawPoints(
                line.offsets,
                PointMode.Polygon,
                line.customization.brush,
                line.customization.strokeWidth,
                line.customization.cap,
                line.customization.pathEffect,
                line.customization.alpha,
                line.customization.colorFilter,
                line.customization.blendMode
            )
            //Let the user config what he wants on the point
            onEachPoint?.let {
                line.offsets.fastForEachIndexed { index, offset ->
                    onEachPoint(this, index, offset, line.tag)
                }
            }
        }
    }
}

data class LineChartData(
    val leftAxis: HorizontalAxis? = null,
    val rightAxis: HorizontalAxis? = null,
    val bottomAxis: Axis? = null,
    val xAxisLinesOffset: AxisOffset = AxisOffset(5, 5)
) {
    data class Line(
        val points: MutableList<Point>,
        val pointsOrder: PointsOrder,
        val tag: Byte,
        val customization: Customization
    ) {
        data class Point(
            val x: Double,
            val y: Double
        )
        sealed interface PointsOrder {
            fun getClosestIndexDistance(offsets: List<Offset>, touchPoint: Offset, isInRange: Offset.(Offset) -> Boolean): Pair<Int, Float>?
            data object Unordered: PointsOrder {
                override fun getClosestIndexDistance(
                    offsets: List<Offset>,
                    touchPoint: Offset,
                    isInRange: Offset.(Offset) -> Boolean,
                ): Pair<Int, Float>? {
                    var closestIndexDistance: Pair<Int, Float>? = null
                    offsets.fastForEachIndexed { index, offset ->
                        if (isInRange(offset, touchPoint)) {
                            val distance = (offset - touchPoint).getDistanceSquared()
                            if (closestIndexDistance == null || closestIndexDistance.second > distance) {
                                closestIndexDistance = Pair(index, distance)
                            }
                        }
                    }
                    return closestIndexDistance
                }
            }

            sealed interface Ordered: PointsOrder {
                fun getClosestByIndex(
                    index: Int,
                    offsets: List<Offset>,
                    touchPoint: Offset,
                    isInRange: Offset.(Offset) -> Boolean,
                ): Pair<Int, Float>? {
                    var closestIndexDistance: Pair<Int, Float>? = null
                    if (index < 0) {
                        var absIndex = abs(index + 1)
                        if (absIndex < offsets.size) {
                            if (isInRange(offsets[absIndex], touchPoint)) {
                                val distance = (offsets[absIndex] - touchPoint).getDistanceSquared()
                                if (closestIndexDistance == null || closestIndexDistance.second > distance) {
                                    closestIndexDistance = Pair(absIndex, distance)
                                }
                            }
                        }
                        if (absIndex != 0) {
                            absIndex -= 1
                            if (isInRange(offsets[absIndex], touchPoint)) {
                                val distance = (offsets[absIndex] - touchPoint).getDistanceSquared()
                                if (closestIndexDistance == null || closestIndexDistance.second > distance) {
                                    closestIndexDistance = Pair(absIndex, distance)
                                }
                            }
                        }
                    } else {
                        closestIndexDistance = Pair(index, 0f)
                    }
                    return closestIndexDistance
                }
                data object X: Ordered {
                    override fun getClosestIndexDistance(
                        offsets: List<Offset>,
                        touchPoint: Offset,
                        isInRange: Offset.(Offset) -> Boolean,
                    ): Pair<Int, Float>? {
                        val index = offsets.binarySearchBy(touchPoint.x) {
                            it.x
                        }
                        return getClosestByIndex(index, offsets, touchPoint, isInRange)
                    }
                }

                data object Y: Ordered {
                    override fun getClosestIndexDistance(
                        offsets: List<Offset>,
                        touchPoint: Offset,
                        isInRange: Offset.(Offset) -> Boolean,
                    ): Pair<Int, Float>? {
                        val index = offsets.binarySearchBy(touchPoint.y) {
                            it.y
                        }
                        return getClosestByIndex(index, offsets, touchPoint, isInRange)
                    }
                }
            }
        }
        class Customization {
            val brush: Brush
            val strokeWidth: Float
            val cap: StrokeCap
            val pathEffect: PathEffect?
            @FloatRange val alpha: Float
            val colorFilter: ColorFilter?
            val blendMode: BlendMode

            constructor(
                brush: Brush,
                strokeWidth: Float = 5f,
                cap: StrokeCap = StrokeCap.Round,
                pathEffect: PathEffect? = null,
                @FloatRange alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = BlendMode.Src,
            ) {
                this.brush = brush
                this.strokeWidth = strokeWidth
                this.cap = cap
                this.pathEffect = pathEffect
                this.alpha = alpha
                this.colorFilter = colorFilter
                this.blendMode = blendMode
            }

            constructor(
                color: Color,
                strokeWidth: Float = 5f,
                cap: StrokeCap = StrokeCap.Round,
                pathEffect: PathEffect? = null,
                @FloatRange alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = BlendMode.Src,
            ) {
                this.brush = Brush.linearGradient(listOf(color, color))
                this.strokeWidth = strokeWidth
                this.cap = cap
                this.pathEffect = pathEffect
                this.alpha = alpha
                this.colorFilter = colorFilter
                this.blendMode = blendMode
            }
        }
    }

    internal data class OffsetLine(
        val offsets: List<Offset>,
        val pointsOrder: Line.PointsOrder,
        val tag: Byte,
        val customization: Line.Customization
    ) {
        internal fun getClosestPointIfInRange(touchPoint: Offset, isInRange: Offset.(Offset) -> Boolean, isLeftAxis: Boolean): DraggedPointDistance? {
            if (offsets.isEmpty()) {
                return null
            }
            val closestIndexDistance = pointsOrder.getClosestIndexDistance(offsets, touchPoint, isInRange)
            if (closestIndexDistance != null) {
                return DraggedPointDistance(DraggedPoint(closestIndexDistance.first, tag), closestIndexDistance.second, isLeftAxis)
            }
            return null
        }
    }

    open class Axis
    class HorizontalAxis(
        val yOffset: AxisOffset = AxisOffset(5, 5),
        val lines: List<Line>
    ): Axis()

    data class AxisOffset(
        val min: Int,
        val max: Int
    )

    abstract class DragCallback(
        val isInRangePx: Offset.(Offset) -> Boolean
    ) {
        abstract fun pointDragged(index: Int, lineTag: Byte, newPosition: Line.Point)
    }
}

data class DraggedPoint(
    val index: Int,
    val lineTag: Byte
)

data class DraggedPointDistance(
    val draggedPoint: DraggedPoint,
    val distance: Float,
    val isLeftAxis: Boolean
)

internal inline fun LineChartData.Line.Point.toCanvasOffset(
    canvasSize: IntSize, minXValue: Double, maxXValue: Double, xOffset: LineChartData.AxisOffset,
    minYValue: Double, maxYValue: Double, yOffset: LineChartData.AxisOffset
) =
    Offset(
        (((x - minXValue) / (maxXValue - minXValue)) * (canvasSize.width - xOffset.min - xOffset.max) + xOffset.min).toFloat(),
        canvasSize.height - (((y - minYValue) / (maxYValue - minYValue)) * (canvasSize.height - yOffset.min - yOffset.max) + yOffset.min).toFloat()
    )

internal inline fun List<LineChartData.Line.Point>.toCanvasOffsets(
    canvasSize: IntSize, minXValue: Double, maxXValue: Double, xOffset: LineChartData.AxisOffset,
    minYValue: Double, maxYValue: Double, yOffset: LineChartData.AxisOffset
) = fastMap { it.toCanvasOffset(canvasSize, minXValue, maxXValue, xOffset, minYValue, maxYValue, yOffset) }

internal inline fun LineChartData.Line.toOffsetLine(
    canvasSize: IntSize, minXValue: Double, maxXValue: Double, xOffset: LineChartData.AxisOffset,
    minYValue: Double, maxYValue: Double, yOffset: LineChartData.AxisOffset
) =
    LineChartData.OffsetLine(
        points.toCanvasOffsets(canvasSize, minXValue, maxXValue, xOffset, minYValue, maxYValue, yOffset),
        pointsOrder,
        tag,
        customization
    )

internal inline fun List<LineChartData.Line>.toOffsetLines(
    canvasSize: IntSize, minXValue: Double, maxXValue: Double, xOffset: LineChartData.AxisOffset,
    minYValue: Double, maxYValue: Double, yOffset: LineChartData.AxisOffset
) = fastMap { it.toOffsetLine(canvasSize, minXValue, maxXValue, xOffset, minYValue, maxYValue, yOffset) }

internal inline fun Offset.toLineChartPoint(
    data: LineChartData,
    draggedPointDistance: DraggedPointDistance,
    canvasSize: IntSize,
    minXValue: Double,
    maxXValue: Double,
    minYValue: Double,
    maxYValue: Double
): LineChartData.Line.Point {
    val minXOffset = data.xAxisLinesOffset.min
    val maxXOffset = data.xAxisLinesOffset.max
    val minYOffset = if (draggedPointDistance.isLeftAxis) data.leftAxis!!.yOffset.min else data.rightAxis!!.yOffset.min
    val maxYOffset = if (draggedPointDistance.isLeftAxis) data.leftAxis!!.yOffset.max else data.rightAxis!!.yOffset.max
    val x = ((x - minXOffset) / (canvasSize.width - minXOffset - maxXOffset)) * (maxXValue - minXValue) + minXValue
    val y = (((canvasSize.height - y - minYOffset) / (canvasSize.height - minYOffset - maxYOffset)) * (maxYValue - minYValue) + minYValue)
    return LineChartData.Line.Point(x, y)
}

internal inline fun LineChartData.Line.getMinX(): Double? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.X -> points.first().x
        LineChartData.Line.PointsOrder.Ordered.Y,
        LineChartData.Line.PointsOrder.Unordered -> points.minOf { it.x }
    }
}

internal inline fun LineChartData.Line.getMaxX(): Double? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.X -> points.last().x
        LineChartData.Line.PointsOrder.Ordered.Y,
        LineChartData.Line.PointsOrder.Unordered -> points.maxOf { it.x }
    }
}

internal inline fun LineChartData.Line.getMinY(): Double? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.Y -> points.first().y
        LineChartData.Line.PointsOrder.Ordered.X,
        LineChartData.Line.PointsOrder.Unordered -> points.minOf { it.y }
    }
}

internal inline fun LineChartData.Line.getMaxY(): Double? {
    if (points.isEmpty()) {
        return null
    }
    return when (pointsOrder) {
        LineChartData.Line.PointsOrder.Ordered.Y -> points.last().y
        LineChartData.Line.PointsOrder.Ordered.X,
        LineChartData.Line.PointsOrder.Unordered -> points.maxOf { it.y }
    }
}