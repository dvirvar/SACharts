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
import com.skellyapps.charts.bar.model.HorizontalBarChartData
import com.skellyapps.charts.bar.view.HorizontalBarChart
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.example.roundToDecimals
import kotlin.random.Random

private var blueCategory = BarChartData.Category(
    (0..12).map { ChartValueCoordinate(Random.nextDouble(-30.0, 30.0)) }.toMutableList(),
    0,
    BarChartData.Category.Customization(Color.Blue, topRightCornerRadius = CornerRadius(5f), bottomRightCornerRadius = CornerRadius(5f)),
)

private val isLeftAxisState = mutableStateOf(true)

private val bottomAxis = HorizontalBarChartData.XAxis(
    mutableListOf(blueCategory),
    BarChartData.Type.Grouped(0.dp, 5.dp),
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
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { index ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLeftAxisState.value) {
            Text(index.toString())
        }
        HorizontalDivider(Modifier.width(8.dp))
        if (!isLeftAxisState.value) {
            Text(index.toString())
        }
    }
}

@Composable
fun SimpleHorizontalBarChartExample() {
    var chartData by retain {
        mutableStateOf(
            HorizontalBarChartData(
                bottomAxis,
                isLeftAxisState.value,
                yAxis,
            ),
            referentialEqualityPolicy()
        )
    }
    val isLeftAxis by retain(isLeftAxisState) { isLeftAxisState }
    LaunchedEffect(isLeftAxis) {
        val customization = if (isLeftAxis) {
            blueCategory.customization.copy(
                topRightCornerRadius = CornerRadius(5f),
                bottomRightCornerRadius = CornerRadius(5f),
                topLeftCornerRadius = CornerRadius.Zero,
                bottomLeftCornerRadius = CornerRadius.Zero
            )
        } else {
            blueCategory.customization.copy(
                topLeftCornerRadius = CornerRadius(5f),
                bottomLeftCornerRadius = CornerRadius(5f),
                topRightCornerRadius = CornerRadius.Zero,
                bottomRightCornerRadius = CornerRadius.Zero
            )
        }
        blueCategory = blueCategory.copy(customization = customization)
        chartData = chartData.copy(bottomAxis.copy(mutableListOf(blueCategory)), isLeftAxis)
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.toggleable(isLeftAxis, role = Role.Checkbox) { isLeftAxisState.value = it }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(isLeftAxis, null)
                Text("Is left axis")
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalBarChart(
            Modifier.fillMaxWidth().height(300.dp).padding(start = if (isLeftAxis) 8.dp else 24.dp, end = if (isLeftAxis) 24.dp else 8.dp),
            chartData,
        )
    }
}