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
import com.skellyapps.charts.bar.animation.BarChartAnimations
import com.skellyapps.charts.bar.extension.binarySearch
import com.skellyapps.charts.bar.model.BarChartData
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
import com.skellyapps.charts.common.view.dividersZIndex
import kotlin.math.abs

private const val axesZIndex = -1f
internal const val barsZIndex = dividersZIndex - 5f

/**
 * @param modifier Mandatory modifier to specify size
 * @param data [BarChartData]
 * @param background The background of the inside of the chart
 * @param animations To enable animations
 * @param clipToBounds To enable clip to bounds
 * @param drawOnEachValue To draw on each value on each category
 */
@Composable
fun BarChart(
    modifier: Modifier,
    data: BarChartData,
    background: Brush = SolidColor(Color.Transparent),
    clipToBounds: Boolean = true,
    animations: BarChartAnimations = BarChartAnimations.None,
    barHover: BarChartData.BarHover? = null,
    drawOnEachValue: (DrawScope.(categoryTag: Int, index: Int, barRect: Rect, isNegative: Boolean) -> Unit)? = null
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize(0,0)) }
//    var canvasZoom by remember { mutableStateOf(Offset(1f,1f)) }
//    var canvasOffset by remember { mutableStateOf(Offset.Zero) }
    val yAxisOffset = remember { Offset.Zero }
    val xAxisOffset = remember(data.xAxisOffset, density) {
        with(density) {
            data.xAxisOffset.toOffset()
        }
    }
    val minXValue = remember { ChartValueCoordinate(0.0) }
    val maxXValue by remember(data.yAxis.categories) {
        derivedStateOf {
            ChartValueCoordinate(data.yAxis.categories.fastFilter { it.values.isNotEmpty() }.maxOfOrNull { it.values.size }?.toDouble() ?: 1.0)
        }
    }
    val xAxisViewport = remember(canvasSize.width,xAxisOffset, maxXValue) {
        val x = ChartPixelCoordinate(0f/*canvasOffset.x*/).toChartValueCoordinate(canvasSize.width, xAxisOffset, minXValue, maxXValue, false)
        val maxX = ChartPixelCoordinate(/*canvasOffset.x +*/ canvasSize.width.toFloat() /*/ canvasZoom.x*/).toChartValueCoordinate(canvasSize.width, xAxisOffset, minXValue, maxXValue, false)
        ChartValue(x, maxX)
    }
    val yAxisMinValue = remember(data.yAxis.minValue) {
        ChartValueCoordinate(data.yAxis.minValue ?: 0.0)
    }
    val yAxisMaxValue by remember(data.yAxis.maxValue, data.yAxis.type, data.yAxis.categories) {
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
    val yAxisViewport = remember(canvasSize.height, yAxisMinValue, yAxisMaxValue) {
        val y = ChartPixelCoordinate(/*canvasOffset.y +*/ canvasSize.height.toFloat() /*/ canvasZoom.y*/).toChartValueCoordinate(canvasSize.height, yAxisOffset, yAxisMinValue, yAxisMaxValue, true)
        val maxY = ChartPixelCoordinate(0f/*canvasOffset.y*/).toChartValueCoordinate(canvasSize.height, yAxisOffset, yAxisMinValue, yAxisMaxValue, true)
        ChartValue(y, maxY)
    }
    val yAxisValues = remember(yAxisMinValue, yAxisMaxValue, data.yAxis.value) {
        data.yAxis.value.getValues(yAxisMinValue, yAxisMaxValue)
    }
    val categoriesSpace = remember(data.yAxis.type.categoriesSpace, density) {
        with(density) {
            data.yAxis.type.categoriesSpace.toPx()
        }
    }
    val categoryWidth = remember(canvasSize.width, maxXValue, xAxisOffset) {
        val maxValue = maxXValue.value.toFloat()
        (canvasSize.width.toFloat() - xAxisOffset.x - xAxisOffset.y - ((maxValue - 1f) * categoriesSpace)) / maxValue
    }
    val bottomAxisValues: List<ChartValueCoordinate> = remember(canvasSize.width, categoriesSpace, categoryWidth, maxXValue, xAxisViewport) {
        val values = mutableListOf<ChartValueCoordinate>()
        val baseXValue = ChartValueCoordinate(0.0).toChartPixelCoordinate(canvasSize.width, xAxisViewport.x, xAxisViewport.y, false)
        val offset = ChartPixelCoordinate(categoryWidth / 2f)
        val categoriesOffset = ChartPixelCoordinate(categoryWidth + categoriesSpace)
        for (i in minXValue.value.toInt()..<maxXValue.value.toInt()) {
            val pixel = (baseXValue + offset + (categoriesOffset * i))
            values.add(pixel.toChartValueCoordinate(canvasSize.width, xAxisViewport.x, xAxisViewport.y, false))
        }
        values
    }
    val barWidth by remember(categoryWidth, data.yAxis.type, data.yAxis.categories) {
        derivedStateOf {
            when (val type = data.yAxis.type) {
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
    val offsetCategories by remember(data.yAxis.categories, barWidth, xAxisViewport, yAxisViewport) {
        derivedStateOf {
            val offsetCategories = mutableListOf<BarChartData.OffsetCategory>()
            if (data.yAxis.categories.isNotEmpty()) {
                val baseValueYPixel = ChartValueCoordinate(0.0).toChartPixelCoordinate(canvasSize.height, yAxisViewport.x, yAxisViewport.y, true).value
                when(data.yAxis.type) {
                    is BarChartData.Type.Grouped -> {
                        val barsSpace = with(density) {
                            data.yAxis.type.barsSpace.toPx()
                        }
                        for ((categoryIndex, category) in data.yAxis.categories.withIndex()) {
                            val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                            val baseXPixel = xAxisOffset.x + ((barWidth + barsSpace) * categoryIndex)
                            category.values.fastForEachIndexed { index, value ->
                                val xPixel = baseXPixel + ((categoryWidth + categoriesSpace) * index)
                                val valueYPixel = value.toChartPixelCoordinate(canvasSize.height,yAxisViewport.x,yAxisViewport.y,true).value
                                val isNegative = value.value < 0.0
                                val yPixel = if (isNegative) {
                                    baseValueYPixel
                                } else {
                                    valueYPixel
                                }
                                offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, yPixel)), Size(barWidth, abs(baseValueYPixel - valueYPixel)), isNegative))
                            }
                            offsetCategories.add(BarChartData.OffsetCategory(offsets, category.tag, category.customization))
                        }
                    }
                    is BarChartData.Type.Stacked -> {
                        val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                        val category = data.yAxis.categories[0]
                        val lastValues = category.values.toMutableList()
                        lastValues.fastForEachIndexed { index, value ->
                            val xPixel = xAxisOffset.x + ((categoryWidth + categoriesSpace) * index)
                            val valueYPixel = value.toChartPixelCoordinate(canvasSize.height, yAxisViewport.x, yAxisViewport.y, true).value
                            offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, valueYPixel)), Size(barWidth, baseValueYPixel - valueYPixel), false))
                        }
                        offsetCategories.add(BarChartData.OffsetCategory(offsets, category.tag, category.customization))
                        for (categoryIndex in 1..<data.yAxis.categories.size) {
                            val offsets = mutableListOf<BarChartData.OffsetCategory.Offset>()
                            val category = data.yAxis.categories[categoryIndex]
                            category.values.fastForEachIndexed { index, value ->
                                val lastValue = lastValues.getOrElse(index, {ChartValueCoordinate(0.0)})
                                val currentValue = lastValue + value
                                if (lastValues.lastIndex < index) {
                                    lastValues.add(currentValue)
                                } else {
                                    lastValues[index] = currentValue
                                }
                                val xPixel = xAxisOffset.x + ((categoryWidth + categoriesSpace) * index)
                                val currentValueYPixel = currentValue.toChartPixelCoordinate(canvasSize.height, yAxisViewport.x, yAxisViewport.y, true).value
                                val lastValueYPixel = lastValue.toChartPixelCoordinate(canvasSize.height, yAxisViewport.x, yAxisViewport.y, true).value
                                offsets.add(BarChartData.OffsetCategory.Offset(ChartPixel(Offset(xPixel, currentValueYPixel)), Size(barWidth, lastValueYPixel - currentValueYPixel), false))
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
                axis.valueView?.let {
                    AxisColumn(
                        Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex),
                        true,
                        yAxisValues,
                        yAxisViewport.x,
                        yAxisViewport.y,
                        true
                    ) {
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
                bottomAxisValues,
                true,
                false
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .zIndex(barsZIndex)
                        .`if`(clipToBounds, Modifier.clipToBounds())
                        .`if`(barHover != null, Modifier.pointerInput(canvasSize, offsetCategories) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val mousePosition = event.changes.first().position
                                    when(event.type) {
                                        PointerEventType.Move -> hoveredBar = offsetCategories.binarySearch(mousePosition, data.yAxis.type, true)
                                        PointerEventType.Exit -> hoveredBar = null
                                    }
                                }
                            }
                        })
                        .drawWithCache {
                            val path = Path()
                            onDrawWithContent {
                                val baseValueYPixel = ChartValueCoordinate(0.0).toChartPixelCoordinate(canvasSize.height, yAxisViewport.x, yAxisViewport.y, true).value
                                offsetCategories.fastForEachIndexed { categoryIndex, category ->
                                    val topLeftCornerRadius = category.customization.topLeftCornerRadius.toCornerRadius()
                                    val topRightCornerRadius = category.customization.topRightCornerRadius.toCornerRadius()
                                    val bottomRightCornerRadius = category.customization.bottomRightCornerRadius.toCornerRadius()
                                    val bottomLeftCornerRadius = category.customization.bottomLeftCornerRadius.toCornerRadius()
                                    category.offsets.fastForEachIndexed { offsetIndex, offset ->
                                        val rect = if (animations.growth != null) {
                                            when(data.yAxis.type) {
                                                is BarChartData.Type.Grouped -> animations.growth.getRect(offset)
                                                is BarChartData.Type.Stacked -> {
                                                    var y = baseValueYPixel
                                                    if (offset.isNegative) {
                                                        (0..<categoryIndex).forEach {
                                                            val height = offsetCategories[it].offsets.getOrNull(offsetIndex)?.size?.height ?: return@forEach
                                                            y += height * animations.growth.value
                                                        }
                                                    } else {
                                                        (0..categoryIndex).forEach {
                                                            val height = offsetCategories[it].offsets.getOrNull(offsetIndex)?.size?.height ?: return@forEach
                                                            y -= height * animations.growth.value
                                                        }
                                                    }
                                                    val height = offset.size.height * animations.growth.value
                                                    Rect(offset.topLeft.offset.copy(y = y), offset.size.copy(height = height))
                                                }
                                            }
                                        } else {
                                            Rect(offset.topLeft.offset, offset.size)
                                        }

                                        path.addRoundRect(
                                            RoundRect(
                                                rect,
                                                topLeft = if (offset.isNegative) bottomLeftCornerRadius else topLeftCornerRadius,
                                                topRight = if (offset.isNegative) bottomRightCornerRadius else topRightCornerRadius,
                                                bottomRight = if (offset.isNegative) topRightCornerRadius else bottomRightCornerRadius,
                                                bottomLeft = if (offset.isNegative) topLeftCornerRadius else bottomLeftCornerRadius,
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
                                                when(data.yAxis.type) {
                                                    is BarChartData.Type.Grouped -> animations.growth.getRect(offset)
                                                    is BarChartData.Type.Stacked -> {
                                                        var y = baseValueYPixel
                                                        if (offset.isNegative) {
                                                            (0..<categoryIndex).forEach {
                                                                val height = offsetCategories[it].offsets.getOrNull(offsetIndex)?.size?.height ?: return@forEach
                                                                y += height * animations.growth.value
                                                            }
                                                        } else {
                                                            (0..categoryIndex).forEach {
                                                                val height = offsetCategories[it].offsets.getOrNull(offsetIndex)?.size?.height ?: return@forEach
                                                                y -= height * animations.growth.value
                                                            }
                                                        }
                                                        val height = offset.size.height * animations.growth.value
                                                        Rect(offset.topLeft.offset.copy(y = y), offset.size.copy(height = height))
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
                                    barHover.getViewOffset(this, canvasSize, viewSize, it.offset)
                                } ?: IntOffset.Zero
                            }
                        ) {
                            barHover.view(hoveredBar!!.categoryTag, hoveredBar!!.index)
                        }
                    }
                }
            }
            data.bottomAxis?.let { axis ->
                axis.valueView?.let {
                    AxisRow(
                        Modifier.fillMaxWidth().zIndex(axesZIndex),
                        bottomAxisValues,
                        xAxisViewport.x,
                        xAxisViewport.y,
                        false
                    ) {
                        for (i in bottomAxisValues.indices) {
                            it(i)
                        }
                    }
                }
            }
        }
        if (!data.isLeftYAxis) {
            data.yAxis.let { axis ->
                axis.valueView?.let {
                    AxisColumn(
                        Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex),
                        false,
                        yAxisValues,
                        yAxisViewport.x,
                        yAxisViewport.y,
                        true
                    ) {
                        yAxisValues.fastForEach { value ->
                            it(value.value)
                        }
                    }
                }
            }
        }
    }
}