package com.skellyapps.charts.example.view.line

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.unit.dp
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.example.roundToDecimals
import com.skellyapps.charts.line.model.LineChartData
import com.skellyapps.charts.line.view.LineChart
import kotlin.random.Random

private val blueLine = LineChartData.Line(
    (0..12).map { ChartValue(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    0,
    LineChartData.Line.Customization(Color.Blue, join = StrokeJoin.Round)
)
private val leftAxis = LineChartData.YAxis(
    lines = mutableStateListOf(blueLine),
    value = GridChartData.Axis.Value.Step(20.0),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value.roundToDecimals(1).toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}
private val redLine = LineChartData.Line(
    (0..15).map { ChartValue(it * 8.5, Random.nextDouble(0.0, 150.0)) }.toMutableList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    1,
    LineChartData.Line.Customization(Color.Red, join = StrokeJoin.Round)
)
private val rightAxis = LineChartData.YAxis(
    lines = mutableStateListOf(redLine),
    value = GridChartData.Axis.Value.Fixed(5),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.width(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}
private val bottomAxis = LineChartData.XAxis(
    value = GridChartData.Axis.Value.Fixed(8),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}

private data class ColorData(
    val color: Color,
    var isLeft: Boolean? = null,
    var inUse: Boolean = false
)
private val colors = listOf(
    ColorData(Color.Blue, true, true),
    ColorData(Color.Red, false, true),
    ColorData(Color.Black),
    ColorData(Color.Magenta),
    ColorData(Color.Yellow),
    ColorData(Color.Green),
    ColorData(Color.Cyan),
    ColorData(Color(255, 128, 20))
)

private fun addLine(isLeft: Boolean) {
    val index = colors.indexOfFirst { !it.inUse }
    colors[index].isLeft = isLeft
    colors[index].inUse = true
    val color = colors[index].color
    val points = if (isLeft) {
        (0..12).map { ChartValue(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableList()
    } else {
        (0..15).map { ChartValue(it * 8.5, Random.nextDouble(0.0, 150.0)) }.toMutableList()
    }
    val line = LineChartData.Line(
        points,
        LineChartData.Line.PointsOrder.Ordered.X,
        index,
        LineChartData.Line.Customization(color, join = StrokeJoin.Round)
    )
    val lines = if (isLeft) {
        leftAxis.lines
    } else {
        rightAxis.lines
    }
    val addIndex = if (lines.isEmpty()) {
        0
    } else {
        var higherIndexCount = 0
        for (i in index+1..<colors.lastIndex) {
            if (colors[i].isLeft == isLeft && colors[i].inUse) {
                ++higherIndexCount
            }
        }
        lines.size - higherIndexCount
    }
    lines.add(addIndex, line)
}

private fun removeLine(isLeft: Boolean) {
    if (isLeft) {
        leftAxis.lines.removeLast()
    } else {
        rightAxis.lines.removeLast()
    }
    colors.last {it.isLeft == isLeft && it.inUse}.apply {
        this.isLeft = null
        inUse = false
    }
}

@Composable
fun SimpleTwoAxesLineChartExample() {
    val chartData by remember {
        mutableStateOf(
            LineChartData(
                leftAxis,
                rightAxis,
                bottomAxis,
            ),
            referentialEqualityPolicy()
        )
    }
    var addLineEnabled by remember { mutableStateOf(true) }
    var removeLeftAxisLineEnabled by remember { mutableStateOf(true) }
    var removeRightAxisLineEnabled by remember { mutableStateOf(true) }
    LaunchedEffect(leftAxis.lines.size, rightAxis.lines.size) {
        val size = leftAxis.lines.size + rightAxis.lines.size
        addLineEnabled = size < colors.size
        removeLeftAxisLineEnabled = leftAxis.lines.isNotEmpty()
        removeRightAxisLineEnabled = rightAxis.lines.isNotEmpty()
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            Column {
                Text("Left axis", style = MaterialTheme.typography.titleSmall)
                Row(Modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button({
                        addLine(true)
                    }, enabled = addLineEnabled) {
                        Text("Add line")
                    }
                    Button({
                        removeLine(true)
                    }, enabled = removeLeftAxisLineEnabled) {
                        Text("Remove line")
                    }
                }
            }
            Column {
                Text("Right axis", style = MaterialTheme.typography.titleSmall)
                Row(Modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button({
                        addLine(false)
                    }, enabled = addLineEnabled) {
                        Text("Add line")
                    }
                    Button({
                        removeLine(false)
                    }, enabled = removeRightAxisLineEnabled) {
                        Text("Remove line")
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        LineChart(
            Modifier.fillMaxWidth().height(300.dp).padding(start = 8.dp),
            chartData,
        )
    }
}