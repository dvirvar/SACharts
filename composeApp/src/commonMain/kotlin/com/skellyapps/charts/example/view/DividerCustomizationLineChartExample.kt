package com.skellyapps.charts.example.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.example.roundToDecimals
import com.skellyapps.charts.line.model.LineChartData
import com.skellyapps.charts.line.view.LineChart
import kotlin.math.max
import kotlin.random.Random

private const val blueTag = 0
private const val redTag = 1

private val colors = listOf(Color.Blue, Color.Red)

private val blueLine = LineChartData.Line(
    (0..12).map { ChartValue(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    blueTag,
    LineChartData.Line.Customization(colors[blueTag], join = StrokeJoin.Round)
)
private var leftAxis = LineChartData.YAxis(
    lines = mutableListOf(blueLine),
    value = GridChartData.Axis.Value.Step(20.0),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp, StrokeCap.Round)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f)))) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value.roundToDecimals(1).toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}
private var lastLeftAxisDividerCustomization = leftAxis.dividerCustomization
private val redLine = LineChartData.Line(
    (0..15).map { ChartValue(it * 8.5, Random.nextDouble(0.0, 150.0)) }.toMutableList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    redTag,
    LineChartData.Line.Customization(colors[redTag], join = StrokeJoin.Round)
)
private val rightAxis = LineChartData.YAxis(
    lines = mutableListOf(redLine),
    value = GridChartData.Axis.Value.Fixed(5)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.width(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}
private val bottomAxis = LineChartData.XAxis(
    value = GridChartData.Axis.Value.Fixed(8),
    gridLines = GridChartData.Axis.GridLines(showFirstLine = false, customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Brush.horizontalGradient(colors))) { value ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}

@Composable
fun DividerCustomizationLineChartExample() {
    val density = LocalDensity.current
    var chartData by remember {
        mutableStateOf(
            LineChartData(
                leftAxis,
                rightAxis,
                bottomAxis,
            ),
            referentialEqualityPolicy()
        )
    }
    var showLeftAxisDivider by remember { mutableStateOf(true) }
    LaunchedEffect(showLeftAxisDivider) {
        val dividerCustomization = if (showLeftAxisDivider) {
            lastLeftAxisDividerCustomization
        } else {
            lastLeftAxisDividerCustomization = leftAxis.dividerCustomization
            null
        }
        leftAxis = leftAxis.copy(dividerCustomization = dividerCustomization)
        chartData = chartData.copy(leftAxis)
    }
    var leftAxisDividerDashLengthText by remember { mutableStateOf("10") }
    var leftAxisDividerDashSpaceLengthText by remember { mutableStateOf("15") }
    LaunchedEffect(leftAxisDividerDashLengthText, leftAxisDividerDashSpaceLengthText) {
        val length = with(density) {
            (leftAxisDividerDashLengthText.toFloatOrNull()?.dp ?: 10.dp).toPx()
        }
        val spaceLength = with(density) {
            (leftAxisDividerDashSpaceLengthText.toFloatOrNull()?.dp ?: 15.dp).toPx()
        }
        try {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(length, spaceLength))
            val dividerCustomization = leftAxis.dividerCustomization?.copy(pathEffect = pathEffect)
            leftAxis = leftAxis.copy(dividerCustomization = dividerCustomization)
            chartData = chartData.copy(leftAxis)
        } catch (e: Exception) {
            println(e)
        }
    }
    var showRightAxisDivider by remember { mutableStateOf(false) }
    LaunchedEffect(showRightAxisDivider) {
        val dividerCustomization = if (showRightAxisDivider) {
            GridChartData.Axis.DividerCustomization(Color.Black)
        } else {
            null
        }
        chartData = chartData.copy(rightAxis = chartData.rightAxis?.copy(dividerCustomization = dividerCustomization))
    }
    var showBottomAxisDivider by remember { mutableStateOf(true) }
    LaunchedEffect(showBottomAxisDivider) {
        val dividerCustomization = if (showBottomAxisDivider) {
            GridChartData.Axis.DividerCustomization(Brush.horizontalGradient(colors))
        } else {
            null
        }
        chartData = chartData.copy(bottomAxis = chartData.bottomAxis?.copy(dividerCustomization = dividerCustomization))
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Left axis", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(showLeftAxisDivider, role = Role.Checkbox) { showLeftAxisDivider = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showLeftAxisDivider, null)
                        Text("Show divider")
                    }
                    if (showLeftAxisDivider) {
                        OutlinedTextField(
                            leftAxisDividerDashLengthText,
                            {
                                if (it.isEmpty() || it.toFloatOrNull() != null) {
                                    leftAxisDividerDashLengthText = it
                                }
                            },
                            Modifier.onPreviewKeyEvent {
                                if (it.type == KeyEventType.KeyDown) {
                                    if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                        var r = (leftAxisDividerDashLengthText.toFloatOrNull() ?: 10f)
                                        if (it.key == Key.DirectionUp) {
                                            r += 1f
                                        } else {
                                            r = max(r - 1f, 0f)
                                        }
                                        leftAxisDividerDashLengthText = r.toString()
                                        true
                                    } else {
                                        false
                                    }
                                } else {
                                    false
                                }
                            },
                            label = {Text("Dash length(dp)")},
                            placeholder = {Text("10")},
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        Spacer(Modifier.width(4.dp))
                        OutlinedTextField(
                            leftAxisDividerDashSpaceLengthText,
                            {
                                if (it.isEmpty() || it.toFloatOrNull() != null) {
                                    leftAxisDividerDashSpaceLengthText = it
                                }
                            },
                            Modifier.onPreviewKeyEvent {
                                if (it.type == KeyEventType.KeyDown) {
                                    if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                        var r = (leftAxisDividerDashSpaceLengthText.toFloatOrNull() ?: 15f)
                                        if (it.key == Key.DirectionUp) {
                                            r += 1f
                                        } else {
                                            r = max(r -1f, 0f)
                                        }
                                        leftAxisDividerDashSpaceLengthText = r.toString()
                                        true
                                    } else {
                                        false
                                    }
                                } else {
                                    false
                                }
                            },
                            label = {Text("Dash space length(dp)")},
                            placeholder = {Text("15")},
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
            }
            Column {
                Text("Right axis", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(showRightAxisDivider, role = Role.Checkbox) { showRightAxisDivider = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showRightAxisDivider, null)
                        Text("Show divider")
                    }
                }
            }
            Column {
                Text("Bottom axis", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(showBottomAxisDivider, role = Role.Checkbox) { showBottomAxisDivider = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showBottomAxisDivider, null)
                        Text("Show divider")
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
        }
    }
    Spacer(Modifier.height(8.dp))
    LineChart(
        Modifier.fillMaxWidth().height(300.dp).padding(start = 8.dp),
        chartData,
    )
}