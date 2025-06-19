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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
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
private val leftAxisValueView = @Composable { value: Double ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value.roundToDecimals(1).toString(), color = Color.Blue)
        HorizontalDivider(Modifier.width(8.dp))
    }
}
private val leftAxis = LineChartData.YAxis(
    mutableListOf(blueLine),
    DpOffset(10.dp, 10.dp),
    value = GridChartData.Axis.Value.Step(20.0),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black),
    valueView = leftAxisValueView
)

private val redLine = LineChartData.Line(
    (0..15).map { ChartValue(it * 8.5, Random.nextDouble(0.0, 150.0)) }.toMutableList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    redTag,
    LineChartData.Line.Customization(colors[redTag], join = StrokeJoin.Round)
)

private val rightAxisValueView = @Composable { value: Double ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.width(8.dp))
        Text(value.roundToDecimals(1).toString(), color = Color.Red)
    }
}

private val rightAxis = LineChartData.YAxis(
    mutableListOf(redLine),
    DpOffset(5.dp, 5.dp),
    value = GridChartData.Axis.Value.Fixed(5),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black),
    valueView = rightAxisValueView
)

private val bottomAxisValueView = @Composable { value: Double ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        val color = when {
            value < 30.0 -> Color(0, 128, 0)
            value > 90.0 -> Color.Red
            else -> Color(255, 165, 0)
        }
        Text(value.roundToDecimals(1).toString(), color = color)
    }
}

private val bottomAxis = LineChartData.XAxis(
    value = GridChartData.Axis.Value.Fixed(8),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black),
    valueView = bottomAxisValueView
)

