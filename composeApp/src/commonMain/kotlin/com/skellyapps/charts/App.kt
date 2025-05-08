package com.skellyapps.charts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.skellyapps.charts.line.LineChart
import com.skellyapps.charts.line.model.LineChartData
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

private const val blueTag = 0.toByte()
private const val yellowTag = 1.toByte()
private const val greenTag = 2.toByte()
private const val redTag = 3.toByte()
private val colors = listOf(Color.Blue, Color.Yellow, Color.Green, Color.Red)
private val blueLine = LineChartData.Line(
    (1..12).map { LineChartData.Line.Point(it * Random.nextInt(5, 15).toDouble(), Random.nextDouble(0.0, 300.0)) }.sortedBy { it.x }.toMutableStateList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    blueTag,
    LineChartData.Line.Customization(colors[blueTag.toInt()]))
private val yellowLine = LineChartData.Line(
    (1..8).map { LineChartData.Line.Point(it * Random.nextInt(5, 15).toDouble(), Random.nextDouble(0.0, 100.0)) }.sortedBy { it.x }.toMutableStateList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    yellowTag,
    LineChartData.Line.Customization(colors[yellowTag.toInt()]))
private val greenLine = LineChartData.Line(
    (1..5).map { LineChartData.Line.Point(it * Random.nextInt(5, 20).toDouble(), Random.nextDouble(0.0, 100.0)) }.sortedBy { it.x }.toMutableStateList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    greenTag,
    LineChartData.Line.Customization(colors[greenTag.toInt()])
)
private val leftAxis = LineChartData.Axis.YAxis(lines = listOf(blueLine, yellowLine, greenLine), step = 20.0, dividerCustomization = LineChartData.Axis.DividerCustomization(color = Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value.roundToDecimals(1).toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}
private val redLine = LineChartData.Line(
    (1..17).map { LineChartData.Line.Point(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableStateList(),
    LineChartData.Line.PointsOrder.Unordered,
    redTag,
    LineChartData.Line.Customization(colors[redTag.toInt()])
)
private val rightAxis = LineChartData.Axis.YAxis(lines = listOf(redLine), step = 20.0) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.width(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}
private val bottomAxis = LineChartData.Axis.XAxis(10.0, LineChartData.Axis.DividerCustomization(color = Color.Black)) { value ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}

private val lines = listOf(blueLine, yellowLine, greenLine, redLine)
private val dragCallback = LineChartData.DragCallback(isInRangePx = { offset ->
    abs(x - offset.x) <= 10 && abs(y - offset.y) <= 10
}, pointDragged = { index, lineTag, newPosition ->
    lines[lineTag.toInt()].points[index] = lines[lineTag.toInt()].points[index].copy(y = newPosition.y)
})

@Composable
@Preview
fun App() {
    MaterialTheme {
        val textMeasurer = rememberTextMeasurer()
        LineChart(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            LineChartData(
                leftAxis = leftAxis,
                rightAxis = rightAxis,
                bottomAxis = bottomAxis
            ),
            { index, offset, lineTag ->
                drawCircle(
                    colors[lineTag.toInt()],
                    radius = 5f,
                    offset
                )
                val point = lines[lineTag.toInt()].points[index]
                val xValue = point.x.roundToDecimals(1)
                val yValue = point.y.roundToDecimals(1)
                val text = "$xValue|$yValue"
                val layout = textMeasurer.measure(text)
                val offset = Offset(max(offset.x - layout.size.width / 2, 0f), max(offset.y - layout.size.height, 0f))
                drawText(
                    layout,
                    topLeft = offset
                )
            },
            dragCallback
        )
    }
}

private fun Double.roundToDecimals(decimals: Int): Double {
    val divider = 10.0.pow(decimals)
    return (this * divider).toInt() / divider
}