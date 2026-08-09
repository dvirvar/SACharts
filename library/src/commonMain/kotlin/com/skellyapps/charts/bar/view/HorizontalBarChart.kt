package com.skellyapps.charts.bar.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrDefault
import androidx.compose.ui.zIndex
import com.skellyapps.charts.bar.animation.HorizontalBarChartAnimations
import com.skellyapps.charts.bar.extension.binarySearch
import com.skellyapps.charts.bar.model.BarChartData
import com.skellyapps.charts.bar.model.HorizontalBarChartData
import com.skellyapps.charts.bar.model.TaggedOffsetEqualityPolicy
import com.skellyapps.charts.common.extension.`if`
import com.skellyapps.charts.common.extension.toOffset
import com.skellyapps.charts.common.model.ChartPixel
import com.skellyapps.charts.common.model.ChartPixelCoordinate
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.view.AxisColumn
import com.skellyapps.charts.common.view.AxisRow
import com.skellyapps.charts.common.view.GridChartCanvas
import kotlin.math.abs

private const val axesZIndex = -1f

/**
 * @param modifier Mandatory modifier to specify size
 * @param data [BarChartData]
 * @param background The background of the inside of the chart
 * @param animations To enable animations
 * @param drawOnEachValue To draw on each value on each category
 */