@Composable
fun AxesCustomizationLineChartExample() {
    var chartData by remember {
        mutableStateOf(
            LineChartData(
                leftAxis,
                rightAxis,
                bottomAxis,
                xAxisOffset = DpOffset(10.dp, 10.dp)
            ),
            referentialEqualityPolicy()
        )
    }
    var showLeftAxisValues by remember { mutableStateOf(true) }
    LaunchedEffect(showLeftAxisValues) {
        val valueView = if (showLeftAxisValues) {
            leftAxisValueView
        } else {
            null
        }
        chartData = chartData.copy(chartData.leftAxis?.copy(valueView = valueView))
    }
    var leftAxisMinOffsetText by remember { mutableStateOf("10") }
    var leftAxisMaxOffsetText by remember { mutableStateOf("10") }
    LaunchedEffect(leftAxisMinOffsetText, leftAxisMaxOffsetText) {
        val minOffset = leftAxisMinOffsetText.toFloatOrNull()?.dp ?: 0.dp
        val maxOffset = leftAxisMaxOffsetText.toFloatOrNull()?.dp ?: 0.dp
        try {
            chartData = chartData.copy(chartData.leftAxis?.copy(offset = DpOffset(minOffset, maxOffset)))
        } catch (e: Exception) {
            println(e)
        }
    }
    var leftAxisValuesStepText by remember { mutableStateOf("20") }
    LaunchedEffect(leftAxisValuesStepText) {
        val step = leftAxisValuesStepText.toDoubleOrNull() ?: 1.0
        val value = GridChartData.Axis.Value.Step(step)
        chartData = chartData.copy(chartData.leftAxis?.copy(value = value))
    }
    var showRightAxisValues by remember { mutableStateOf(true) }
    LaunchedEffect(showRightAxisValues) {
        val valueView = if (showRightAxisValues) {
            rightAxisValueView
        } else {
            null
        }
        chartData = chartData.copy(rightAxis = chartData.rightAxis?.copy(valueView = valueView))
    }
    var rightAxisMinOffsetText by remember { mutableStateOf("5") }
    var rightAxisMaxOffsetText by remember { mutableStateOf("5") }
    LaunchedEffect(rightAxisMinOffsetText, rightAxisMaxOffsetText) {
        val minOffset = rightAxisMinOffsetText.toFloatOrNull()?.dp ?: 0.dp
        val maxOffset = rightAxisMaxOffsetText.toFloatOrNull()?.dp ?: 0.dp
        try {
            chartData = chartData.copy(rightAxis = chartData.rightAxis?.copy(offset = DpOffset(minOffset, maxOffset)))
        } catch (e: Exception) {
            println(e)
        }
    }
    var rightAxisValuesAmountText by remember { mutableStateOf("5") }
    LaunchedEffect(rightAxisValuesAmountText) {
        val amount: Int = rightAxisValuesAmountText.toUIntOrNull()?.toInt() ?: 1
        val value = GridChartData.Axis.Value.Fixed(amount)
        chartData = chartData.copy(rightAxis = chartData.rightAxis?.copy(value = value))
    }
    var showBottomAxisValues by remember { mutableStateOf(true) }
    LaunchedEffect(showBottomAxisValues) {
        val valueView = if (showBottomAxisValues) {
            bottomAxisValueView
        } else {
            null
        }
        chartData = chartData.copy(bottomAxis = chartData.bottomAxis?.copy(valueView = valueView))
    }
    var bottomAxisMinOffsetText by remember { mutableStateOf("10") }
    var bottomAxisMaxOffsetText by remember { mutableStateOf("10") }
    LaunchedEffect(bottomAxisMinOffsetText, bottomAxisMaxOffsetText) {
        val minOffset = bottomAxisMinOffsetText.toFloatOrNull()?.dp ?: 0.dp
        val maxOffset = bottomAxisMaxOffsetText.toFloatOrNull()?.dp ?: 0.dp
        try {
            chartData = chartData.copy(xAxisOffset = DpOffset(minOffset, maxOffset))
        } catch (e: Exception) {
            println(e)
        }
    }
    var bottomAxisValuesAmountText by remember { mutableStateOf("8") }
    LaunchedEffect(bottomAxisValuesAmountText) {
        val amount: Int = bottomAxisValuesAmountText.toUIntOrNull()?.toInt() ?: 1
        val value = GridChartData.Axis.Value.Fixed(amount)
        chartData = chartData.copy(bottomAxis = chartData.bottomAxis?.copy(value = value))
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Left axis", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(showLeftAxisValues, role = Role.Checkbox) { showLeftAxisValues = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showLeftAxisValues, null)
                        Text("Show values")
                    }
                    OutlinedTextField(
                        leftAxisMinOffsetText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                leftAxisMinOffsetText = it
                            }
                        },
                        Modifier.onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                    var r = (leftAxisMinOffsetText.toFloatOrNull() ?: 0f)
                                    if (it.key == Key.DirectionUp) {
                                        r += 1f
                                    } else {
                                        r = max(r - 1f, 0f)
                                    }
                                    leftAxisMinOffsetText = r.toString()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                        label = {Text("Min offset(dp)")},
                        placeholder = {Text("0")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        leftAxisMaxOffsetText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                leftAxisMaxOffsetText = it
                            }
                        },
                        Modifier.onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                    var r = (leftAxisMaxOffsetText.toFloatOrNull() ?: 0f)
                                    if (it.key == Key.DirectionUp) {
                                        r += 1f
                                    } else {
                                        r = max(r -1f, 0f)
                                    }
                                    leftAxisMaxOffsetText = r.toString()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                        label = {Text("Max offset(dp)")},
                        placeholder = {Text("0")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        leftAxisValuesStepText,
                        {
                            if (it.isEmpty() || it.toDoubleOrNull()?.run { this > 0.0 } == true) {
                                leftAxisValuesStepText = it
                            }
                        },
                        Modifier.onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                    var r = (leftAxisValuesStepText.toDoubleOrNull() ?: 1.0)
                                    if (it.key == Key.DirectionUp) {
                                        r += 1.0
                                    } else {
                                        r = max(r -1.0, 1.0)
                                    }
                                    leftAxisValuesStepText = r.toString()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                        label = {Text("Values step")},
                        placeholder = {Text("1")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }
            Column {
                Text("Right axis", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(showRightAxisValues, role = Role.Checkbox) { showRightAxisValues = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showRightAxisValues, null)
                        Text("Show values")
                    }
                    OutlinedTextField(
                        rightAxisMinOffsetText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                rightAxisMinOffsetText = it
                            }
                        },
                        Modifier.onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                    var r = (rightAxisMinOffsetText.toFloatOrNull() ?: 0f)
                                    if (it.key == Key.DirectionUp) {
                                        r += 1f
                                    } else {
                                        r = max(r - 1f, 0f)
                                    }
                                    rightAxisMinOffsetText = r.toString()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                        label = {Text("Min offset(dp)")},
                        placeholder = {Text("0")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        rightAxisMaxOffsetText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                rightAxisMaxOffsetText = it
                            }
                        },
                        Modifier.onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                    var r = (rightAxisMaxOffsetText.toFloatOrNull() ?: 0f)
                                    if (it.key == Key.DirectionUp) {
                                        r += 1f
                                    } else {
                                        r = max(r -1f, 0f)
                                    }
                                    rightAxisMaxOffsetText = r.toString()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                        label = {Text("Max offset(dp)")},
                        placeholder = {Text("0")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        rightAxisValuesAmountText,
                        {
                            if (it.isEmpty() || it.toUIntOrNull()?.run { this > 1u } == true) {
                                rightAxisValuesAmountText = it
                            }
                        },
                        Modifier.onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                    var r = (rightAxisValuesAmountText.toUIntOrNull()?.toInt() ?: 0)
                                    if (it.key == Key.DirectionUp) {
                                        r += 1
                                    } else {
                                        r = max(r - 1, 1)
                                    }
                                    rightAxisValuesAmountText = r.toString()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                        label = {Text("Values amount")},
                        placeholder = {Text("1")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
            Column {
                Text("Bottom axis", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(showBottomAxisValues, role = Role.Checkbox) { showBottomAxisValues = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showBottomAxisValues, null)
                        Text("Show values")
                    }
                    OutlinedTextField(
                        bottomAxisMinOffsetText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                bottomAxisMinOffsetText = it
                            }
                        },
                        Modifier.onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                    var r = (bottomAxisMinOffsetText.toFloatOrNull() ?: 0f)
                                    if (it.key == Key.DirectionUp) {
                                        r += 1f
                                    } else {
                                        r = max(r - 1f, 0f)
                                    }
                                    bottomAxisMinOffsetText = r.toString()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                        label = {Text("Min offset(dp)")},
                        placeholder = {Text("0")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        bottomAxisMaxOffsetText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                bottomAxisMaxOffsetText = it
                            }
                        },
                        Modifier.onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                    var r = (bottomAxisMaxOffsetText.toFloatOrNull() ?: 0f)
                                    if (it.key == Key.DirectionUp) {
                                        r += 1f
                                    } else {
                                        r = max(r -1f, 0f)
                                    }
                                    bottomAxisMaxOffsetText = r.toString()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                        label = {Text("Max offset(dp)")},
                        placeholder = {Text("0")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        bottomAxisValuesAmountText,
                        {
                            if (it.isEmpty() || it.toUIntOrNull()?.run { this > 1u } == true) {
                                bottomAxisValuesAmountText = it
                            }
                        },
                        Modifier.onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                                    var r = (bottomAxisValuesAmountText.toUIntOrNull()?.toInt() ?: 0)
                                    if (it.key == Key.DirectionUp) {
                                        r += 1
                                    } else {
                                        r = max(r - 1, 1)
                                    }
                                    bottomAxisValuesAmountText = r.toString()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                        label = {Text("Values amount")},
                        placeholder = {Text("1")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        LineChart(
            Modifier.fillMaxWidth().height(300.dp).padding(horizontal = 8.dp),
            chartData,
        )
    }
}