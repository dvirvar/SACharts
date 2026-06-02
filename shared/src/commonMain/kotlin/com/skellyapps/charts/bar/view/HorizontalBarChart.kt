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
import com.skellyapps.charts.bar.model.HorizontalBarChartData
import com.skellyapps.charts.common.model.ChartPixel
import com.skellyapps.charts.common.model.ChartPixelCoordinate
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.view.AxisColumn
import com.skellyapps.charts.common.view.AxisRow
import com.skellyapps.charts.common.view.GridChartCanvas
import kotlin.math.abs

private const val axesZIndex = -1f

@Composable
fun HorizontalBarChart(
    modifier: Modifier,
    data: HorizontalBarChartData,
    background: Brush = SolidColor(Color.Transparent),
    drawOnEachValue: (DrawScope.(canvasSize: Size, categoryTag: Int, index: Int, topLeft: Offset, barSize: Size) -> Unit)? = null
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize(0,0)) }
    var canvasZoom by remember { mutableStateOf(Offset(1f,1f)) }
    var canvasOffset by remember { mutableStateOf(Offset.Zero) }
    val yAxisOffset by remember(data.yAxis?.offset) {
        derivedStateOf {
            if (data.yAxis == null) {
                Offset.Zero
            } else {
                with(density) {
                    Offset(data.yAxis.offset.x.toPx(), data.yAxis.offset.y.toPx())
                }
            }
        }
    }
    val xAxisOffset = Offset.Zero
    val minXValue by remember(data.bottomAxis.minValue) {
        derivedStateOf {
            ChartValueCoordinate(data.bottomAxis.minValue ?: 0.0)
        }
    }
    val maxXValue by remember(data.bottomAxis.maxValue, data.bottomAxis.categories, data.bottomAxis.type) {
        derivedStateOf {
            ChartValueCoordinate(
                if (data.bottomAxis.maxValue != null) {
                    data.bottomAxis.maxValue
                } else {
                    val max = when (data.bottomAxis.type) {
                        is BarChartData.Type.Grouped -> data.bottomAxis.categories.fastFilter { it.values.isNotEmpty() }.maxOfOrNull { it.values.max() }?.value ?: 1.0
                        is BarChartData.Type.Stacked -> {
                            val values = mutableListOf<ChartValueCoordinate>()
                            data.bottomAxis.categories.fastForEach {
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
                    if (max == minXValue.value) {
                        max + 1.0
                    } else {
                        max
                    }
                }
            )
        }
    }
    val xAxisViewport by remember(maxXValue) {
        derivedStateOf {
            val x = ChartPixelCoordinate(canvasOffset.x).toChartValueCoordinate(canvasSize.width, xAxisOffset, minXValue, maxXValue, false)
            val maxX = ChartPixelCoordinate(canvasOffset.x + canvasSize.width.toFloat() / canvasZoom.x).toChartValueCoordinate(canvasSize.width, xAxisOffset, minXValue, maxXValue, false)
            ChartValue(x, maxX)
        }
    }
    val yAxisMinValue = ChartValueCoordinate(0.0)
    val yAxisMaxValue by remember(data.bottomAxis.categories) {
        derivedStateOf {
            ChartValueCoordinate(
                data.bottomAxis.categories.fastFilter { it.values.isNotEmpty() }.maxOfOrNull { it.values.size }?.toDouble() ?: 0.0
            )
        }
    }
    val yAxisViewport by remember(yAxisOffset, yAxisMinValue, yAxisMaxValue) {
        derivedStateOf {
            val y = ChartPixelCoordinate(canvasOffset.y).toChartValueCoordinate(canvasSize.height, yAxisOffset, yAxisMinValue, yAxisMaxValue, false)
            val maxY = ChartPixelCoordinate(canvasOffset.y + canvasSize.height.toFloat() / canvasZoom.y).toChartValueCoordinate(canvasSize.height, yAxisOffset, yAxisMinValue, yAxisMaxValue, false)
            ChartValue(y, maxY)
        }
    }
    val xAxisValues by remember(minXValue, maxXValue, data.bottomAxis.value) {
        derivedStateOf {
            data.bottomAxis.value.getValues(minXValue, maxXValue)
        }
    }
    val categoriesSpace by remember(data.bottomAxis.type.categoriesSpace) {
        derivedStateOf {
            with(density) {
                data.bottomAxis.type.categoriesSpace.toPx()
            }
        }
    }
    val categoryHeight by remember(canvasSize.height, yAxisMaxValue, yAxisOffset, data.bottomAxis.type) {
        derivedStateOf {
            val maxValue = yAxisMaxValue.value.toFloat()
            (canvasSize.height.toFloat() - yAxisOffset.x - yAxisOffset.y - ((maxValue - 1f) * categoriesSpace)) / maxValue
        }
    }
    val yAxisValues: List<ChartValueCoordinate> by remember(canvasSize.height, yAxisMaxValue, yAxisOffset, yAxisViewport) {
        derivedStateOf {
            if (canvasSize.height == 0) {
                listOf()
            } else {
                val values = mutableListOf<ChartValueCoordinate>()
                val offset = ChartPixelCoordinate(categoryHeight / 2f).toChartValueCoordinate(canvasSize.height, yAxisOffset, yAxisViewport.x, yAxisViewport.y, false)
                val categoriesOffset = ChartPixelCoordinate(categoryHeight + categoriesSpace).toChartValueCoordinate(canvasSize.height, yAxisOffset, yAxisViewport.x, yAxisViewport.y, false).value
                for (i in yAxisMinValue.value.toInt()..yAxisMaxValue.value.toInt()) {
                    values.add(
                        ChartValueCoordinate(offset.value + (categoriesOffset * i))
                    )
                }
                values
            }
        }
    }
    val barHeight by remember(categoryHeight, data.bottomAxis.categories, data.bottomAxis.type) {
        derivedStateOf {
            val type = data.bottomAxis.type
            when (type) {
                is BarChartData.Type.Grouped -> {
                    val barsSpace = with(density) {
                        type.barsSpace.toPx()
                    }
                    (categoryHeight - ((data.bottomAxis.categories.size - 1) * barsSpace)) / data.bottomAxis.categories.size
                }
                is BarChartData.Type.Stacked -> categoryHeight
            }
        }
    }
    val offsetCategories by remember(data.bottomAxis.categories, data.isLeftYAxis, barHeight, xAxisViewport) {
        derivedStateOf {
            val offsetCategories = mutableListOf<BarChartData.OffsetCategory>()
            if (data.bottomAxis.categories.isNotEmpty()) {
                val baseValueXPixel = ChartValueCoordinate(0.0).toChartPixelCoordinate(canvasSize.width, xAxisOffset, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                when(data.bottomAxis.type) {
                    is BarChartData.Type.Grouped -> {
                        val barsSpace = with(density) {
                            data.bottomAxis.type.barsSpace.toPx()
                        }
                        for (categoryIndex in 0..<data.bottomAxis.categories.size) {
                            val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                            val baseYPixel = yAxisOffset.x + ((barHeight + barsSpace) * categoryIndex)
                            data.bottomAxis.categories[categoryIndex].values.fastForEachIndexed { index, value ->
                                val yPixel = baseYPixel + ((categoryHeight + categoriesSpace) * index)
                                val valueXPixel = value.toChartPixelCoordinate(canvasSize.width, xAxisOffset, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                                val isNegative = value.value < 0.0
                                val isLeftToRight = (isNegative && data.isLeftYAxis) || (!isNegative && !data.isLeftYAxis)
                                val xPixel = if (isLeftToRight) {
                                    valueXPixel
                                } else {
                                    baseValueXPixel
                                }
                                offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, yPixel)), Size(abs(baseValueXPixel - valueXPixel), barHeight), isNegative))
                            }
                            offsetCategories.add(BarChartData.OffsetCategory(offsets, data.bottomAxis.categories[categoryIndex].tag, data.bottomAxis.categories[categoryIndex].customization))
                        }
                    }
                    is BarChartData.Type.Stacked -> {
                        val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                        val lastValues = data.bottomAxis.categories.first().values.toMutableList()
                        lastValues.fastForEachIndexed { index, value ->
                            val yPixel = yAxisOffset.x + ((categoryHeight + categoriesSpace) * index)
                            val valueXPixel = value.toChartPixelCoordinate(canvasSize.width, xAxisOffset, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                            val xPixel = if (data.isLeftYAxis) {
                                baseValueXPixel
                            } else {
                                valueXPixel
                            }
                            offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, yPixel)), Size(abs(baseValueXPixel - valueXPixel), barHeight), false))
                        }
                        offsetCategories.add(BarChartData.OffsetCategory(offsets, data.bottomAxis.categories[0].tag, data.bottomAxis.categories[0].customization))
                        for (categoryIndex in 1..<data.bottomAxis.categories.size) {
                            val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                            data.bottomAxis.categories[categoryIndex].values.fastForEachIndexed { index, value ->
                                val lastValue = lastValues.getOrElse(index, { ChartValueCoordinate(0.0)})
                                val currentValue = lastValue + value
                                if (lastValues.lastIndex < index) {
                                    lastValues.add(currentValue)
                                } else {
                                    lastValues[index] = currentValue
                                }
                                val yPixel = yAxisOffset.x + ((categoryHeight + categoriesSpace) * index)
                                val currentValueXPixel = currentValue.toChartPixelCoordinate(canvasSize.width, xAxisOffset, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                                val lastValueXPixel = lastValue.toChartPixelCoordinate(canvasSize.width, xAxisOffset, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                                offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(currentValueXPixel, yPixel)), Size(lastValueXPixel - currentValueXPixel, barHeight), false))
                            }
                            offsetCategories.add(BarChartData.OffsetCategory(offsets, data.bottomAxis.categories[categoryIndex].tag, data.bottomAxis.categories[categoryIndex].customization))
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
                axis?.valueView?.let {
                    AxisColumn(Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex), true, yAxisValues, yAxisViewport.x, yAxisViewport.y, false) {
                        for (i in 0..yAxisValues.lastIndex) {
                            it(i)
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
                xAxisValues
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
                                        topLeft = if(it.isNegative) category.customization.topRightCornerRadius else category.customization.topLeftCornerRadius,
                                        topRight = if (it.isNegative) category.customization.topLeftCornerRadius else category.customization.topRightCornerRadius,
                                        bottomRight = if (it.isNegative) category.customization.bottomLeftCornerRadius else category.customization.bottomRightCornerRadius,
                                        bottomLeft = if (it.isNegative) category.customization.bottomRightCornerRadius else category.customization.bottomLeftCornerRadius,
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
            data.bottomAxis.let { axis ->
                axis.valueView?.let {
                    AxisRow(Modifier.fillMaxWidth().zIndex(axesZIndex), xAxisValues, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis) {
                        xAxisValues.fastForEach { value ->
                            it(value.value)
                        }
                    }
                }
            }
        }
        if (!data.isLeftYAxis) {
            data.yAxis.let { axis ->
                axis?.valueView?.let {
                    AxisColumn(Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex), false, yAxisValues, yAxisViewport.x, yAxisViewport.y, false) {
                        for (i in 0..yAxisValues.lastIndex) {
                            it(i)
                        }
                    }
                }
            }
        }
    }
}