@Composable
fun HorizontalBarChart(
    modifier: Modifier,
    data: HorizontalBarChartData,
    background: Brush = SolidColor(Color.Transparent),
    animations: HorizontalBarChartAnimations = HorizontalBarChartAnimations.None,
    barHover: BarChartData.BarHover? = null,
    drawOnEachValue: (DrawScope.(categoryTag: Int, index: Int, barRect: Rect, isNegative: Boolean) -> Unit)? = null
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize(0,0)) }
//    var canvasZoom by remember { mutableStateOf(Offset(1f,1f)) }
//    var canvasOffset by remember { mutableStateOf(Offset.Zero) }
    val yAxisOffset = remember(data.yAxis?.offset, density) {
        if (data.yAxis == null) {
            Offset.Zero
        } else {
            with(density) {
                data.yAxis.offset.toOffset()
            }
        }
    }
    val xAxisOffset = remember { Offset.Zero }
    val minXValue = remember(data.bottomAxis.minValue) {
        ChartValueCoordinate(data.bottomAxis.minValue ?: 0.0)
    }
    val maxXValue by remember(data.bottomAxis.maxValue, data.bottomAxis.type, data.bottomAxis.categories) {
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
    val xAxisViewport = remember(canvasSize.width, minXValue, maxXValue) {
        val x = ChartPixelCoordinate(0f/*canvasOffset.x*/).toChartValueCoordinate(canvasSize.width, xAxisOffset, minXValue, maxXValue, false)
        val maxX = ChartPixelCoordinate(/*canvasOffset.x +*/ canvasSize.width.toFloat() /*/ canvasZoom.x*/).toChartValueCoordinate(canvasSize.width, xAxisOffset, minXValue, maxXValue, false)
        ChartValue(x, maxX)
    }
    val yAxisMinValue = remember { ChartValueCoordinate(0.0) }
    val yAxisMaxValue by remember(data.bottomAxis.categories) {
        derivedStateOf {
            ChartValueCoordinate(data.bottomAxis.categories.fastFilter { it.values.isNotEmpty() }.maxOfOrNull { it.values.size }?.toDouble() ?: 1.0)
        }
    }
    val yAxisViewport = remember(canvasSize.height, yAxisOffset, yAxisMaxValue) {
        val y = ChartPixelCoordinate(0f/*canvasOffset.y*/).toChartValueCoordinate(canvasSize.height, yAxisOffset, yAxisMinValue, yAxisMaxValue, false)
        val maxY = ChartPixelCoordinate(/*canvasOffset.y +*/ canvasSize.height.toFloat() /*/ canvasZoom.y*/).toChartValueCoordinate(canvasSize.height, yAxisOffset, yAxisMinValue, yAxisMaxValue, false)
        ChartValue(y, maxY)
    }
    val xAxisValues = remember(minXValue, maxXValue, data.bottomAxis.value) {
        data.bottomAxis.value.getValues(minXValue, maxXValue)
    }
    val categoriesSpace = remember(data.bottomAxis.type.categoriesSpace, density) {
        with(density) {
            data.bottomAxis.type.categoriesSpace.toPx()
        }
    }
    val categoryHeight = remember(canvasSize.height, yAxisMaxValue, yAxisOffset, categoriesSpace) {
        val maxValue = yAxisMaxValue.value.toFloat()
        (canvasSize.height.toFloat() - yAxisOffset.x - yAxisOffset.y - ((maxValue - 1f) * categoriesSpace)) / maxValue
    }
    val yAxisValues: List<ChartValueCoordinate> = remember(canvasSize.height, yAxisMaxValue, yAxisViewport, categoriesSpace, categoryHeight) {
        val values = mutableListOf<ChartValueCoordinate>()
        val baseYValue = ChartValueCoordinate(0.0).toChartPixelCoordinate(canvasSize.height, yAxisViewport.x, yAxisViewport.y, false)
        val offset = ChartPixelCoordinate(categoryHeight / 2f)
        val categoriesOffset = ChartPixelCoordinate(categoryHeight + categoriesSpace)
        for (i in yAxisMinValue.value.toInt()..<yAxisMaxValue.value.toInt()) {
            val pixel = (baseYValue + offset + (categoriesOffset * i))
            values.add(pixel.toChartValueCoordinate(canvasSize.height, yAxisViewport.x, yAxisViewport.y, false))
        }
        values
    }
    val barHeight by remember(categoryHeight, data.bottomAxis.type, data.bottomAxis.categories) {
        derivedStateOf {
            when (val type = data.bottomAxis.type) {
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
    val offsetCategories by remember(data.bottomAxis.categories, data.isLeftYAxis, barHeight, xAxisViewport, categoriesSpace, categoryHeight) {
        derivedStateOf {
            val offsetCategories = mutableListOf<BarChartData.OffsetCategory>()
            if (data.bottomAxis.categories.isNotEmpty()) {
                val baseValueXPixel = ChartValueCoordinate(0.0).toChartPixelCoordinate(canvasSize.width, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                when(data.bottomAxis.type) {
                    is BarChartData.Type.Grouped -> {
                        val barsSpace = with(density) {
                            data.bottomAxis.type.barsSpace.toPx()
                        }
                        for ((categoryIndex, category) in data.bottomAxis.categories.withIndex()) {
                            val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                            val baseYPixel = yAxisOffset.x + ((barHeight + barsSpace) * categoryIndex)
                            category.values.fastForEachIndexed { index, value ->
                                val yPixel = baseYPixel + ((categoryHeight + categoriesSpace) * index)
                                val valueXPixel = value.toChartPixelCoordinate(canvasSize.width, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                                val isNegative = value.value < 0.0
                                val isLeftToRight = isNegative == data.isLeftYAxis
                                val xPixel = if (isLeftToRight) {
                                    valueXPixel
                                } else {
                                    baseValueXPixel
                                }
                                offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, yPixel)), Size(abs(baseValueXPixel - valueXPixel), barHeight), isNegative))
                            }
                            offsetCategories.add(BarChartData.OffsetCategory(offsets, category.tag, category.customization))
                        }
                    }
                    is BarChartData.Type.Stacked -> {
                        val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                        val category = data.bottomAxis.categories[0]
                        val lastValues = category.values.toMutableList()
                        lastValues.fastForEachIndexed { index, value ->
                            val yPixel = yAxisOffset.x + ((categoryHeight + categoriesSpace) * index)
                            val valueXPixel = value.toChartPixelCoordinate(canvasSize.width, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                            val xPixel = if (data.isLeftYAxis) {
                                baseValueXPixel
                            } else {
                                valueXPixel
                            }
                            offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, yPixel)), Size(abs(baseValueXPixel - valueXPixel), barHeight), false))
                        }
                        offsetCategories.add(BarChartData.OffsetCategory(offsets, category.tag, category.customization))
                        for (categoryIndex in 1..<data.bottomAxis.categories.size) {
                            val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                            val category = data.bottomAxis.categories[categoryIndex]
                            category.values.fastForEachIndexed { index, value ->
                                val lastValue = lastValues.getOrElse(index, {ChartValueCoordinate(0.0)})
                                val currentValue = lastValue + value
                                if (lastValues.lastIndex < index) {
                                    lastValues.add(currentValue)
                                } else {
                                    lastValues[index] = currentValue
                                }
                                val yPixel = yAxisOffset.x + ((categoryHeight + categoriesSpace) * index)
                                val currentValueXPixel = currentValue.toChartPixelCoordinate(canvasSize.width, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                                val lastValueXPixel = lastValue.toChartPixelCoordinate(canvasSize.width, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                                val xPixel = if (data.isLeftYAxis) {
                                    lastValueXPixel
                                } else {
                                    currentValueXPixel
                                }
                                offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, yPixel)), Size(abs(lastValueXPixel - currentValueXPixel), barHeight), false))
                            }
                            offsetCategories.add(BarChartData.OffsetCategory(offsets, category.tag, category.customization))
                        }
                    }
                }
            }
            offsetCategories
        }
    }
    var hoveredBar by remember { mutableStateOf(null, TaggedOffsetEqualityPolicy) }

    Row(modifier) {
        if (data.isLeftYAxis) {
            data.yAxis.let { axis ->
                axis?.valueView?.let {
                    AxisColumn(
                        Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex),
                        true,
                        yAxisValues,
                        yAxisViewport.x,
                        yAxisViewport.y,
                        false
                    ) {
                        for (i in yAxisValues.indices) {
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
                xAxisValues,
                false,
                !data.isLeftYAxis
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .zIndex(barsZIndex)
                        .clipToBounds()
                        .`if`(barHover != null, Modifier.pointerInput(canvasSize, offsetCategories) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val mousePosition = event.changes.first().position
                                    when(event.type) {
                                        PointerEventType.Move -> hoveredBar = offsetCategories.binarySearch(mousePosition, data.bottomAxis.type, false)
                                        PointerEventType.Exit -> hoveredBar = null
                                    }
                                }
                            }
                        })
                        .drawWithCache {
                            val path = Path()
                            onDrawWithContent {
                                val baseValueXPixel = ChartValueCoordinate(0.0).toChartPixelCoordinate(canvasSize.width, xAxisViewport.x, xAxisViewport.y, !data.isLeftYAxis).value
                                offsetCategories.fastForEachIndexed { categoryIndex, category ->
                                    category.offsets.fastForEachIndexed { offsetIndex, offset ->
                                        val rect = if (animations.growth != null) {
                                            when(data.bottomAxis.type) {
                                                is BarChartData.Type.Grouped -> animations.growth.getRect(offset)
                                                is BarChartData.Type.Stacked -> {
                                                    var x = baseValueXPixel
                                                    if (data.isLeftYAxis) {
                                                        (0..<categoryIndex).forEach {
                                                            val width = offsetCategories[it].offsets.getOrNull(offsetIndex)?.size?.width ?: return@forEach
                                                            x += width * animations.growth.value
                                                        }
                                                    } else {
                                                        (0..categoryIndex).forEach {
                                                            val width = offsetCategories[it].offsets.getOrNull(offsetIndex)?.size?.width ?: return@forEach
                                                            x -= width * animations.growth.value
                                                        }
                                                    }
                                                    val width = offset.size.width * animations.growth.value
                                                    Rect(offset.topLeft.offset.copy(x = x), offset.size.copy(width = width))
                                                }
                                            }
                                        } else {
                                            Rect(offset.topLeft.offset, offset.size)
                                        }
                                        path.addRoundRect(
                                            RoundRect(
                                                rect,
                                                topLeft = (if (offset.isNegative) category.customization.topRightCornerRadius else category.customization.topLeftCornerRadius).toCornerRadius(),
                                                topRight = (if (offset.isNegative) category.customization.topLeftCornerRadius else category.customization.topRightCornerRadius).toCornerRadius(),
                                                bottomRight = (if (offset.isNegative) category.customization.bottomLeftCornerRadius else category.customization.bottomRightCornerRadius).toCornerRadius(),
                                                bottomLeft = (if (offset.isNegative) category.customization.bottomRightCornerRadius else category.customization.bottomLeftCornerRadius).toCornerRadius(),
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
                                    offsetCategories.fastForEachIndexed { categoryIndex, category ->
                                        category.offsets.fastForEachIndexed { offsetIndex, offset ->
                                            val rect = if (animations.growth != null) {
                                                when(data.bottomAxis.type) {
                                                    is BarChartData.Type.Grouped -> animations.growth.getRect(offset)
                                                    is BarChartData.Type.Stacked -> {
                                                        var x = baseValueXPixel
                                                        if (data.isLeftYAxis) {
                                                            (0..<categoryIndex).forEach {
                                                                val width = offsetCategories[it].offsets.getOrNull(offsetIndex)?.size?.width ?: return@forEach
                                                                x += width * animations.growth.value
                                                            }
                                                        } else {
                                                            (0..categoryIndex).forEach {
                                                                val width = offsetCategories[it].offsets.getOrNull(offsetIndex)?.size?.width ?: return@forEach
                                                                x -= width * animations.growth.value
                                                            }
                                                        }
                                                        val width = offset.size.width * animations.growth.value
                                                        Rect(offset.topLeft.offset.copy(x = x), offset.size.copy(width = width))
                                                    }
                                                }
                                            } else {
                                                Rect(offset.topLeft.offset, offset.size)
                                            }
                                            drawOnEachValue(
                                                this,
                                                category.tag,
                                                offsetIndex,
                                                rect,
                                                offset.isNegative
                                            )
                                        }
                                    }
                                }
                                drawContent()
                            }
                        }) {
                    if (barHover != null && hoveredBar != null) {
                        var viewSize by remember { mutableStateOf(IntSize.Zero) }
                        Box(Modifier
                            .onSizeChanged {
                                viewSize = it
                            }
                            .offset {
                                hoveredBar?.let {
                                    barHover.getViewOffsetHorizontal(this, canvasSize, viewSize, it.offset)
                                } ?: IntOffset.Zero
                            }
                        ) {
                            barHover.view(hoveredBar!!.categoryTag, hoveredBar!!.index)
                        }
                    }
                }
            }
            data.bottomAxis.let { axis ->
                axis.valueView?.let {
                    AxisRow(
                        Modifier.fillMaxWidth().zIndex(axesZIndex),
                        xAxisValues,
                        xAxisViewport.x,
                        xAxisViewport.y,
                        !data.isLeftYAxis
                    ) {
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
                    AxisColumn(
                        Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex),
                        false,
                        yAxisValues,
                        yAxisViewport.x,
                        yAxisViewport.y,
                        false
                    ) {
                        for (i in yAxisValues.indices) {
                            it(i)
                        }
                    }
                }
            }
        }
    }
}