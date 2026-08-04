package com.skellyapps.charts.line.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextDecoration
import com.skellyapps.charts.common.model.ChartValue
import com.skellyapps.charts.common.model.ChartValueCoordinate
import com.skellyapps.charts.common.model.Position

class LineChartDrawHelper internal constructor(
    private val xAxisViewport: ChartValue,
    private val leftAxisYViewport: ChartValue,
    private val rightAxisYViewport: ChartValue,
)  {
    context(d: DrawScope)
    inline fun drawSquare(
        brush: Brush,
        offset: Offset,
        size: Float,
        alpha: Float = 1f,
        style: DrawStyle = Fill,
        colorFilter: ColorFilter? = null,
        blendMode: BlendMode = DrawScope.DefaultBlendMode
    ) {
        d.drawRect(
            brush,
            offset - Offset(size / 2f, size / 2f),
            Size(size, size),
            alpha,
            style,
            colorFilter,
            blendMode
        )
    }

    context(d: DrawScope)
    inline fun drawSquare(
        color: Color,
        offset: Offset,
        size: Float,
        alpha: Float = 1f,
        style: DrawStyle = Fill,
        colorFilter: ColorFilter? = null,
        blendMode: BlendMode = DrawScope.DefaultBlendMode
    ) {
        d.drawRect(
            color,
            offset - Offset(size / 2f, size / 2f),
            Size(size, size),
            alpha,
            style,
            colorFilter,
            blendMode
        )
    }

    context(d: DrawScope)
    fun drawText(
        textLayoutResult: TextLayoutResult,
        offset: Offset,
        position: Position,
        stayInCanvasBounds: Boolean,
        color: Color = Color.Unspecified,
        alpha: Float = Float.NaN,
        shadow: Shadow? = null,
        textDecoration: TextDecoration? = null,
        drawStyle: DrawStyle? = null,
        blendMode: BlendMode = DrawScope.DefaultBlendMode
    ) {
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height
        var y: Float = when {
            Position.Top in position -> offset.y - textHeight
            Position.Bottom in position -> offset.y
            else -> offset.y - textHeight / 2f
        }
        var x: Float = when {
            Position.Left in position -> offset.x - textWidth
            Position.Right in position -> offset.x
            else -> offset.x - textWidth / 2f
        }
        if (stayInCanvasBounds) {
            x = x.coerceIn(0f, d.size.width - textWidth)
            y = y.coerceIn(0f, d.size.height - textHeight)
        }
        d.drawText(
            textLayoutResult,
            color,
            Offset(x, y),
            alpha,
            shadow,
            textDecoration,
            drawStyle,
            blendMode
        )
    }

    context(d: DrawScope)
    fun ChartValue.toOffset(isLeftAxis: Boolean): Offset {
        val yAxis = if(isLeftAxis) leftAxisYViewport else rightAxisYViewport
        return toChartPixel(d.size, xAxisViewport.x, xAxisViewport.y, yAxis.x, yAxis.y).offset
    }

    context(d: DrawScope)
    fun ChartValueCoordinate.toXPixel(): Float {
        return toChartPixelCoordinate(d.size.width, xAxisViewport.x, xAxisViewport.y, false).value
    }

    context(d: DrawScope)
    fun ChartValueCoordinate.toYPixel(isLeftAxis: Boolean): Float {
        val minCoordinate = if (isLeftAxis) {
            leftAxisYViewport.x
        } else {
            rightAxisYViewport.x
        }
        val maxCoordinate = if (isLeftAxis) {
            leftAxisYViewport.y
        } else {
            rightAxisYViewport.y
        }
        return toChartPixelCoordinate(d.size.height, minCoordinate, maxCoordinate, true).value
    }
}