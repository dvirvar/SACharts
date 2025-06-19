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
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp, StrokeCap.Round, PathEffect.dashPathEffect(floatArrayOf(10f, 15f)))),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value.roundToDecimals(1).toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}
private var lastLeftAxisGridLines = leftAxis.gridLines
private val redLine = LineChartData.Line(
    (0..15).map { ChartValue(it * 8.5, Random.nextDouble(0.0, 150.0)) }.toMutableList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    redTag,
    LineChartData.Line.Customization(colors[redTag], join = StrokeJoin.Round)
)
private val rightAxis = LineChartData.YAxis(
    lines = mutableListOf(redLine),
    value = GridChartData.Axis.Value.Fixed(5),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.width(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}
private val bottomAxis = LineChartData.XAxis(
    value = GridChartData.Axis.Value.Fixed(8),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Brush.verticalGradient(colors), 1.5.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}

@Composable
fun GridLineCustomizationLineChartExample() {
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
    var showLeftAxisGrid by remember { mutableStateOf(true) }
    LaunchedEffect(showLeftAxisGrid) {
        val gridLines = if (showLeftAxisGrid) {
            lastLeftAxisGridLines
        } else {
            lastLeftAxisGridLines = leftAxis.gridLines
            null
        }
        leftAxis = leftAxis.copy(gridLines = gridLines)
        chartData = chartData.copy(leftAxis)
    }
    var leftAxisGridDashLengthText by remember { mutableStateOf("10") }
    var leftAxisGridDashSpaceLengthText by remember { mutableStateOf("15") }
    LaunchedEffect(leftAxisGridDashLengthText, leftAxisGridDashSpaceLengthText) {
        val length = with(density) {
            (leftAxisGridDashLengthText.toFloatOrNull()?.dp ?: 10.dp).toPx()
        }
        val spaceLength = with(density) {
            (leftAxisGridDashSpaceLengthText.toFloatOrNull()?.dp ?: 15.dp).toPx()
        }
        try {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(length, spaceLength))
            leftAxis.gridLines?.customization?.copy(pathEffect = pathEffect)?.let {
                leftAxis = leftAxis.copy(gridLines = leftAxis.gridLines?.copy(customization = it))
                chartData = chartData.copy(leftAxis)
            }
        } catch (e: Exception) {
            println(e)
        }
    }
    var showRightAxisGrid by remember { mutableStateOf(false) }
    LaunchedEffect(showRightAxisGrid) {
        val gridLines = if (showRightAxisGrid) {
            GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp))
        } else {
            null
        }
        chartData = chartData.copy(rightAxis = chartData.rightAxis?.copy(gridLines = gridLines))
    }
    var showBottomAxisGrid by remember { mutableStateOf(true) }
    LaunchedEffect(showBottomAxisGrid) {
        val gridLines = if (showBottomAxisGrid) {
            GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Brush.verticalGradient(colors), 1.5.dp))
        } else {
            null
        }
        chartData = chartData.copy(bottomAxis = chartData.bottomAxis?.copy(gridLines = gridLines))
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Left axis", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(showLeftAxisGrid, role = Role.Checkbox) { showLeftAxisGrid = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showLeftAxisGrid, null)
                        Text("Show grid")
                    }
                    if (showLeftAxisGrid) {
                        OutlinedTextField(
                            leftAxisGridDashLengthText,
                            {
                                if (it.isEmpty() || it.toFloatOrNull() != null) {
                                    leftAxisGridDashLengthText = it
                                }
                            },
                            Modifier.onPreviewKeyEvent {
                                if (it.type == KeyEventType.KeyDown) {
                                    if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                        var r = (leftAxisGridDashLengthText.toFloatOrNull() ?: 10f)
                                        if (it.key == Key.DirectionUp) {
                                            r += 1f
                                        } else {
                                            r = max(r - 1f, 0f)
                                        }
                                        leftAxisGridDashLengthText = r.toString()
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
                            leftAxisGridDashSpaceLengthText,
                            {
                                if (it.isEmpty() || it.toFloatOrNull() != null) {
                                    leftAxisGridDashSpaceLengthText = it
                                }
                            },
                            Modifier.onPreviewKeyEvent {
                                if (it.type == KeyEventType.KeyDown) {
                                    if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                        var r = (leftAxisGridDashSpaceLengthText.toFloatOrNull() ?: 15f)
                                        if (it.key == Key.DirectionUp) {
                                            r += 1f
                                        } else {
                                            r = max(r -1f, 0f)
                                        }
                                        leftAxisGridDashSpaceLengthText = r.toString()
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
                    Row(Modifier.toggleable(showRightAxisGrid, role = Role.Checkbox) { showRightAxisGrid = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showRightAxisGrid, null)
                        Text("Show grid")
                    }
                }
            }
            Column {
                Text("Bottom axis", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(showBottomAxisGrid, role = Role.Checkbox) { showBottomAxisGrid = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showBottomAxisGrid, null)
                        Text("Show grid")
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