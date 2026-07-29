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
interface HorizontalBarChartDrawScope : DrawScope {
    fun drawTextOutside(
        textLayoutResult: TextLayoutResult,
        canvasSize: Size,
        barRect: Rect,
        stayInCanvasBounds: Boolean,
        isNegative: Boolean,
        isLeftYAxis: Boolean,
        color: Color = Color.Unspecified,
        alpha: Float = Float.NaN,
        shadow: Shadow? = null,
        textDecoration: TextDecoration? = null,
        drawStyle: DrawStyle? = null,
        blendMode: BlendMode = DrawScope.DefaultBlendMode
    ) {
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height
        var x = if (isNegative == isLeftYAxis) {
            barRect.left - textWidth
        } else {
            barRect.right
        }
        var y = barRect.top + barRect.height / 2f - textHeight / 2f
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
        val y: Float = when {
            Position.Top in position -> barRect.top
            Position.Bottom in position -> barRect.bottom - textHeight
            else -> barRect.top + barRect.height / 2f - textHeight / 2f
        }
        val x: Float = when {
            Position.Left in position -> if (isNegative) barRect.right - textWidth else barRect.left
            Position.Right in position -> if (isNegative) barRect.left else barRect.right - textWidth
            else -> barRect.left + barRect.width / 2f - textWidth / 2f
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

internal class HorizontalBarChartDrawScopeImpl(
    private val drawScope: DrawScope
) : HorizontalBarChartDrawScope, DrawScope by drawScope