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
        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.map { measurable ->
            // Measure each children
            measurable.measure(constraints)
        }
        val maxHeight = placeables.fastMaxOfOrDefault(0) { it.height }
        val xRange = (maxXValue - minXValue).value
        val scaledCanvasWidth = canvasScale * (constraints.maxWidth - axisOffset.x - axisOffset.y)
        val scaledCanvasOffset = (axisOffset.x - canvasXOffset) * canvasScale
        val maxWidthTolerance = constraints.maxWidth + 0.01
        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, maxHeight) {
            // Place children in the parent layout
            placeables.fastForEachIndexed { index, placeable ->
                // Position item on the screen
                val xOffset = (((values[index] - minXValue).value / xRange) * scaledCanvasWidth + scaledCanvasOffset)
                if (xOffset >= 0.0 && xOffset <= maxWidthTolerance) {
                    placeable.placeRelative(x = (xOffset - (placeable.width / 2.0)).fastRoundToInt(), y = 0)
                }
            }
        }
    }
}