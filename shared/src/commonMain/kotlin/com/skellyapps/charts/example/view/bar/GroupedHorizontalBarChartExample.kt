package com.skellyapps.charts.example.view.bar

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import com.skellyapps.charts.bar.animation.HorizontalBarChartAnimations
import com.skellyapps.charts.bar.graphics.HorizontalBarChartDrawHelper
import com.skellyapps.charts.bar.model.BarChartData
import com.skellyapps.charts.bar.model.HorizontalBarChartData
import com.skellyapps.charts.bar.view.HorizontalBarChart
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.DpCornerRadius
import com.skellyapps.charts.common.model.GridChartData
import com.skellyapps.charts.common.model.Position
import com.skellyapps.charts.example.roundToDecimals
import kotlinx.coroutines.launch
import kotlin.random.Random

private val blueCategory = BarChartData.Category(
    (0..12).map { ChartValueCoordinate(Random.nextDouble(-30.0, 30.0)) }.toMutableList(),
    0,
    BarChartData.Category.Customization(Color.Blue, topRightCornerRadius = DpCornerRadius(5.dp), bottomRightCornerRadius = DpCornerRadius(5.dp)),
)

private val bottomAxis = HorizontalBarChartData.XAxis(
    mutableStateListOf(blueCategory),
    BarChartData.Type.Grouped(3.dp, 10.dp),
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
        Text(index.toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}

private val barHover = BarChartData.BarHover(
    Position.Right,
    true,
    DpOffset.Zero,
    true
) { categoryTag, index ->
    val value = bottomAxis.categories.getOrNull(categoryTag)?.values[index] ?: return@BarHover
    Box(Modifier.background(colors[categoryTag].copy(0.9f), CircleShape).padding(8.dp), Alignment.Center) {
        Text(value.roundToDecimals(1).toString(), color = Color.White)
    }
}

private var currentColor = 0
private val colors = listOf(Color.Blue, Color.Red, Color.Magenta, Color.DarkGray)

private fun generateCategory(): BarChartData.Category {
    ++currentColor
    return BarChartData.Category(
        (0..12).map { ChartValueCoordinate(Random.nextDouble(-30.0, 30.0)) }.toMutableList(),
        currentColor,
        BarChartData.Category.Customization(colors[currentColor], topRightCornerRadius = DpCornerRadius(5.dp), bottomRightCornerRadius = DpCornerRadius(5.dp)),
    )
}

@Composable
fun GroupedHorizontalBarChartExample() {
    val valuesCount by retain(bottomAxis.categories) {
        derivedStateOf {
            bottomAxis.categories.sumOf { it.values.size }
        }
    }
    val textMeasurer = rememberTextMeasurer(valuesCount)
    var chartData by retain {
        mutableStateOf(
            HorizontalBarChartData(
                bottomAxis,
                true,
                yAxis,
            ),
            referentialEqualityPolicy()
        )
    }
    var addCategoryEnabled by retain { mutableStateOf(true) }
    var removeCategoryEnabled by retain { mutableStateOf(true) }
    LaunchedEffect(bottomAxis.categories.size) {
        addCategoryEnabled = bottomAxis.categories.size < colors.size
        removeCategoryEnabled = bottomAxis.categories.size > 1
    }
    val animations = retain { HorizontalBarChartAnimations(
        HorizontalBarChartAnimations.Growth(tween(3000), 1f)
    ) }
    val scope = rememberCoroutineScope()
    val textLayouts by retain(bottomAxis.categories) {
        derivedStateOf {
            bottomAxis.categories.fastMap {
                it.values.fastMap { value ->
                    val text = value.roundToDecimals(1).toString()
                    textMeasurer.measure(text)
                }
            }
        }
    }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            Arrangement.spacedBy(8.dp),
            Alignment.Bottom
        ) {
            Button({
                bottomAxis.categories.add(generateCategory())
            }, enabled = addCategoryEnabled) {
                Text("Add category")
            }
            Button({
                --currentColor
                bottomAxis.categories.removeLast()
            }, enabled = removeCategoryEnabled) {
                Text("Remove category")
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
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalBarChart(
            Modifier.fillMaxWidth().height(600.dp).padding(start = 8.dp, end = 24.dp),
            chartData,
            animations = animations,
            barHover = barHover
        ) { categoryTag, index, barRect, isNegative ->
            val textLayout = textLayouts[categoryTag][index]
            HorizontalBarChartDrawHelper.drawTextOutside(
                textLayout,
                barRect,
                true,
                isNegative,
                true
            )
        }
    }
}