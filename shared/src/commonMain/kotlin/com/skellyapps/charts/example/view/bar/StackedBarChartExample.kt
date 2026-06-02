package com.skellyapps.charts.example.view.bar

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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.skellyapps.charts.bar.model.BarChartData
import com.skellyapps.charts.bar.view.BarChart
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.example.roundToDecimals
import kotlin.random.Random

private val blueCategory = BarChartData.Category(
    (0..12).map { ChartValueCoordinate(Random.nextDouble(6.0, 30.0)) }.toMutableList(),
    0,
    BarChartData.Category.Customization(Color.Blue),
)

private val yAxis = BarChartData.YAxis(
    mutableStateListOf(blueCategory),
    BarChartData.Type.Stacked(10.dp),
    minValue = 0.0,
    maxValue = 120.0,
    value = GridChartData.Axis.Value.Fixed(15),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value.roundToDecimals(1).toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}

private val bottomAxis = BarChartData.XAxis(
    GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(value.toString())
    }
}

private var currentColor = 0
private val colors = listOf(Color.Blue, Color.Red, Color.Magenta, Color.DarkGray)

private fun generateCategory(): BarChartData.Category {
    ++currentColor
    return BarChartData.Category(
        (0..12).map { ChartValueCoordinate(Random.nextDouble(6.0, 30.0)) }.toMutableList(),
        currentColor,
        BarChartData.Category.Customization(colors[currentColor]),
    )
}

@Composable
fun StackedBarChartExample() {
    val textMeasurer = rememberTextMeasurer()
    var chartData by retain {
        mutableStateOf(
            BarChartData(
                yAxis,
                true,
                bottomAxis,
            ),
            referentialEqualityPolicy()
        )
    }
    var addCategoryEnabled by retain { mutableStateOf(true) }
    var removeCategoryEnabled by retain { mutableStateOf(true) }
    LaunchedEffect(yAxis.categories.size) {
        addCategoryEnabled = yAxis.categories.size < colors.size
        removeCategoryEnabled = yAxis.categories.size > 1
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button({
                yAxis.categories.add(generateCategory())
            }, enabled = addCategoryEnabled) {
                Text("Add category")
            }
            Button({
                --currentColor
                yAxis.categories.removeLast()
            }, enabled = removeCategoryEnabled) {
                Text("Remove category")
            }
        }
        Spacer(Modifier.height(8.dp))
        BarChart(
            Modifier.fillMaxWidth().height(300.dp).padding(start = 8.dp, end = 24.dp),
            chartData,
        ) { canvasSize, categoryTag, index, topLeft, barSize ->
            val text = yAxis.categories[categoryTag].values[index].value.roundToDecimals(1).toString()
            val layout = textMeasurer.measure(text)
            val topLeft = topLeft.copy(topLeft.x + barSize.width / 2f - layout.size.width / 2f, topLeft.y + barSize.height - layout.size.height)
            drawText(layout, Color.White, topLeft)
        }
    }
}