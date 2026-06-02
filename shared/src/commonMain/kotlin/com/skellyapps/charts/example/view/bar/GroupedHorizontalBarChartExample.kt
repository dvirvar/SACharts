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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.skellyapps.charts.bar.model.BarChartData
import com.skellyapps.charts.bar.model.HorizontalBarChartData
import com.skellyapps.charts.bar.view.HorizontalBarChart
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.example.roundToDecimals
import kotlin.random.Random

private val blueCategory = BarChartData.Category(
    (0..12).map { ChartValueCoordinate(Random.nextDouble(-30.0, 30.0)) }.toMutableList(),
    0,
    BarChartData.Category.Customization(Color.Blue, topRightCornerRadius = CornerRadius(5f), bottomRightCornerRadius = CornerRadius(5f)),
)

private val bottomAxis = HorizontalBarChartData.XAxis(
    mutableStateListOf(blueCategory),
    BarChartData.Type.Grouped(3.dp, 10.dp),
    minValue = -30.0,
    maxValue = 30.0,
    value = GridChartData.Axis.Value.Fixed(15),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}

private val yAxis = HorizontalBarChartData.YAxis(
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value.toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}

private var currentColor = 0
private val colors = listOf(Color.Blue, Color.Red, Color.Magenta, Color.DarkGray)

private fun generateCategory(): BarChartData.Category {
    ++currentColor
    return BarChartData.Category(
        (0..12).map { ChartValueCoordinate(Random.nextDouble(-30.0, 30.0)) }.toMutableList(),
        currentColor,
        BarChartData.Category.Customization(colors[currentColor], topRightCornerRadius = CornerRadius(5f), bottomRightCornerRadius = CornerRadius(5f)),
    )
}

@Composable
fun GroupedHorizontalBarChartExample() {
    val textMeasurer = rememberTextMeasurer()
    var chartData by retain {
        mutableStateOf(
            HorizontalBarChartData(
                bottomAxis,
                true,
                yAxis,
            ),
            referentialEqualityPolicy()
        )
    }
    var addCategoryEnabled by retain { mutableStateOf(true) }
    var removeCategoryEnabled by retain { mutableStateOf(true) }
    LaunchedEffect(bottomAxis.categories.size) {
        addCategoryEnabled = bottomAxis.categories.size < colors.size
        removeCategoryEnabled = bottomAxis.categories.size > 1
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button({
                bottomAxis.categories.add(generateCategory())
            }, enabled = addCategoryEnabled) {
                Text("Add category")
            }
            Button({
                --currentColor
                bottomAxis.categories.removeLast()
            }, enabled = removeCategoryEnabled) {
                Text("Remove category")
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalBarChart(
            Modifier.fillMaxWidth().height(600.dp).padding(start = 8.dp, end = 24.dp),
            chartData,
        ) { canvasSize, categoryTag, index, topLeft, barSize ->
            val value = bottomAxis.categories[categoryTag].values[index].value
            val isNegative = value < 0.0
            val text = value.roundToDecimals(1).toString()
            val layout = textMeasurer.measure(text)
            val x = if (isNegative) {
                topLeft.x - layout.size.width
            } else {
                topLeft.x + barSize.width
            }.coerceIn(0f, canvasSize.width - layout.size.width)
            val topLeft = topLeft.copy(x, topLeft.y + barSize.height / 2f - layout.size.height / 2f)
            drawText(layout, Color.Black, topLeft)
        }
    }
}