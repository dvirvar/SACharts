package com.skellyapps.charts.bar.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextDecoration
import com.skellyapps.charts.common.model.Position

object BarChartDrawHelper {
    context(d: DrawScope)
    fun drawTextOutside(
        textLayoutResult: TextLayoutResult,
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
        val y: Float = when {
            Position.Top in position -> if (isNegative) barRect.bottom - textHeight else barRect.top
            Position.Bottom in position -> if (isNegative) barRect.top else barRect.bottom - textHeight
            else -> barRect.top + barRect.height / 2f - textHeight / 2f
        }
        val x: Float = when {
            Position.Left in position -> barRect.left
            Position.Right in position -> barRect.right - textWidth
            else -> barRect.left + barRect.width / 2f - textWidth / 2f
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
}