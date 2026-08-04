package com.skellyapps.charts.example.view.line

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.common.model.Position
import com.skellyapps.charts.common.model.Zoom
import com.skellyapps.charts.example.roundToDecimals
import com.skellyapps.charts.line.model.LineChartData
import com.skellyapps.charts.line.view.LineChart
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private const val blueTag = 0
private const val redTag = 1

private val colors = listOf(Color.Blue, Color.Red)

private val blueLine = LineChartData.Line(
    (0..12).map { ChartValue(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableStateList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    blueTag,
    LineChartData.Line.Customization(colors[blueTag], join = StrokeJoin.Round)
)
private val blueLineMinY = blueLine.points.minOf {it.y}.value
private val blueLineMaxY = blueLine.points.maxOf {it.y}.value
private val leftAxis = LineChartData.YAxis(
    mutableListOf(blueLine),
    minValue = blueLineMinY,
    maxValue = blueLineMaxY,
    value = GridChartData.Axis.Value.Step(20.0),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value.roundToDecimals(1).toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}
private val redLine = LineChartData.Line(
    (0..15).map { ChartValue(it * 8.5, Random.nextDouble(0.0, 150.0)) }.toMutableStateList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    redTag,
    LineChartData.Line.Customization(colors[redTag], join = StrokeJoin.Round)
)
private val redLineMinY = redLine.points.minOf {it.y}.value
private val redLineMaxY = redLine.points.maxOf {it.y}.value
private val rightAxis = LineChartData.YAxis(
    mutableListOf(redLine),
    minValue = redLineMinY,
    maxValue = redLineMaxY,
    value = GridChartData.Axis.Value.Fixed(5),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.width(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}
private val minX = min(blueLine.points.first().x.value, redLine.points.first().x.value)
private val maxX = max(blueLine.points.last().x.value, redLine.points.last().x.value)
private val bottomAxis = LineChartData.XAxis(
    minValue = minX,
    maxValue = maxX,
    value = GridChartData.Axis.Value.Fixed(8),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}

private val lines = listOf(blueLine, redLine)

private val pointClick = LineChartData.PointClick(
    isPointInRange = { point, press ->
        (press - point).getDistance() / this.density <= 15.0
    },
    viewPosition = Position.Bottom,
    viewOffset = DpOffset(0.dp, 5.dp),
    viewStayInChartBounds = true,
    view = { lineTag, index ->
        val point = lines[lineTag].points[index]
        Column(Modifier.width(50.dp).background(colors[lineTag].copy(.5f), AbsoluteRoundedCornerShape(if (lineTag == blueTag) 25 else 0)), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(point.x.roundToDecimals(1).toString(), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(thickness = 4.dp)
            Text(point.y.roundToDecimals(1).toString(), style = MaterialTheme.typography.bodySmall)
        }
    }
)

//Combines lineTag + index into a single Int key (lineTag in the high 16 bits, index in the low 16).
//Avoids allocating a Pair/data class on every lookup during draw; fine as long as no line
//has more than 65535 points and tags stay under 65535(which make sense because this library is for small data sets)
private fun pointKey(lineTag: Int, index: Int): Int = (lineTag shl 16) or (index and 0xFFFF)

private fun measurePointLabel(textMeasurer: TextMeasurer, point: ChartValue): TextLayoutResult {
    val xValue = point.x.roundToDecimals(1)
    val yValue = point.y.roundToDecimals(1)
    return textMeasurer.measure("X:$xValue\nY:$yValue")
}

@Composable
fun FunctionalityLineChartExample() {
    val textMeasurer = rememberTextMeasurer(blueLine.points.size + redLine.points.size)
    val chartData by retain {
        mutableStateOf(
            LineChartData(
                leftAxis,
                rightAxis,
                bottomAxis,
            ),
            referentialEqualityPolicy()
        )
    }
    val zoom by retain { mutableStateOf<Zoom?>(Zoom(0.3f, 5f)) }
    val textLayouts = retain {
        mutableMapOf<Int, TextLayoutResult>().apply {
            lines.fastForEach { line ->
                line.points.forEachIndexed { index, point ->
                    this[pointKey(line.tag, index)] = measurePointLabel(textMeasurer, point)
                }
            }
        }
    }
    val pointDrag = retain {
        LineChartData.PointDrag(isPointInRange = { point, press ->
            (press - point).getDistance() / this.density <= 15.0
        }, pointDragged = { lineTag, index, newPosition ->
            if (newPosition.x.value < minX || newPosition.x.value > maxX) {
                return@PointDrag
            }
            if (lineTag == blueTag) {
                if (newPosition.y.value < blueLineMinY || newPosition.y.value > blueLineMaxY) {
                    return@PointDrag
                }
            } else {
                if (newPosition.y.value < redLineMinY || newPosition.y.value > redLineMaxY) {
                    return@PointDrag
                }
            }
            val line = lines[lineTag]
            if (index != 0) {
                val previousPoint = line.points[index-1]
                if (newPosition.x.value <= previousPoint.x.value) {
                    return@PointDrag
                }
            }
            if (index != line.points.size - 1) {
                val nextPoint = line.points[index+1]
                if (newPosition.x.value >= nextPoint.x.value) {
                    return@PointDrag
                }
            }
            //Points will update the UI so we must change the point label before setting the new position
            textLayouts[pointKey(lineTag, index)] = measurePointLabel(textMeasurer, newPosition)
            line.points[index] = newPosition
        })
    }
    Column(Modifier.fillMaxWidth()) {
        Text("You can zoom and click or drag points", Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(8.dp))
        LineChart(
            Modifier.fillMaxWidth().height(300.dp).padding(start = 8.dp),
            chartData,
            background = Brush.linearGradient(colors.map {it.copy(0.25f)}),
            zoom = zoom,
            pointClick = pointClick,
            pointDrag = pointDrag
        ) { drawHelper, lineTag, index, offset, _ ->
            val radius = 5.dp.toPx()
            if (offset.x < -radius || offset.x > size.width + radius ||
                offset.y < -radius || offset.y > size.height + radius) {
                return@LineChart
            }
            if (lineTag == blueTag) {
                drawCircle(
                    colors[lineTag],
                    radius,
                    offset
                )
            } else {
                drawHelper.drawSquare(
                    colors[lineTag],
                    offset,
                    radius * 2f
                )
            }
            val textLayout = textLayouts[pointKey(lineTag, index)]!!
            drawHelper.drawText(
                textLayout,
                offset,
                Position.Top,
                true
            )
        }
    }
}