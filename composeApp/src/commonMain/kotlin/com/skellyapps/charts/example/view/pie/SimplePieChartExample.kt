@file:OptIn(ExperimentalMaterial3Api::class)

package com.skellyapps.charts.example.view.pie

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import com.skellyapps.charts.pie.model.PieChartData
import com.skellyapps.charts.pie.view.PieChart
import kotlin.random.Random

private val slices = mutableStateListOf(PieChartData.Slice(20.0, Color.Blue))

private var currentColor = 0
private val colors = listOf(Color.Blue, Color.Red, Color.Black, Color.Magenta, Color.Yellow, Color.Green, Color.Cyan)

private fun generateSlice(): PieChartData.Slice {
    ++currentColor
    return PieChartData.Slice(
        Random.nextDouble(10.0, 30.0),
        colors[currentColor]
    )
}

@Composable
fun SimplePieChartExample() {
    var chartData by remember {
        mutableStateOf(
            PieChartData(
                slices,
                customization = PieChartData.Customization(
                    PieChartData.Customization.Line(
                        3.dp,
                        SolidColor(Color(170,90,170))
                    ),
                    PieChartData.Customization.Line(
                        4.dp,
                        SolidColor(Color(170,90,170))
                    )
                )
            ),
            referentialEqualityPolicy()
        )
    }
    var addLineEnabled by remember { mutableStateOf(true) }
    var removeLineEnabled by remember { mutableStateOf(true) }
    LaunchedEffect(slices.size) {
        addLineEnabled = slices.size < colors.size
        removeLineEnabled = slices.size > 1
    }
    var startAngle by remember { mutableFloatStateOf(chartData.startAngle) }
    LaunchedEffect(startAngle) {
        chartData = chartData.copy(startAngle = startAngle)
    }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button({
                slices.add(generateSlice())
            }, enabled = addLineEnabled) {
                Text("Add slice")
            }
            Button({
                --currentColor
                slices.removeLast()
            }, enabled = removeLineEnabled) {
                Text("Remove slice")
            }
            Column {
                Text("Start angle: ${startAngle.fastRoundToInt()}")
                Slider(startAngle, {startAngle = it}, valueRange = 0f..359f, steps = 360)
            }
        }
        Spacer(Modifier.height(8.dp))
        PieChart(
            Modifier.fillMaxWidth().height(300.dp),
            chartData,
        )
    }
}