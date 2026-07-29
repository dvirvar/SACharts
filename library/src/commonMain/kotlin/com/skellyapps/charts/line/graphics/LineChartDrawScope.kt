package com.skellyapps.charts.line.graphics

import androidx.compose.foundation.layout.LayoutScopeMarker
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
import com.skellyapps.charts.common.model.Position

@LayoutScopeMarker
interface LineChartDrawScope : DrawScope {
    fun drawSquare(
        brush: Brush,
        offset: Offset,
        size: Float,
        alpha: Float = 1f,
        style: DrawStyle = Fill,
        colorFilter: ColorFilter? = null,
        blendMode: BlendMode = DrawScope.DefaultBlendMode
    ) {
        drawRect(
            brush,
            offset - Offset(size / 2f, size / 2f),
            Size(size, size),
            alpha,
            style,
            colorFilter,
            blendMode
        )
    }

    fun drawSquare(
        color: Color,
        offset: Offset,
        size: Float,
        alpha: Float = 1f,
        style: DrawStyle = Fill,
        colorFilter: ColorFilter? = null,
        blendMode: BlendMode = DrawScope.DefaultBlendMode
    ) {
        drawRect(
            color,
            offset - Offset(size / 2f, size / 2f),
            Size(size, size),
            alpha,
            style,
            colorFilter,
            blendMode
        )
    }

    fun drawText(
        textLayoutResult: TextLayoutResult,
        canvasSize: Size,
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
            x = x.coerceIn(0f, canvasSize.width - textWidth)
            y = y.coerceIn(0f, canvasSize.height - textHeight)
        }
        drawText(
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
}

internal class LineChartDrawScopeImpl(
    private val drawScope: DrawScope
) : LineChartDrawScope, DrawScope by drawScope