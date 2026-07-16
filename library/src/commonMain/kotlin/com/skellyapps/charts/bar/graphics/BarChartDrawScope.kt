package com.skellyapps.charts.bar.graphics

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextDecoration
import com.skellyapps.charts.common.model.Position

@LayoutScopeMarker
interface BarChartDrawScope : DrawScope {
    fun drawTextOutside(
        textLayoutResult: TextLayoutResult,
        canvasSize: Size,
        barRect: Rect,
        stayInCanvasBounds: Boolean,
        isNegative: Boolean,
        color: Color = Color.Unspecified,
        alpha: Float = Float.NaN,
        shadow: Shadow? = null,
        textDecoration: TextDecoration? = null,
        drawStyle: DrawStyle? = null,
        blendMode: BlendMode = DrawScope.DefaultBlendMode
    ) {
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height
        var x = barRect.left + barRect.width / 2f - textWidth / 2f
        var y = if (isNegative) {
            barRect.bottom
        } else {
            barRect.top - textHeight
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

    fun drawTextInside(
        textLayoutResult: TextLayoutResult,
        barRect: Rect,
        position: Position,
        isNegative: Boolean,
        color: Color = Color.Unspecified,
        alpha: Float = Float.NaN,
        shadow: Shadow? = null,
        textDecoration: TextDecoration? = null,
        drawStyle: DrawStyle? = null,
        blendMode: BlendMode = DrawScope.DefaultBlendMode
    ) {
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height
        var x: Float
        var y: Float
        when (position) {
            Position.TopLeft -> {
                x = barRect.left
                y = if (isNegative) barRect.bottom - textHeight else barRect.top
            }
            Position.Top -> {
                x = barRect.left + barRect.width / 2f - textWidth / 2f
                y = if (isNegative) barRect.bottom - textHeight else barRect.top
            }
            Position.TopRight -> {
                x = barRect.right - textWidth
                y = if (isNegative) barRect.bottom - textHeight else barRect.top
            }
            Position.MiddleLeft -> {
                x = barRect.left
                y = barRect.top + barRect.height / 2f - textHeight / 2f
            }
            Position.Middle -> {
                x = barRect.left + barRect.width / 2f - textWidth / 2f
                y = barRect.top + barRect.height / 2f - textHeight / 2f
            }
            Position.MiddleRight -> {
                x = barRect.right - textWidth
                y = barRect.top + barRect.height / 2f - textHeight / 2f
            }
            Position.BottomLeft -> {
                x = barRect.left
                y = if (isNegative) barRect.top else barRect.bottom - textHeight
            }
            Position.Bottom -> {
                x = barRect.left + barRect.width / 2f - textWidth / 2f
                y = if (isNegative) barRect.top else barRect.bottom - textHeight
            }
            Position.BottomRight -> {
                x = barRect.right - textWidth
                y = if (isNegative) barRect.top else barRect.bottom - textHeight
            }
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

internal class BarChartDrawScopeImpl(
    private val drawScope: DrawScope
) : BarChartDrawScope, DrawScope by drawScope