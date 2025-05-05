package com.skellyapps.charts

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxOfOrNull
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private const val blueTag = 0.toByte()
private const val yellowTag = 1.toByte()
private const val greenTag = 2.toByte()
private val colors = listOf(Color.Blue, Color.Yellow, Color.Green)
private val blueLine = LineChartData.Line(
    (1..75).map { LineChartData.Line.Point(it * Random.nextDouble(5.0, 15.0), Random.nextDouble(0.0, 100.0)) }.sortedBy { it.x },
    blueTag,
    LineChartData.Line.Customization(colors[blueTag.toInt()]))
private val yellowLine = LineChartData.Line(
    (1..66).map { LineChartData.Line.Point(it * Random.nextDouble(5.0, 15.0), Random.nextDouble(0.0, 100.0)) }.sortedBy { it.x },
    yellowTag,
    LineChartData.Line.Customization(colors[yellowTag.toInt()]))
private val greenLine = LineChartData.Line(
    (1..44).map { LineChartData.Line.Point(it * Random.nextDouble(5.0, 15.0), Random.nextDouble(0.0, 100.0)) }.sortedBy { it.x },
    greenTag,
    LineChartData.Line.Customization(colors[greenTag.toInt()])
)
private val leftAxis = LineChartData.HorizontalAxis(lines = listOf(blueLine, yellowLine, greenLine))

@Composable
@Preview
fun App() {
    MaterialTheme {
        LineChart(
            Modifier.fillMaxSize(),
            LineChartData(
                leftAxis = leftAxis
            )) { index, offset, lineTag ->
            drawCircle(
                colors[lineTag.toInt()],
                radius = 5f,
                offset
            )
        }
    }
}

@Composable
private fun LineChart(modifier: Modifier,
                      data: LineChartData,
                      onEachPoint: (DrawScope.(index: Int, offset: Offset, lineTag: Byte) -> Unit)? = null
) {
    var canvasSize by remember { mutableStateOf(IntSize(0,0)) }
    val minXValue by derivedStateOf {
        min(data.leftAxis?.lines?.minOfOrNull { it.points.minOfOrNull { it.x } ?: 0.0 } ?: 0.0, data.rightAxis?.lines?.minOfOrNull { it.points.minOfOrNull { it.x } ?: 0.0 } ?: 0.0)
    }
    val minYValue by derivedStateOf {
        min(data.leftAxis?.lines?.minOfOrNull { it.points.minOfOrNull { it.y } ?: 0.0 } ?: 0.0, data.rightAxis?.lines?.minOfOrNull { it.points.minOfOrNull { it.y } ?: 0.0 } ?: 0.0)
    }
    val maxXValue by derivedStateOf {
        max(data.leftAxis?.lines?.fastMaxOfOrNull { it.points.fastMaxOfOrNull { it.x } ?: 1.0 } ?: 1.0, data.rightAxis?.lines?.fastMaxOfOrNull { it.points.fastMaxOfOrNull { it.x } ?: 1.0 } ?: 1.0)
    }
    val maxYValue by derivedStateOf {
        max(data.leftAxis?.lines?.fastMaxOfOrNull { it.points.fastMaxOfOrNull { it.y } ?: 1.0 } ?: 1.0, data.rightAxis?.lines?.fastMaxOfOrNull { it.points.fastMaxOfOrNull { it.y } ?: 1.0 } ?: 1.0)
    }
    val leftOffsetLines by derivedStateOf {
        val minXOffset = data.xAxisLinesOffset.min
        val maxXOffset = data.xAxisLinesOffset.max
        val minYOffset = data.leftAxis?.yOffset?.min
        val maxYOffset = data.leftAxis?.yOffset?.max
        data.leftAxis?.lines?.fastMap {
            LineChartData.OffsetLine(it.points.fastMap {
                Offset(
                    (((it.x - minXValue) / (maxXValue - minXValue)) * (canvasSize.width - minXOffset - maxXOffset) + minXOffset).toFloat(),
                    canvasSize.height - (((it.y - minYValue) / (maxYValue - minYValue)) * (canvasSize.height - minYOffset!! - maxYOffset!!) + minYOffset).toFloat()
                )
            }, it.tag, it.customization)
        }
    }
    val rightOffsetLines by derivedStateOf {
        val minXOffset = data.xAxisLinesOffset.min
        val maxXOffset = data.xAxisLinesOffset.max
        val minYOffset = data.rightAxis?.yOffset?.min
        val maxYOffset = data.rightAxis?.yOffset?.max
        data.rightAxis?.lines?.fastMap {
            LineChartData.OffsetLine(it.points.fastMap {
                Offset(
                    (((it.x - minXValue) / (maxXValue - minXValue)) * (canvasSize.width - minXOffset - maxXOffset) + minXOffset).toFloat(),
                    canvasSize.height - (((it.y - minYValue) / (maxYValue - minYValue)) * (canvasSize.height - minYOffset!! - maxYOffset!!) + minYOffset).toFloat()
                )
            }, it.tag, it.customization)
        }
    }
    var pointDragIndex by remember { mutableStateOf(-1) }

    Canvas(modifier
        .onSizeChanged {
            canvasSize = it
        }
        .pointerInput(Unit) {
//            detectDragGesturesAfterLongPress(onDragStart = { offset ->
//                pointDragIndex = points.indexOfFirst {
//                    it.closeTo(offset)
//                }
//            }, onDragEnd = {pointDragIndex = -1}, onDragCancel = {pointDragIndex = -1}) { change, offset ->
//                if (pointDragIndex == -1) return@detectDragGesturesAfterLongPress
//                val point = points[pointDragIndex]
//                val newPoint = Offset(point.x + offset.x, point.y + offset.y)
//                val temp = points.apply { set(pointDragIndex, newPoint) }.toMutableList()
//                points = temp
//            }
        }) {
        // Draw the line connecting the points
        if (leftOffsetLines != null) {
            leftOffsetLines!!.fastForEach { line ->
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
                onEachPoint?.let {
                    line.offsets.fastForEachIndexed { index, offset ->
                        onEachPoint(this, index, offset, line.tag)
                    }
                }
            }
        }
        if (rightOffsetLines != null) {
            rightOffsetLines!!.fastForEach { line ->
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
                onEachPoint?.let {
                    line.offsets.fastForEachIndexed { index, offset ->
                        onEachPoint(this, index, offset, line.tag)
                    }
                }
            }
        }
    }
}

private fun Offset.closeTo(offset: Offset): Boolean {
    return abs(x - offset.x) <= 10 && abs(y - offset.y) <= 10
}

data class LineChartData(
    val leftAxis: HorizontalAxis? = null,
    val rightAxis: HorizontalAxis? = null,
    val bottomAxis: Axis? = null,
    val xAxisLinesOffset: AxisOffset = AxisOffset(5, 5)
) {
    data class Line(
        val points: List<Point>,
        val tag: Byte,
        val customization: Customization
    ) {
        data class Point(
            val x: Double,
            val y: Double
        )
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
        val tag: Byte,
        val customization: Line.Customization
    )

    open class Axis
    class HorizontalAxis(
        val yOffset: AxisOffset = AxisOffset(5, 5),
        val lines: List<Line>
    ): Axis()

    data class AxisOffset(
        val min: Int,
        val max: Int
    )
}