package com.skellyapps.charts.bar.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrDefault
import androidx.compose.ui.zIndex
import com.skellyapps.charts.bar.model.BarChartData
import com.skellyapps.charts.common.model.ChartPixel
import com.skellyapps.charts.common.model.ChartPixelCoordinate
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.view.AxisColumn
import com.skellyapps.charts.common.view.AxisRow
import com.skellyapps.charts.common.view.GridChartCanvas
import com.skellyapps.charts.common.view.dividersZIndex
import kotlin.math.abs

private const val axesZIndex = -1f
internal const val barsZIndex = dividersZIndex - 5f

@Composable
fun BarChart(
    modifier: Modifier,
    data: BarChartData,
    background: Brush = SolidColor(Color.Transparent),
    drawOnEachValue: (DrawScope.(canvasSize: Size, categoryTag: Int, index: Int, topLeft: Offset, barSize: Size) -> Unit)? = null
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize(0,0)) }
    var canvasZoom by remember { mutableStateOf(Offset(1f,1f)) }
    var canvasOffset by remember { mutableStateOf(Offset.Zero) }
    val yAxisOffset = Offset.Zero
    val xAxisOffset by remember(data.xAxisOffset) {
        derivedStateOf {
            with(density) {
                Offset(data.xAxisOffset.x.toPx(), data.xAxisOffset.y.toPx())
            }
        }
    }
    val minXValue = ChartValueCoordinate(0.0)
    val maxXValue by remember(data.yAxis.categories) {
        derivedStateOf {
            ChartValueCoordinate(
                data.yAxis.categories.fastFilter { it.values.isNotEmpty() }.maxOfOrNull { it.values.size }?.toDouble() ?: 0.0
            )
        }
    }
    val xAxisViewport by remember(xAxisOffset, maxXValue) {
        derivedStateOf {
            val x = ChartPixelCoordinate(canvasOffset.x).toChartValueCoordinate(canvasSize.width, xAxisOffset, minXValue, maxXValue, false)
            val maxX = ChartPixelCoordinate(canvasOffset.x + canvasSize.width.toFloat() / canvasZoom.x).toChartValueCoordinate(canvasSize.width, xAxisOffset, minXValue, maxXValue, false)
            ChartValue(x, maxX)
        }
    }
    val yAxisMinValue by remember(data.yAxis.minValue) {
        derivedStateOf {
            ChartValueCoordinate(data.yAxis.minValue ?: 0.0)
        }
    }
    val yAxisMaxValue by remember(data.yAxis.maxValue, data.yAxis.categories, data.yAxis.type) {
        derivedStateOf {
            ChartValueCoordinate(
                if (data.yAxis.maxValue != null) {
                    data.yAxis.maxValue
                } else {
                    val max = when (data.yAxis.type) {
                        is BarChartData.Type.Grouped -> data.yAxis.categories.fastFilter { it.values.isNotEmpty() }.maxOfOrNull { it.values.max() }?.value ?: 1.0
                        is BarChartData.Type.Stacked -> {
                            val values = mutableListOf<ChartValueCoordinate>()
                            data.yAxis.categories.fastForEach {
                                it.values.fastForEachIndexed { index, value ->
                                    if (values.lastIndex < index) {
                                        values.add(value)
                                    } else {
                                        values[index] += value
                                    }
                                }
                            }
                            values.fastMaxOfOrDefault(1.0) { it.value }
                        }
                    }
                    if (max == yAxisMinValue.value) {
                        max + 1.0
                    } else {
                        max
                    }
                }
            )
        }
    }
    val yAxisViewport by remember(yAxisMinValue, yAxisMaxValue) {
        derivedStateOf {
            val y = ChartPixelCoordinate(canvasOffset.y + canvasSize.height.toFloat() / canvasZoom.y).toChartValueCoordinate(canvasSize.height, yAxisOffset, yAxisMinValue, yAxisMaxValue, true)
            val maxY = ChartPixelCoordinate(canvasOffset.y).toChartValueCoordinate(canvasSize.height, yAxisOffset, yAxisMinValue, yAxisMaxValue, true)
            ChartValue(y, maxY)
        }
    }
    val yAxisValues by remember(yAxisMinValue, yAxisMaxValue, data.yAxis.value) {
        derivedStateOf {
            data.yAxis.value.getValues(yAxisMinValue, yAxisMaxValue)
        }
    }
    val categoriesSpace by remember(data.yAxis.type.categoriesSpace) {
        derivedStateOf {
            with(density) {
                data.yAxis.type.categoriesSpace.toPx()
            }
        }
    }
    val categoryWidth by remember(canvasSize.width, maxXValue, xAxisOffset, data.yAxis.type) {
        derivedStateOf {
            val maxValue = maxXValue.value.toFloat()
            (canvasSize.width.toFloat() - xAxisOffset.x - xAxisOffset.y - ((maxValue - 1f) * categoriesSpace)) / maxValue
        }
    }
    val bottomAxisValues: List<ChartValueCoordinate> by remember(canvasSize.width, maxXValue, xAxisOffset, xAxisViewport) {
        derivedStateOf {
            if (canvasSize.width == 0) {
                listOf()
            } else {
                val values = mutableListOf<ChartValueCoordinate>()
                val offset = ChartPixelCoordinate(categoryWidth / 2f).toChartValueCoordinate(canvasSize.width, xAxisOffset, xAxisViewport.x, xAxisViewport.y, false)
                val categoriesOffset = ChartPixelCoordinate(categoryWidth + categoriesSpace).toChartValueCoordinate(canvasSize.width, xAxisOffset, xAxisViewport.x, xAxisViewport.y, false).value
                for (i in minXValue.value.toInt()..maxXValue.value.toInt()) {
                    values.add(
                        ChartValueCoordinate(offset.value + (categoriesOffset * i))
                    )
                }
                values
            }
        }
    }
    val barWidth by remember(categoryWidth, data.yAxis.categories, data.yAxis.type) {
        derivedStateOf {
            val type = data.yAxis.type
            when (type) {
                is BarChartData.Type.Grouped -> {
                    val barsSpace = with(density) {
                        type.barsSpace.toPx()
                    }
                    (categoryWidth - ((data.yAxis.categories.size - 1) * barsSpace)) / data.yAxis.categories.size
                }
                is BarChartData.Type.Stacked -> categoryWidth
            }
        }
    }
    val offsetCategories by remember(data.yAxis.categories, barWidth, yAxisViewport) {
        derivedStateOf {
            val offsetCategories = mutableListOf<BarChartData.OffsetCategory>()
            if (data.yAxis.categories.isNotEmpty()) {
                val baseValueYPixel = ChartValueCoordinate(0.0).toChartPixelCoordinate(canvasSize.height, yAxisOffset, yAxisViewport.x, yAxisViewport.y, true).value
                when(data.yAxis.type) {
                    is BarChartData.Type.Grouped -> {
                        val barsSpace = with(density) {
                            data.yAxis.type.barsSpace.toPx()
                        }
                        for (categoryIndex in 0..<data.yAxis.categories.size) {
                            val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                            val baseXPixel = xAxisOffset.x + ((barWidth + barsSpace) * categoryIndex)
                            data.yAxis.categories[categoryIndex].values.fastForEachIndexed { index, value ->
                                val xPixel = baseXPixel + ((categoryWidth + categoriesSpace) * index)
                                val valueYPixel = value.toChartPixelCoordinate(canvasSize.height, yAxisOffset, yAxisViewport.x, yAxisViewport.y, true).value
                                val isNegative = value.value < 0.0
                                val yPixel = if (isNegative) {
                                    baseValueYPixel
                                } else {
                                    valueYPixel
                                }
                                offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, yPixel)), Size(barWidth, abs(baseValueYPixel - valueYPixel)), isNegative))
                            }
                            offsetCategories.add(BarChartData.OffsetCategory(offsets, data.yAxis.categories[categoryIndex].tag, data.yAxis.categories[categoryIndex].customization))
                        }
                    }
                    is BarChartData.Type.Stacked -> {
                        val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                        val lastValues = data.yAxis.categories.first().values.toMutableList()
                        lastValues.fastForEachIndexed { index, value ->
                            val xPixel = xAxisOffset.x + ((categoryWidth + categoriesSpace) * index)
                            val valueYPixel = value.toChartPixelCoordinate(canvasSize.height, yAxisOffset, yAxisViewport.x, yAxisViewport.y, true).value
                            offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, valueYPixel)), Size(barWidth, baseValueYPixel - valueYPixel), false))
                        }
                        offsetCategories.add(BarChartData.OffsetCategory(offsets, data.yAxis.categories[0].tag, data.yAxis.categories[0].customization))
                        for (categoryIndex in 1..<data.yAxis.categories.size) {
                            val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                            data.yAxis.categories[categoryIndex].values.fastForEachIndexed { index, value ->
                                val lastValue = lastValues.getOrElse(index, { ChartValueCoordinate(0.0)})
                                val currentValue = lastValue + value
                                if (lastValues.lastIndex < index) {
                                    lastValues.add(currentValue)
                                } else {
                                    lastValues[index] = currentValue
                                }
                                val xPixel = xAxisOffset.x + ((categoryWidth + categoriesSpace) * index)
                                val currentValueYPixel = currentValue.toChartPixelCoordinate(canvasSize.height, yAxisOffset, yAxisViewport.x, yAxisViewport.y, true).value
                                val lastValueYPixel = lastValue.toChartPixelCoordinate(canvasSize.height, yAxisOffset, yAxisViewport.x, yAxisViewport.y, true).value
                                offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, currentValueYPixel)), Size(barWidth, lastValueYPixel - currentValueYPixel), false))
                            }
                            offsetCategories.add(BarChartData.OffsetCategory(offsets, data.yAxis.categories[categoryIndex].tag, data.yAxis.categories[categoryIndex].customization))
                        }
                    }
                }
            }
            offsetCategories
        }
    }

    Row(modifier) {
        if (data.isLeftYAxis) {
            data.yAxis.let { axis ->
                axis.valueView?.let {
                    AxisColumn(Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex), true, yAxisValues, yAxisViewport.x, yAxisViewport.y, true) {
                        yAxisValues.fastForEach { value ->
                            it(value.value)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f)) {
            GridChartCanvas(
                Modifier.fillMaxWidth().weight(1f).onSizeChanged {
                    canvasSize = it
                }.drawBehind {
                    drawRect(background)
                },
                if (data.isLeftYAxis) data.yAxis else null,
                if (!data.isLeftYAxis) data.yAxis else null,
                data.bottomAxis,
                if (data.isLeftYAxis) yAxisViewport.x else ChartValueCoordinate(0.0),
                if (data.isLeftYAxis) yAxisViewport.y else ChartValueCoordinate(1.0),
                if (!data.isLeftYAxis) yAxisViewport.x else ChartValueCoordinate(0.0),
                if (!data.isLeftYAxis) yAxisViewport.y else ChartValueCoordinate(1.0),
                xAxisViewport.x,
                xAxisViewport.y,
                if (data.isLeftYAxis) yAxisValues else listOf(),
                if (!data.isLeftYAxis) yAxisValues else listOf(),
                bottomAxisValues
            ) {
                Box(Modifier
                    .fillMaxSize()
                    .zIndex(barsZIndex)
                    .clipToBounds()
                    .drawBehind {
                        val path = Path()
                        offsetCategories.fastForEach { category ->
                            category.offsets.fastForEach {
                                path.addRoundRect(
                                    RoundRect(
                                        Rect(it.topLeft.offset, it.size),
                                        topLeft = if(it.isNegative) category.customization.bottomLeftCornerRadius else category.customization.topLeftCornerRadius,
                                        topRight = if (it.isNegative) category.customization.bottomRightCornerRadius else category.customization.topRightCornerRadius,
                                        bottomRight = if (it.isNegative) category.customization.topRightCornerRadius else category.customization.bottomRightCornerRadius,
                                        bottomLeft = if (it.isNegative) category.customization.topLeftCornerRadius else category.customization.bottomLeftCornerRadius,
                                    )
                                )
                            }
                            drawPath(
                                path,
                                category.customization.brush,
                                category.customization.alpha,
                                Fill,
                                category.customization.colorFilter,
                                category.customization.blendMode
                            )
                            path.reset()
                        }
                        drawOnEachValue?.let {
                            offsetCategories.fastForEach { category ->
                                category.offsets.fastForEachIndexed { index, offsetCategory ->
                                    drawOnEachValue(this, size, category.tag, index, offsetCategory.topLeft.offset, offsetCategory.size)
                                }
                            }
                        }
                    }
                )
            }
            data.bottomAxis?.let { axis ->
                axis.valueView?.let {
                    AxisRow(Modifier.fillMaxWidth().zIndex(axesZIndex), bottomAxisValues, xAxisViewport.x, xAxisViewport.y, false) {
                        for (i in 0..bottomAxisValues.lastIndex) {
                            it(i)
                        }
                    }
                }
            }
        }
        if (!data.isLeftYAxis) {
            data.yAxis.let { axis ->
                axis.valueView?.let {
                    AxisColumn(Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex), false, yAxisValues, yAxisViewport.x, yAxisViewport.y, true) {
                        yAxisValues.fastForEach { value ->
                            it(value.value)
                        }
                    }
                }
            }
        }
    }
}