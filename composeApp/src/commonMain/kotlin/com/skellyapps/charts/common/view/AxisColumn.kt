package com.skellyapps.charts.common.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrDefault
import androidx.compose.ui.util.fastRoundToInt
import com.skellyapps.charts.common.model.ChartValueCoordinate

@Composable
internal fun AxisColumn(
    modifier: Modifier,
    canvasScale: Float,
    canvasYOffset: Float,
    axisOffset: Offset,
    leftAxis: Boolean,
    values: List<ChartValueCoordinate>,
    minYValue: ChartValueCoordinate,
    maxYValue: ChartValueCoordinate,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.map { measurable ->
            // Measure each children
            measurable.measure(constraints)
        }
        val maxWidth = placeables.fastMaxOfOrDefault(0) { it.width }
        val yRange = (maxYValue - minYValue).value
        val scaledCanvasHeight = canvasScale * (constraints.maxHeight - axisOffset.x - axisOffset.y)
        val scaledCanvasOffset = (axisOffset.x + canvasYOffset) * canvasScale
        val maxHeightTolerance = constraints.maxHeight + 0.01
        // Set the size of the layout as big as it can
        layout(maxWidth, constraints.maxHeight) {
            // Place children in the parent layout
            placeables.fastForEachIndexed { index, placeable ->
                // Position item on the screen
                val yOffset = (constraints.maxHeight * canvasScale - (((values[index] - minYValue).value / yRange) * scaledCanvasHeight + scaledCanvasOffset))
                if (yOffset >= 0.0 && yOffset <= maxHeightTolerance) {
                    val y = (yOffset - (placeable.height / 2f)).fastRoundToInt()
                    if (leftAxis) {
                        placeable.placeRelative(x = maxWidth - placeable.width, y = y)
                    } else {
                        placeable.placeRelative(x = 0, y = y)
                    }
                }
            }
        }
    }
}

@Composable
internal fun AxisColumn(
    modifier: Modifier,
    leftAxis: Boolean,
    values: List<ChartValueCoordinate>,
    minYValue: ChartValueCoordinate,
    maxYValue: ChartValueCoordinate,
    inverted: Boolean,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.map { measurable ->
            // Measure each children
            measurable.measure(constraints)
        }
        val maxWidth = placeables.fastMaxOfOrDefault(0) { it.width }
        val maxHeightTolerance = constraints.maxHeight + 0.01
        // Set the size of the layout as big as it can
        layout(maxWidth, constraints.maxHeight) {
            // Place children in the parent layout
            placeables.fastForEachIndexed { index, placeable ->
                // Position item on the screen
                val yOffset = values[index].toChartPixelCoordinate(constraints.maxHeight, minYValue, maxYValue, inverted).value
                if (yOffset >= 0.0 && yOffset <= maxHeightTolerance) {
                    val y = (yOffset - (placeable.height / 2f)).fastRoundToInt()
                    if (leftAxis) {
                        placeable.placeRelative(x = maxWidth - placeable.width, y = y)
                    } else {
                        placeable.placeRelative(x = 0, y = y)
                    }
                }
            }
        }
    }
}