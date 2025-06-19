package com.skellyapps.charts.common.view

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.GridChartData

internal const val axesZIndex = -1f

@Composable
internal fun GridChart(
    modifier: Modifier,
    background: Brush,
    leftAxis: GridChartData.Axis.YAxis?,
    rightAxis: GridChartData.Axis.YAxis?,
    bottomAxis: GridChartData.Axis.XAxis?,
    leftAxisValues: List<ChartValueCoordinate>,
    rightAxisValues: List<ChartValueCoordinate>,
    bottomAxisValues: List<ChartValueCoordinate>,
    leftAxisYViewport: ChartValue,
    rightAxisYViewport: ChartValue,
    xAxisViewport: ChartValue,
    onCanvasSizeChanged: (IntSize) -> Unit,
    content: @Composable (BoxWithConstraintsScope.() -> Unit)
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Row(modifier) {
        leftAxis?.let { axis ->
            axis.valueView?.let {
                AxisColumn(Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex), true, leftAxisValues, leftAxisYViewport.x, leftAxisYViewport.y) {
                    leftAxisValues.fastForEach { value ->
                        it(value.value)
                    }
                }
            }
        }
        Column(Modifier.weight(1f)) {
            GridChartCanvas(
                Modifier.fillMaxWidth().weight(1f).onSizeChanged {
                    canvasSize = it
                    onCanvasSizeChanged(it)
                }.drawBehind {
                    drawRect(background)
                },
                leftAxis,
                rightAxis,
                bottomAxis,
                leftAxisYViewport.x,
                leftAxisYViewport.y,
                rightAxisYViewport.x,
                rightAxisYViewport.y,
                xAxisViewport.x,
                xAxisViewport.y,
                leftAxisValues,
                rightAxisValues,
                bottomAxisValues,
                content
            )
            bottomAxis?.let { axis ->
                axis.valueView?.let {
                    AxisRow(Modifier.fillMaxWidth().zIndex(axesZIndex), bottomAxisValues, xAxisViewport.x, xAxisViewport.y) {
                        bottomAxisValues.fastForEach { value ->
                            it(value.value)
                        }
                    }
                }
            }
        }
        rightAxis?.let { axis ->
            axis.valueView?.let {
                AxisColumn(Modifier.height(with(density) { canvasSize.height.toDp() }).zIndex(axesZIndex), false, rightAxisValues, rightAxisYViewport.x, rightAxisYViewport.y) {
                    rightAxisValues.fastForEach { value ->
                        it(value.value)
                    }
                }
            }
        }
    }
}