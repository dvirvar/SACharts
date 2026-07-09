@file:OptIn(ExperimentalMaterial3Api::class)

package com.skellyapps.charts.example.view.pie

import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import com.skellyapps.charts.example.arrowValueStepper
import com.skellyapps.charts.example.roundToDecimals
import com.skellyapps.charts.pie.model.DynamicPieChartData
import com.skellyapps.charts.pie.model.PieChartData
import com.skellyapps.charts.pie.view.DynamicPieChart
import kotlin.random.Random

private val labels = listOf("Blue", "Red", "Black", "Magenta", "Yellow", "Green", "Cyan")
private val slices = mutableStateListOf(PieChartData.Slice(20.0, Color.Blue, 0, labels.first()))

private var currentColor = 0
private val colors = listOf(Color.Blue, Color.Red, Color.Black, Color.Magenta, Color.Yellow, Color.Green, Color.Cyan)

private fun generateSlice(): PieChartData.Slice {
    ++currentColor
    return PieChartData.Slice(
        Random.nextDouble(10.0, 30.0),
        colors[currentColor],
        currentColor,
        labels[currentColor]
    )
}

@Composable
fun DynamicLabelsPieChartExample() {
    val textMeasurer = rememberTextMeasurer()
    var chartData by retain {
        mutableStateOf(
            DynamicPieChartData(
                slices,
                sliceSpacingDegrees = 10f,
                innerRadiusPercentage = 0.5f,
                sliceBorder = PieChartData.Slice.Border(2.dp, Color.Black),
                labelCustomization = PieChartData.LabelCustomization(
                    textMeasurer,
                    Color.Black,
                    2.dp,
                    2.dp,
                    PieChartData.LineCustomization(
                        6.dp,
                        16.dp,
                        Color.DarkGray,
                        2.dp,
                        StrokeJoin.Round
                    )
                )
            ),
            referentialEqualityPolicy()
        )
    }
    var addLineEnabled by retain { mutableStateOf(true) }
    var removeLineEnabled by retain { mutableStateOf(true) }
    LaunchedEffect(slices.size) {
        addLineEnabled = slices.size < colors.size
        removeLineEnabled = slices.size > 1
    }
    var startAngle by retain { mutableFloatStateOf(chartData.startAngle) }
    LaunchedEffect(startAngle) {
        chartData = chartData.copy(startAngle = startAngle)
    }
    var outerRadiusMinPercentage by retain { mutableFloatStateOf(chartData.outerRadiusMinPercentage) }
    LaunchedEffect(outerRadiusMinPercentage) {
        chartData = chartData.copy(outerRadiusMinPercentage = outerRadiusMinPercentage)
    }
    var innerRadiusPercentage by retain { mutableFloatStateOf(chartData.innerRadiusPercentage) }
    LaunchedEffect(innerRadiusPercentage) {
        chartData = chartData.copy(innerRadiusPercentage = innerRadiusPercentage)
    }
    var showSliceBorder by retain { mutableStateOf(true) }
    LaunchedEffect(showSliceBorder) {
        val sliceBorder = if (showSliceBorder) {
            PieChartData.Slice.Border(2.dp, Color.Black)
        } else {
            null
        }
        chartData = chartData.copy(sliceBorder = sliceBorder)
    }
    var edgePaddingText by retain { mutableStateOf("2") }
    LaunchedEffect(edgePaddingText) {
        val edgePadding = (edgePaddingText.toFloatOrNull()?.dp ?: 2.dp)
        val lc = chartData.labelCustomization!!.copy(edgePadding = edgePadding)
        chartData = chartData.copy(labelCustomization = lc)
    }
    var textToLinePaddingText by retain { mutableStateOf("2") }
    LaunchedEffect(textToLinePaddingText) {
        val linePadding = (textToLinePaddingText.toFloatOrNull()?.dp ?: 2.dp)
        val lc = chartData.labelCustomization!!.copy(linePadding = linePadding)
        chartData = chartData.copy(labelCustomization = lc)
    }
    var lineThicknessText by retain { mutableStateOf("2") }
    LaunchedEffect(lineThicknessText) {
        val lineThickness = (lineThicknessText.toFloatOrNull()?.dp ?: 2.dp)
        val lc = chartData.labelCustomization!!.lineCustomization.copy(thickness = lineThickness)
        chartData = chartData.copy(labelCustomization = chartData.labelCustomization?.copy(lineCustomization = lc))
    }
    var extensionLineMaxLengthText by retain { mutableStateOf("16") }
    LaunchedEffect(extensionLineMaxLengthText) {
        val extensionLineMaxLength = (extensionLineMaxLengthText.toFloatOrNull()?.dp ?: 16.dp)
        val lc = chartData.labelCustomization!!.lineCustomization.copy(extensionMaxLength = extensionLineMaxLength)
        chartData = chartData.copy(labelCustomization = chartData.labelCustomization?.copy(lineCustomization = lc))
    }
    var shoulderLineLengthText by retain { mutableStateOf("6") }
    LaunchedEffect(shoulderLineLengthText) {
        val shoulderLineLength = (shoulderLineLengthText.toFloatOrNull()?.dp ?: 6.dp)
        val lc = chartData.labelCustomization!!.lineCustomization.copy(shoulderLength = shoulderLineLength)
        chartData = chartData.copy(labelCustomization = chartData.labelCustomization?.copy(lineCustomization = lc))
    }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
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
                Text("Outer radius min percentage: ${(outerRadiusMinPercentage * 100f).fastRoundToInt()}")
                Spacer(Modifier.weight(1f))
                Slider(
                    outerRadiusMinPercentage,
                    {outerRadiusMinPercentage = it},
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
            Row(Modifier.toggleable(showSliceBorder, role = Role.Checkbox) { showSliceBorder = it }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(showSliceBorder, null)
                Text("Show slide border")
            }
            Column {
                Text("Label customization", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    OutlinedTextField(
                        edgePaddingText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                edgePaddingText = it
                            }
                        },
                        Modifier.arrowValueStepper(edgePaddingText, 2f) {
                            edgePaddingText = it
                        },
                        label = {Text("Edge padding(dp)")},
                        placeholder = {Text("2")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        textToLinePaddingText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                textToLinePaddingText = it
                            }
                        },
                        Modifier.arrowValueStepper(textToLinePaddingText, 2f) {
                            textToLinePaddingText = it
                        },
                        label = {Text("Text to line padding(dp)")},
                        placeholder = {Text("2")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        lineThicknessText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                lineThicknessText = it
                            }
                        },
                        Modifier.arrowValueStepper(lineThicknessText, 2f) {
                            lineThicknessText = it
                        },
                        label = {Text("Line thickness(dp)")},
                        placeholder = {Text("2")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        extensionLineMaxLengthText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                extensionLineMaxLengthText = it
                            }
                        },
                        Modifier.arrowValueStepper(extensionLineMaxLengthText, 16f) {
                            extensionLineMaxLengthText = it
                        },
                        label = {Text("Extension line max length(dp)")},
                        placeholder = {Text("16")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        shoulderLineLengthText,
                        {
                            if (it.isEmpty() || it.toFloatOrNull() != null) {
                                shoulderLineLengthText = it
                            }
                        },
                        Modifier.arrowValueStepper(shoulderLineLengthText, 6f) {
                            shoulderLineLengthText = it
                        },
                        label = {Text("Shoulder line length(dp)")},
                        placeholder = {Text("6")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        DynamicPieChart(
            Modifier.size(300.dp).align(Alignment.CenterHorizontally).border(1.dp, Color.Black),
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