package com.skellyapps.charts.pie.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextDecoration
import kotlin.math.cos
import kotlin.math.sin

object PieChartDrawHelper {
    context(d: DrawScope)
    fun drawTextInMiddle(
        textLayoutResult: TextLayoutResult,
        centerX: Float,
        centerY: Float,
        outerRadius: Float,
        innerRadius: Float,
        middleRad: Double,
        hasMoreThanOneSlice: Boolean,
        color: Color = Color.Unspecified,
        alpha: Float = Float.NaN,
        shadow: Shadow? = null,
        textDecoration: TextDecoration? = null,
        drawStyle: DrawStyle? = null,
        blendMode: BlendMode = DrawScope.DefaultBlendMode
    ) {
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height
        val middlePointX: Float
        val middlePointY: Float
        if (hasMoreThanOneSlice) {
            val middleRadius = (outerRadius + innerRadius) / 2f
            middlePointX = (centerX + middleRadius * cos(middleRad)).toFloat()
            middlePointY = (centerY + middleRadius * sin(middleRad)).toFloat()
        } else {
            middlePointX = centerX
            middlePointY = centerY
        }
        val x = middlePointX - textWidth / 2f
        val y = middlePointY - textHeight / 2f
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