@file:OptIn(ExperimentalMaterial3Api::class)

package com.skellyapps.charts.example.view.pie

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import com.skellyapps.charts.example.arrowValueStepper
import com.skellyapps.charts.example.roundToDecimals
import com.skellyapps.charts.pie.model.PieChartData
import com.skellyapps.charts.pie.view.PieChart
import kotlin.random.Random

private val slices = mutableStateListOf(PieChartData.Slice(20.0, Color.Blue, 0))

private var currentColor = 0
private val colors = listOf(Color.Blue, Color.Red, Color.Black, Color.Magenta, Color.Yellow, Color.Green, Color.Cyan)

private var sliceBorder = PieChartData.Slice.Border(2.dp, Color.Black)

private fun generateSlice(): PieChartData.Slice {
    ++currentColor
    return PieChartData.Slice(
        Random.nextDouble(10.0, 30.0),
        colors[currentColor],
        currentColor
    )
}

@Composable
fun SimplePieChartExample() {
    val textMeasurer = rememberTextMeasurer()
    var chartData by retain {
        mutableStateOf(
            PieChartData(
                slices,
                sliceSpacingDegrees = 10f,
                innerRadiusPercentage = 0.5f,
                sliceBorder = sliceBorder,
            ),
            referentialEqualityPolicy()
        )
    }
    var addSliceEnabled by retain { mutableStateOf(true) }
    var removeSliceEnabled by retain { mutableStateOf(true) }
    LaunchedEffect(slices.size) {
        addSliceEnabled = slices.size < colors.size
        removeSliceEnabled = slices.size > 1
    }
    var startAngle by retain { mutableFloatStateOf(chartData.startAngle) }
    LaunchedEffect(startAngle) {
        chartData = chartData.copy(startAngle = startAngle)
    }
    var outerRadiusPercentage by retain { mutableFloatStateOf(chartData.outerRadiusPercentage) }
    LaunchedEffect(outerRadiusPercentage) {
        chartData = chartData.copy(outerRadiusPercentage = outerRadiusPercentage)
    }
    var innerRadiusPercentage by retain { mutableFloatStateOf(chartData.innerRadiusPercentage) }
    LaunchedEffect(innerRadiusPercentage) {
        chartData = chartData.copy(innerRadiusPercentage = innerRadiusPercentage)
    }
    var sliceSpacingDegreesText by retain { mutableStateOf("10") }
    LaunchedEffect(sliceSpacingDegreesText) {
        val sliceSpacingDegrees = sliceSpacingDegreesText.toFloatOrNull() ?: 10f
        chartData = chartData.copy(sliceSpacingDegrees = sliceSpacingDegrees)
    }
    var showSliceBorder by retain { mutableStateOf(true) }
    LaunchedEffect(showSliceBorder) {
        val sliceBorder = if (showSliceBorder) {
            sliceBorder
        } else {
            sliceBorder = chartData.sliceBorder!!
            null
        }
        chartData = chartData.copy(sliceBorder = sliceBorder)
    }
    var sliceBorderThicknessText by retain { mutableStateOf("2") }
    LaunchedEffect(sliceBorderThicknessText) {
        val thickness = (sliceBorderThicknessText.toFloatOrNull()?.dp ?: 2.dp)
        sliceBorder = PieChartData.Slice.Border(thickness, Color.Black)
        chartData = chartData.copy(sliceBorder = sliceBorder)
    }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Button({
                slices.add(generateSlice())
            }, enabled = addSliceEnabled) {
                Text("Add slice")
            }
            Button({
                --currentColor
                slices.removeLast()
            }, enabled = removeSliceEnabled) {
                Text("Remove slice")
            }
            Column {
                Text("Start angle: ${startAngle.fastRoundToInt()}")
                Spacer(Modifier.weight(1f))
                Slider(
                    startAngle,
                    {startAngle = it},
                    Modifier.width(250.dp),
                    valueRange = 0f..359f,
                    steps = 360
                )
            }
            Column {
                Text("Outer radius percentage: ${(outerRadiusPercentage * 100f).fastRoundToInt()}")
                Spacer(Modifier.weight(1f))
                Slider(
                    outerRadiusPercentage,
                    {outerRadiusPercentage = it},
                    Modifier.width(250.dp),
                    valueRange = 0.01f..1f
                )
            }
            Column {
                Text("Inner radius percentage: ${(innerRadiusPercentage * 100f).fastRoundToInt()}")
                Spacer(Modifier.weight(1f))
                Slider(
                    innerRadiusPercentage,
                    {innerRadiusPercentage = it},
                    Modifier.width(250.dp),
                    valueRange = 0f..0.99f
                )
            }
            OutlinedTextField(
                sliceSpacingDegreesText,
                {
                    if (it.isEmpty() || it.toFloatOrNull() != null) {
                        sliceSpacingDegreesText = it
                    }
                },
                Modifier.arrowValueStepper(sliceSpacingDegreesText, 10f) {
                    sliceSpacingDegreesText = it
                },
                label = {Text("Slice spacing(degrees)")},
                placeholder = {Text("10")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            Column {
                Text("Slice border", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Row(Modifier.toggleable(showSliceBorder, role = Role.Checkbox) { showSliceBorder = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(showSliceBorder, null)
                        Text("Show")
                    }
                    if (showSliceBorder) {
                        OutlinedTextField(
                            sliceBorderThicknessText,
                            {
                                if (it.isEmpty() || it.toFloatOrNull() != null) {
                                    sliceBorderThicknessText = it
                                }
                            },
                            Modifier.arrowValueStepper(sliceBorderThicknessText, 2f) {
                                sliceBorderThicknessText = it
                            },
                            label = {Text("Thickness(dp)")},
                            placeholder = {Text("2")},
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        PieChart(
            Modifier.size(300.dp).align(Alignment.CenterHorizontally),
            chartData,
            { sliceTag: Int, centerX: Float, centerY: Float, outerRadius: Float, innerRadius: Float, middleRad: Double ->
                val layout = textMeasurer.measure(
                    slices[sliceTag].value.roundToDecimals(1).toString()
                )
                val textColor = if (slices.size == 1) {
                    Color(255,165,0)
                } else {
                    if (sliceTag <=3) Color.White else Color.Black
                }
                drawTextInMiddle(
                    layout,
                    centerX,
                    centerY,
                    outerRadius,
                    innerRadius,
                    middleRad,
                    slices.size > 1,
                    textColor
                )
            }
        )
    }
}