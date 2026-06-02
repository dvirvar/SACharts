package com.skellyapps.charts.example.view.line

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.retain.retain
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
private val bottomAxis = LineChartData.XAxis(
    value = GridChartData.Axis.Value.Fixed(8),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}

private var currentColor = 0
private val colors = listOf(Color.Blue, Color.Red, Color.Black, Color.Magenta, Color.Yellow, Color.Green, Color.Cyan)

private fun generateLine(): LineChartData.Line {
    ++currentColor
    return LineChartData.Line(
        (0..12).map { ChartValue(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableList(),
        LineChartData.Line.PointsOrder.Ordered.X,
        currentColor,
        LineChartData.Line.Customization(colors[currentColor], join = StrokeJoin.Round)
    )
}

@Composable
fun SimpleLineChartExample() {
    val chartData by retain {
        mutableStateOf(
            LineChartData(
                leftAxis = leftAxis,
                bottomAxis = bottomAxis,
            ),
            referentialEqualityPolicy()
        )
    }
    var addLineEnabled by retain { mutableStateOf(true) }
    var removeLineEnabled by retain { mutableStateOf(true) }
    LaunchedEffect(leftAxis.lines.size) {
        addLineEnabled = leftAxis.lines.size < colors.size
        removeLineEnabled = leftAxis.lines.isNotEmpty()
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button({
                leftAxis.lines.add(generateLine())
            }, enabled = addLineEnabled) {
                Text("Add line")
            }
            Button({
                --currentColor
                leftAxis.lines.removeLast()
            }, enabled = removeLineEnabled) {
                Text("Remove line")
            }
        }
        Spacer(Modifier.height(8.dp))
        LineChart(
            Modifier.fillMaxWidth().height(300.dp).padding(start = 8.dp, end = 24.dp),
            chartData,
        )
    }
}