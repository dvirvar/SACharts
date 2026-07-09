package com.skellyapps.charts.example.view.bar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.skellyapps.charts.bar.model.BarChartData
import com.skellyapps.charts.bar.view.BarChart
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.example.roundToDecimals
import kotlin.random.Random

private val blueCategory = BarChartData.Category(
    (0..12).map { ChartValueCoordinate(Random.nextDouble(-30.0, 30.0)) }.toMutableList(),
    0,
    BarChartData.Category.Customization(Color.Blue, topLeftCornerRadius = CornerRadius(5f), topRightCornerRadius = CornerRadius(5f)),
)

private val isLeftAxisState = mutableStateOf(true)

private val yAxis = BarChartData.YAxis(
    mutableListOf(blueCategory),
    BarChartData.Type.Grouped(0.dp, 5.dp),
    minValue = -30.0,
    maxValue = 30.0,
    value = GridChartData.Axis.Value.Fixed(15),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLeftAxisState.value) {
            Text(value.roundToDecimals(1).toString())
        }
        HorizontalDivider(Modifier.width(8.dp))
        if (!isLeftAxisState.value) {
            Text(value.roundToDecimals(1).toString())
        }
    }
}

private val bottomAxis = BarChartData.XAxis(
    GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    GridChartData.Axis.DividerCustomization(Color.Black)) { index ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(index.toString())
    }
}

@Composable
fun SimpleBarChartExample() {
    var chartData by retain {
        mutableStateOf(
            BarChartData(
                yAxis,
                isLeftAxisState.value,
                bottomAxis,
            ),
            referentialEqualityPolicy()
        )
    }
    val isLeftAxis by retain(isLeftAxisState) { isLeftAxisState }
    LaunchedEffect(isLeftAxis) {
        chartData = chartData.copy(isLeftYAxis = isLeftAxis)
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.toggleable(isLeftAxis, role = Role.Checkbox) { isLeftAxisState.value = it }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(isLeftAxis, null)
                Text("Is left axis")
            }
        }
        Spacer(Modifier.height(8.dp))
        BarChart(
            Modifier.fillMaxWidth().height(300.dp).padding(start = if (isLeftAxis) 8.dp else 24.dp, end = if (isLeftAxis) 24.dp else 8.dp),
            chartData,
        )
    }
}