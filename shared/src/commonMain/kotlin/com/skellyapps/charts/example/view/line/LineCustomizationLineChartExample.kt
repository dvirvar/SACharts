package com.skellyapps.charts.example.view.line

import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.example.arrowValueStepper
import com.skellyapps.charts.example.roundToDecimals
import com.skellyapps.charts.line.animation.LineChartAnimations
import com.skellyapps.charts.line.model.LineChartData
import com.skellyapps.charts.line.view.LineChart
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

private const val blueTag = 0
private const val redTag = 1

private val colors = listOf(Color.Blue, Color.Red)

private var blueLine = LineChartData.Line(
    (0..12).map { ChartValue(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    blueTag,
    LineChartData.Line.Customization(colors[blueTag], join = StrokeJoin.Round, pathEffect = PathEffect.cornerPathEffect(3f)),
    LineChartData.Line.FillCustomization(colors[blueTag], 0.25f)
)
private val leftAxis = LineChartData.YAxis(
    lines = mutableListOf(blueLine),
    value = GridChartData.Axis.Value.Step(20.0),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value.roundToDecimals(1).toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}
private var redLine = LineChartData.Line(
    (0..15).map { ChartValue(it * 8.5, Random.nextDouble(0.0, 150.0)) }.toMutableList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    redTag,
    LineChartData.Line.Customization(colors[redTag], join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f))),
    LineChartData.Line.FillCustomization(colors[redTag], 0.25f)
)
private val rightAxis = LineChartData.YAxis(
    lines = mutableListOf(redLine),
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

private val lines = listOf(blueLine, redLine)

@Composable
fun LineCustomizationLineChartExample() {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer(blueLine.points.size + redLine.points.size)
    var chartData by retain {
        mutableStateOf(
            LineChartData(
                leftAxis,
                rightAxis,
                bottomAxis,
            ),
            referentialEqualityPolicy()
        )
    }
    var showValues by retain { mutableStateOf(true) }
    var showPoints by retain { mutableStateOf(true) }
    var pointsRadiusText by retain { mutableStateOf("5") }
    val pointsRadius by retain(pointsRadiusText) {
        mutableFloatStateOf(
            with(density) {
                (pointsRadiusText.toFloatOrNull()?.dp ?: 0.dp).toPx()
            }
        )
    }
    var blueLineCornerRadiusText by retain { mutableStateOf("5") }
    LaunchedEffect(blueLineCornerRadiusText) {
        val radius = with(density) {
            (blueLineCornerRadiusText.toFloatOrNull()?.dp ?: 1.dp).toPx()
        }
        try {
            chartData = chartData.copy(chartData.leftAxis?.copy(mutableListOf(blueLine.copy(customization = blueLine.customization.copy(pathEffect = PathEffect.cornerPathEffect(radius))))))
        } catch (e: Exception) {
            println(e)
        }
    }
    var showBlueLineFill by retain { mutableStateOf(true) }
    LaunchedEffect(showBlueLineFill) {
        val fillCustomization = if(showBlueLineFill) {
            LineChartData.Line.FillCustomization(colors[blueTag], 0.25f)
        } else {
            null
        }
        blueLine = blueLine.copy(fillCustomization = fillCustomization)
        chartData = chartData.copy(chartData.leftAxis?.copy(mutableListOf(blueLine)))
    }
    var redLineDashLengthText by retain { mutableStateOf("10") }
    var redLineDashSpaceLengthText by retain { mutableStateOf("15") }
    LaunchedEffect(redLineDashLengthText, redLineDashSpaceLengthText) {
        val length = with(density) {
            (redLineDashLengthText.toFloatOrNull()?.dp ?: 10.dp).toPx()
        }
        val spaceLength = with(density) {
            (redLineDashSpaceLengthText.toFloatOrNull()?.dp ?: 15.dp).toPx()
        }
        try {
            redLine = redLine.copy(customization = redLine.customization.copy(pathEffect = PathEffect.dashPathEffect(floatArrayOf(length, spaceLength))))
            chartData = chartData.copy(rightAxis = chartData.rightAxis?.copy(mutableListOf(redLine)))
        } catch (e: Exception) {
            println(e)
        }
    }
    var showRedLineFill by retain { mutableStateOf(true) }
    LaunchedEffect(showRedLineFill) {
        val fillCustomization = if(showRedLineFill) {
            LineChartData.Line.FillCustomization(colors[redTag], 0.25f)
        } else {
            null
        }
        redLine = redLine.copy(fillCustomization = fillCustomization)
        chartData = chartData.copy(rightAxis = chartData.rightAxis?.copy(mutableListOf(redLine)))
    }
    var animateDrawOnPoints by retain { mutableStateOf(true) }
    val animations = retain { LineChartAnimations(
        LineChartAnimations.Growth(tween(2000), 1f),
        LineChartAnimations.Reveal(tween(2000), 1f)
    ) }
    val scope = rememberCoroutineScope()
    val textLayouts = retain {
        lines.fastMap {
            it.points.fastMap { point ->
                val xValue = point.x.roundToDecimals(1)
                val yValue = point.y.roundToDecimals(1)
                val text = "X:$xValue\nY:$yValue"
                textMeasurer.measure(text)
            }
        }
    }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            Arrangement.spacedBy(8.dp),
            Alignment.Bottom
        ) {
            Column {
                Text("General", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(showValues, role = Role.Checkbox) { showValues = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showValues, null)
                        Text("Show values")
                    }
                    Row(Modifier.toggleable(showPoints, role = Role.Checkbox) { showPoints = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showPoints, null)
                        Text("Show points")
                    }
                    if (showPoints) {
                        OutlinedTextField(
                            pointsRadiusText,
                            {
                                if (it.isEmpty() || it.toFloatOrNull() != null) {
                                    pointsRadiusText = it
                                }
                            },
                            Modifier.arrowValueStepper(pointsRadiusText, 0f) {
                                pointsRadiusText = it
                            },
                            label = {Text("Points radius(dp)")},
                            placeholder = {Text("0")},
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
            }
            Column {
                Text("Blue line", Modifier, style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.Bottom) {
                    OutlinedTextField(
                        blueLineCornerRadiusText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull()?.run { this >= 1f } == true) {
                                blueLineCornerRadiusText = it
                            }
                        },
                        Modifier.arrowValueStepper(blueLineCornerRadiusText, 0f, 1f) {
                            blueLineCornerRadiusText = it
                        },
                        label = {Text("Corner radius(dp)")},
                        placeholder = {Text("1")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Row(Modifier.toggleable(showBlueLineFill, role = Role.Checkbox) { showBlueLineFill = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showBlueLineFill, null)
                        Text("Show line fill")
                    }
                }
            }
            Column {
                Text("Red line", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.Bottom) {
                    OutlinedTextField(
                        redLineDashLengthText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                redLineDashLengthText = it
                            }
                        },
                        Modifier.arrowValueStepper(redLineDashLengthText, 10f) {
                            redLineDashLengthText = it
                        },
                        label = {Text("Dash length(dp)")},
                        placeholder = {Text("10")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        redLineDashSpaceLengthText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                redLineDashSpaceLengthText = it
                            }
                        },
                        Modifier.arrowValueStepper(redLineDashSpaceLengthText, 15f) {
                            redLineDashSpaceLengthText = it
                        },
                        label = {Text("Dash space length(dp)")},
                        placeholder = {Text("15")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Row(Modifier.toggleable(showRedLineFill, role = Role.Checkbox) { showRedLineFill = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showRedLineFill, null)
                        Text("Show line fill")
                    }
                }
            }
            Column {
                Text("Animations", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(animateDrawOnPoints, role = Role.Checkbox) { animateDrawOnPoints = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(animateDrawOnPoints, null)
                        Text("Animate draw on points")
                    }
                    Button({
                        scope.launch {
                            animations.growth!!.snapTo(0f)
                            animations.growth!!.animate()
                        }
                    }, enabled = !animations.growth!!.isRunning) {
                        Text("Growth")
                    }
                    Button({
                        scope.launch {
                            animations.reveal!!.snapTo(0f)
                            animations.reveal!!.animate()
                        }
                    }, enabled = !animations.reveal!!.isRunning) {
                        Text("Reveal")
                    }
                    Button(
                        {
                            scope.launch {
                                animations.growth!!.snapTo(0f)
                                animations.growth!!.animate()
                            }
                            scope.launch {
                                animations.reveal!!.snapTo(0f)
                                animations.reveal!!.animate()
                            }
                        }, enabled = !animations.growth!!.isRunning && !animations.reveal!!.isRunning
                    ) {
                        Text("All")
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        LineChart(
            Modifier.fillMaxWidth().height(300.dp).padding(start = 8.dp),
            chartData,
            animations = animations,
        ) { drawHelper, lineTag, index, offset, animatedYPixel ->
            val offset = if (animateDrawOnPoints) offset.copy(y = animatedYPixel) else offset
            if (showPoints) {
                if (lineTag == blueTag) {
                    drawCircle(
                        colors[lineTag],
                        pointsRadius,
                        offset
                    )
                } else {
                    drawRect(
                        colors[lineTag],
                        offset - Offset(pointsRadius, pointsRadius),
                        Size(pointsRadius * 2f, pointsRadius * 2f)
                    )
                }
            }
            if (showValues) {
                val textLayout = textLayouts[lineTag][index]
                val x = offset.x.coerceIn(textLayout.size.width / 2f, size.width - textLayout.size.width / 2f) - textLayout.size.width / 2f
                val topLeftOffset = Offset(x, max(offset.y - textLayout.size.height, 0f))
                drawText(
                    textLayout,
                    topLeft = topLeftOffset
                )
            }
        }
    }
}