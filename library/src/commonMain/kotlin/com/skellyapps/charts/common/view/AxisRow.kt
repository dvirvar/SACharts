package com.skellyapps.charts.common.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrDefault
import androidx.compose.ui.util.fastRoundToInt
import com.skellyapps.charts.common.model.ChartValueCoordinate
//For the "RealZoom" feature
@Composable
internal fun AxisRow(
    modifier: Modifier,
    canvasScale: Float,
    canvasXOffset: Float,
    axisOffset: Offset,
    values: List<ChartValueCoordinate>,
    minXValue: ChartValueCoordinate,
    maxXValue: ChartValueCoordinate,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        val maxHeight = placeables.fastMaxOfOrDefault(0) { it.height }
        val xRange = (maxXValue - minXValue).value
        val scaledCanvasWidth = canvasScale * (constraints.maxWidth - axisOffset.x - axisOffset.y)
        val scaledCanvasOffset = (axisOffset.x - canvasXOffset) * canvasScale
        val maxWidthTolerance = constraints.maxWidth + 0.01

        layout(constraints.maxWidth, maxHeight) {
            placeables.fastForEachIndexed { index, placeable ->
                val xOffset = (((values[index] - minXValue).value / xRange) * scaledCanvasWidth + scaledCanvasOffset)
                if (xOffset in 0.0..maxWidthTolerance) {
                    placeable.placeRelative(x = (xOffset - (placeable.width / 2f)).fastRoundToInt(), y = 0)
                }
            }
        }
    }
}

@Composable
internal fun AxisRow(
    modifier: Modifier,
    values: List<ChartValueCoordinate>,
    minXValue: ChartValueCoordinate,
    maxXValue: ChartValueCoordinate,
    inverted: Boolean,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        val maxHeight = placeables.fastMaxOfOrDefault(0) { it.height }
        val maxWidthTolerance = constraints.maxWidth + 0.01

        layout(constraints.maxWidth, maxHeight) {

            placeables.fastForEachIndexed { index, placeable ->
                val xOffset = values[index].toChartPixelCoordinate(constraints.maxWidth, minXValue, maxXValue, inverted).value
                if (xOffset in 0.0..maxWidthTolerance) {
                    placeable.placeRelative(x = (xOffset - (placeable.width / 2f)).fastRoundToInt(), y = 0)
                }
            }
        }
    }
}