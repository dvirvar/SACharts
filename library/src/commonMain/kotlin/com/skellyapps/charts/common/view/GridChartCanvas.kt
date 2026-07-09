package com.skellyapps.charts.common.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.GridChartData

internal const val gridLinesZIndex = 10f
internal const val dividersZIndex = gridLinesZIndex + 10f

@Composable
internal fun GridChartCanvas(
    modifier: Modifier,
    leftAxis: GridChartData.Axis.YAxis?,
    rightAxis: GridChartData.Axis.YAxis?,
    bottomAxis: GridChartData.Axis.XAxis?,
    leftAxisMinYValue: ChartValueCoordinate,
    leftAxisMaxYValue: ChartValueCoordinate,
    rightAxisMinYValue: ChartValueCoordinate,
    rightAxisMaxYValue: ChartValueCoordinate,
    minXValue: ChartValueCoordinate,
    maxXValue: ChartValueCoordinate,
    leftAxisValues: List<ChartValueCoordinate>,
    rightAxisValues: List<ChartValueCoordinate>,
    bottomAxisValues: List<ChartValueCoordinate>,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    BoxWithConstraints(modifier) {
        //Grid lines canvas
        Canvas(Modifier.fillMaxSize().zIndex(gridLinesZIndex).clipToBounds()) {
            leftAxis?.let { axis ->
                //Draw left axis grid lines
                axis.gridLines?.let {
                    val thickness = it.customization.thickness.toPx()
                    val startIndex = if (it.showFirstLine) 0 else 1
                    val endIndex = if (it.showLastLine) leftAxisValues.size - 1 else leftAxisValues.size - 2
                    for (i in startIndex..endIndex) {
                        val yOffset = leftAxisValues[i].toChartPixelCoordinate(size.height, leftAxisMinYValue, leftAxisMaxYValue, true).value
                        drawLine(
                            it.customization.brush,
                            Offset(0f, yOffset),
                            Offset(size.width, yOffset),
                            thickness,
                            it.customization.cap,
                            it.customization.pathEffect,
                            it.customization.alpha,
                            it.customization.colorFilter,
                            it.customization.blendMode
                        )
                    }
                }
            }
            rightAxis?.let { axis ->
                //Draw right axis grid lines
                axis.gridLines?.let {
                    val thickness = it.customization.thickness.toPx()
                    val startIndex = if (it.showFirstLine) 0 else 1
                    val endIndex = if (it.showLastLine) rightAxisValues.size - 1 else rightAxisValues.size - 2
                    for (i in startIndex..endIndex) {
                        val yOffset = rightAxisValues[i].toChartPixelCoordinate(size.height, rightAxisMinYValue, rightAxisMaxYValue, true).value
                        drawLine(
                            it.customization.brush,
                            Offset(size.width, yOffset),
                            Offset(0f, yOffset),
                            thickness,
                            it.customization.cap,
                            it.customization.pathEffect,
                            it.customization.alpha,
                            it.customization.colorFilter,
                            it.customization.blendMode
                        )
                    }
                }
            }
            bottomAxis?.let { axis ->
                //Draw bottom axis grid lines
                axis.gridLines?.let {
                    val thickness = it.customization.thickness.toPx()
                    val startIndex = if (it.showFirstLine) 0 else 1
                    val endIndex = if (it.showLastLine) bottomAxisValues.size - 1 else bottomAxisValues.size - 2
                    for (i in startIndex..endIndex) {
                        val xOffset = bottomAxisValues[i].toChartPixelCoordinate(size.width, minXValue, maxXValue, false).value
                        drawLine(
                            it.customization.brush,
                            Offset(xOffset, 0f),
                            Offset(xOffset, size.height),
                            thickness,
                            it.customization.cap,
                            it.customization.pathEffect,
                            it.customization.alpha,
                            it.customization.colorFilter,
                            it.customization.blendMode
                        )
                    }
                }
            }
        }
        //Dividers canvas
        Canvas(Modifier.fillMaxSize().zIndex(dividersZIndex)) {
            //Draw left axis divider
            leftAxis?.dividerCustomization?.let {
                val thickness = it.thickness.toPx()
                drawLine(
                    it.brush,
                    Offset(0f , size.height),
                    Offset(0f, 0f),
                    thickness,
                    it.cap,
                    it.pathEffect,
                    it.alpha,
                    it.colorFilter,
                    it.blendMode
                )
            }
            //Draw right axis divider
            rightAxis?.dividerCustomization?.let {
                val thickness = it.thickness.toPx()
                drawLine(
                    it.brush,
                    Offset(size.width, size.height),
                    Offset(size.width, 0f),
                    thickness,
                    it.cap,
                    it.pathEffect,
                    it.alpha,
                    it.colorFilter,
                    it.blendMode
                )
            }
            //Draw bottom axis divider
            bottomAxis?.dividerCustomization?.let {
                val thickness = it.thickness.toPx()
                drawLine(
                    it.brush,
                    Offset(0f, size.height),
                    Offset(size.width, size.height),
                    thickness,
                    StrokeCap.Square,
                    it.pathEffect,
                    it.alpha,
                    it.colorFilter,
                    it.blendMode
                )
            }
        }
        content()
    }
}
//For the "RealZoom" feature
@Composable
internal fun GridChartCanvas(
    modifier: Modifier,
    leftAxis: GridChartData.Axis.YAxis?,
    rightAxis: GridChartData.Axis.YAxis?,
    bottomAxis: GridChartData.Axis.XAxis?,
    xAxisOffset: Offset,
    canvasZoom: Float,
    canvasOffset: Offset,
    leftAxisMinYValue: ChartValueCoordinate,
    leftAxisMaxYValue: ChartValueCoordinate,
    rightAxisMinYValue: ChartValueCoordinate,
    rightAxisMaxYValue: ChartValueCoordinate,
    minXValue: ChartValueCoordinate,
    maxXValue: ChartValueCoordinate,
    leftAxisValues: List<ChartValueCoordinate>,
    rightAxisValues: List<ChartValueCoordinate>,
    bottomAxisValues: List<ChartValueCoordinate>,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    BoxWithConstraints(modifier) {
        //Grid lines canvas
        Canvas(Modifier.fillMaxSize().zIndex(gridLinesZIndex).clipToBounds().graphicsLayer {
            translationX = -canvasOffset.x * canvasZoom
            translationY = -canvasOffset.y * canvasZoom
            scaleX = canvasZoom
            scaleY = canvasZoom
            transformOrigin = TransformOrigin(0f, 0f)
        }) {
            leftAxis?.let { axis ->
                //Draw left axis grid lines
                axis.gridLines?.let {
                    val thickness = it.customization.thickness.toPx()
                    val startIndex = if (it.showFirstLine) 0 else 1
                    val endIndex = if (it.showLastLine) leftAxisValues.size - 1 else leftAxisValues.size - 2
                    val offset = Offset(axis.offset.x.toPx(), axis.offset.y.toPx())
                    for (i in startIndex..endIndex) {
                        val yOffset = leftAxisValues[i].toChartPixelCoordinate(size.height, offset, leftAxisMinYValue, leftAxisMaxYValue, true).value
                        drawLine(
                            it.customization.brush,
                            Offset(0f, yOffset),
                            Offset(size.width, yOffset),
                            thickness,
                            it.customization.cap,
                            it.customization.pathEffect,
                            it.customization.alpha,
                            it.customization.colorFilter,
                            it.customization.blendMode
                        )
                    }
                }
            }
            rightAxis?.let { axis ->
                //Draw right axis grid lines
                axis.gridLines?.let {
                    val thickness = it.customization.thickness.toPx()
                    val startIndex = if (it.showFirstLine) 0 else 1
                    val endIndex = if (it.showLastLine) rightAxisValues.size - 1 else rightAxisValues.size - 2
                    val offset = Offset(axis.offset.x.toPx(), axis.offset.y.toPx())
                    for (i in startIndex..endIndex) {
                        val yOffset = rightAxisValues[i].toChartPixelCoordinate(size.height, offset, rightAxisMinYValue, rightAxisMaxYValue, true).value
                        drawLine(
                            it.customization.brush,
                            Offset(size.width, yOffset),
                            Offset(0f, yOffset),
                            thickness,
                            it.customization.cap,
                            it.customization.pathEffect,
                            it.customization.alpha,
                            it.customization.colorFilter,
                            it.customization.blendMode
                        )
                    }
                }
            }
            bottomAxis?.let { axis ->
                //Draw bottom axis grid lines
                axis.gridLines?.let {
                    val thickness = it.customization.thickness.toPx()
                    val startIndex = if (it.showFirstLine) 0 else 1
                    val endIndex = if (it.showLastLine) bottomAxisValues.size - 1 else bottomAxisValues.size - 2
                    val offset = Offset(xAxisOffset.x, xAxisOffset.y)
                    for (i in startIndex..endIndex) {
                        val xOffset = bottomAxisValues[i].toChartPixelCoordinate(size.width, offset, minXValue, maxXValue, false).value
                        drawLine(
                            it.customization.brush,
                            Offset(xOffset, 0f),
                            Offset(xOffset, size.height),
                            thickness,
                            it.customization.cap,
                            it.customization.pathEffect,
                            it.customization.alpha,
                            it.customization.colorFilter,
                            it.customization.blendMode
                        )
                    }
                }
            }
        }
        //Dividers canvas
        Canvas(Modifier.fillMaxSize().zIndex(dividersZIndex)) {
            //Draw left axis divider
            leftAxis?.dividerCustomization?.let {
                val thickness = it.thickness.toPx()
                drawLine(
                    it.brush,
                    Offset(0f , 0f),
                    Offset(0f, size.height),
                    thickness,
                    it.cap,
                    it.pathEffect,
                    it.alpha,
                    it.colorFilter,
                    it.blendMode
                )
            }
            //Draw right axis divider
            rightAxis?.dividerCustomization?.let {
                val thickness = it.thickness.toPx()
                drawLine(
                    it.brush,
                    Offset(size.width, 0f),
                    Offset(size.width, size.height),
                    thickness,
                    it.cap,
                    it.pathEffect,
                    it.alpha,
                    it.colorFilter,
                    it.blendMode
                )
            }
            //Draw bottom axis divider
            bottomAxis?.dividerCustomization?.let {
                val thickness = it.thickness.toPx()
                drawLine(
                    it.brush,
                    Offset(0f, size.height),
                    Offset(size.width, size.height),
                    thickness,
                    StrokeCap.Square,
                    it.pathEffect,
                    it.alpha,
                    it.colorFilter,
                    it.blendMode
                )
            }
        }
        content()
    }
}