package com.skellyapps.charts.bar.graphics

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.ui.geometry.Offset
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
        topLeft: Offset,
        barSize: Size,
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
            topLeft.x - textWidth
        } else {
            topLeft.x + barSize.width
        }
        var y = topLeft.y + barSize.height / 2f - textHeight / 2f
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
        topLeft: Offset,
        barSize: Size,
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
                x = if (isNegative) topLeft.x + barSize.width - textWidth else topLeft.x
                y = topLeft.y
            }
            Position.Top -> {
                x = topLeft.x + barSize.width / 2f - textWidth / 2f
                y = topLeft.y
            }
            Position.TopRight -> {
                x = if (isNegative) topLeft.x else topLeft.x + barSize.width - textWidth
                y = topLeft.y
            }
            Position.MiddleLeft -> {
                x = if (isNegative) topLeft.x + barSize.width - textWidth else topLeft.x
                y = topLeft.y + barSize.height / 2f - textHeight / 2f
            }
            Position.Middle -> {
                x = topLeft.x + barSize.width / 2f - textWidth / 2f
                y = topLeft.y + barSize.height / 2f - textHeight / 2f
            }
            Position.MiddleRight -> {
                x = if (isNegative) topLeft.x else topLeft.x + barSize.width - textWidth
                y = topLeft.y + barSize.height / 2f - textHeight / 2f
            }
            Position.BottomLeft -> {
                x = if (isNegative) topLeft.x + barSize.width - textWidth else topLeft.x
                y = topLeft.y + barSize.height - textHeight
            }
            Position.Bottom -> {
                x = topLeft.x + barSize.width / 2f - textWidth / 2f
                y = topLeft.y + barSize.height - textHeight
            }
            Position.BottomRight -> {
                x = if (isNegative) topLeft.x else topLeft.x + barSize.width - textWidth
                y = topLeft.y + barSize.height - textHeight
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

internal class HorizontalBarChartDrawScopeImpl(
    private val drawScope: DrawScope
) : HorizontalBarChartDrawScope, DrawScope by drawScope