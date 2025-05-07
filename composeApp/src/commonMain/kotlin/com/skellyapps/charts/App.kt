package com.skellyapps.charts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.random.Random

private const val blueTag = 0.toByte()
private const val yellowTag = 1.toByte()
private const val greenTag = 2.toByte()
private const val redTag = 3.toByte()
private val colors = listOf(Color.Blue, Color.Yellow, Color.Green, Color.Red)
private val blueLine = LineChartData.Line(
    (1..75).map { LineChartData.Line.Point(it * Random.nextDouble(5.0, 15.0), Random.nextDouble(0.0, 100.0)) }.sortedBy { it.x }.toMutableStateList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    blueTag,
    LineChartData.Line.Customization(colors[blueTag.toInt()]))
private val yellowLine = LineChartData.Line(
    (1..66).map { LineChartData.Line.Point(it * Random.nextDouble(5.0, 15.0), Random.nextDouble(0.0, 100.0)) }.sortedBy { it.x }.toMutableStateList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    yellowTag,
    LineChartData.Line.Customization(colors[yellowTag.toInt()]))
private val greenLine = LineChartData.Line(
    (1..44).map { LineChartData.Line.Point(it * Random.nextDouble(5.0, 15.0), Random.nextDouble(0.0, 100.0)) }.sortedBy { it.x }.toMutableStateList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    greenTag,
    LineChartData.Line.Customization(colors[greenTag.toInt()])
)
private val leftAxis = LineChartData.HorizontalAxis(lines = listOf(blueLine, yellowLine, greenLine))
private val redLine = LineChartData.Line(
    (1..100).map { LineChartData.Line.Point(it * Random.nextDouble(5.0, 15.0), Random.nextDouble(0.0, 100.0)) }.sortedBy { it.x }.toMutableStateList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    redTag,
    LineChartData.Line.Customization(colors[redTag.toInt()])
)
private val rightAxis = LineChartData.HorizontalAxis(lines = listOf(redLine))
private val lines = listOf(blueLine, yellowLine, greenLine, redLine)

private val dragCallback = object: LineChartData.DragCallback(isInRangePx = { offset ->
    abs(x - offset.x) <= 10 && abs(y - offset.y) <= 10
}) {
    override fun pointDragged(index: Int, lineTag: Byte, newPosition: LineChartData.Line.Point) {
        lines[lineTag.toInt()].points[index] = newPosition
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        LineChart(
            Modifier.size(350.dp, 200.dp),
            LineChartData(
                leftAxis = leftAxis,
                rightAxis = rightAxis
            ),
            { index, offset, lineTag ->
                drawCircle(
                    colors[lineTag.toInt()],
                    radius = 5f,
                    offset
                )
            },
            dragCallback
        )
    }
}