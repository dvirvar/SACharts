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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.common.model.Position
import com.skellyapps.charts.common.model.Zoom
import com.skellyapps.charts.example.roundToDecimals
import com.skellyapps.charts.line.animation.LineChartAnimations
import com.skellyapps.charts.line.model.LineChartData
import com.skellyapps.charts.line.view.LineChart
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private val blueLine = LineChartData.Line(
    (0..12).map { ChartValue(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableList(),
    LineChartData.Line.PointsOrder.Ordered.X,
    0,
    LineChartData.Line.Customization(Color.Blue, join = StrokeJoin.Round)
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
private val bottomAxis = LineChartData.XAxis(
    value = GridChartData.Axis.Value.Fixed(8),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)) { value ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}

@Composable
fun LimitLinesLineChartExample() {
    val textMeasurer = rememberTextMeasurer()
    val highTextLayout = textMeasurer.measure("High")
    val mediumTextLayout = textMeasurer.measure("Medium")
    val lowTextLayout = textMeasurer.measure("Low")
    val chartData = retain {
        LineChartData(
            leftAxis = leftAxis,
            bottomAxis = bottomAxis,
        )
    }
    var zoom by retain { mutableStateOf<Zoom?>(null) }
    val animations = retain { LineChartAnimations(
        LineChartAnimations.Growth(tween(2000), 1f),
        LineChartAnimations.Reveal(tween(2000), 1f)
    ) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            Arrangement.spacedBy(8.dp),
            Alignment.Bottom
        ) {
            Row(Modifier.toggleable(zoom != null, role = Role.Checkbox) { zoom = if (zoom == null) Zoom(0.2f, 6f) else null }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(zoom != null, null)
                Text("Enable zoom")
            }
            Column {
                Text("Animations", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
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
            Modifier.fillMaxWidth().height(300.dp).padding(start = 8.dp, end = 24.dp),
            chartData,
            zoom = zoom,
            animations = animations,
            drawOnChart = { drawHelper ->
                val highOffsetStart = with(drawHelper) { ChartValue(0.0, 60.0).toOffset(true) }
                if (highOffsetStart.y >= 0f && highOffsetStart.y <= size.height) {
                    val highOffsetEnd = Offset(size.width, highOffsetStart.y)
                    drawHelper.drawText(highTextLayout, highOffsetEnd, Position.TopLeft, true)
                    drawLine(Color.Red, highOffsetStart, highOffsetEnd)
                }
                val lowOffsetStart = with(drawHelper) { ChartValue(0.0, 20.0).toOffset(true) }
                if (lowOffsetStart.y >= 0f && lowOffsetStart.y <= size.height) {
                    val lowOffsetEnd = Offset(size.width, lowOffsetStart.y)
                    drawHelper.drawText(lowTextLayout, lowOffsetEnd, Position.BottomLeft, true)
                    drawLine(Color.Blue, lowOffsetStart, lowOffsetEnd)
                }

                var mediumYStart = with(drawHelper) { ChartValueCoordinate(50.0).toYPixel(true) }
                var mediumYEnd = with(drawHelper) { ChartValueCoordinate(30.0).toYPixel(true) }
                if (mediumYStart <= size.height && mediumYEnd >= 0f) {
                    mediumYStart = max(0f, mediumYStart)
                    mediumYEnd = min(size.height, mediumYEnd)
                    drawRect(Color.Yellow.copy(0.7f), Offset(0f, mediumYStart), Size(size.width, mediumYEnd - mediumYStart))
                    drawHelper.drawText(mediumTextLayout, Offset(size.width, (mediumYEnd + mediumYStart) / 2f), Position.Left, true)
                }
            }
        )
    }
}