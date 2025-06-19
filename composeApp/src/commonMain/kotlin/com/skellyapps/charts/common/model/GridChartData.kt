package com.skellyapps.charts.common.model

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlin.jvm.JvmInline

class GridChartData {
    sealed interface Axis {
        val value: Value
        val gridLines: GridLines?
        val dividerCustomization: DividerCustomization?
        val valueView: @Composable ((value: Double) -> Unit)?

        interface XAxis: Axis

        interface YAxis: Axis {
            val offset: DpOffset
            val minValue: Double?
            val maxValue: Double?
        }

        sealed interface Value {
            fun getValues(minValue: ChartValueCoordinate, maxValue: ChartValueCoordinate): List<ChartValueCoordinate>
            @JvmInline
            value class Step(val step: Double): Value {
                init {
                    if (step <= 0) {
                        throw IllegalArgumentException("Step must be greater than 0")
                    }
                }
                override fun getValues(minValue: ChartValueCoordinate, maxValue: ChartValueCoordinate): List<ChartValueCoordinate> {
                    val values = mutableListOf<ChartValueCoordinate>()
                    var value = minValue
                    while (value <= maxValue) {
                        values.add(value)
                        value += ChartValueCoordinate(step)
                    }
                    return values
                }
            }
            @JvmInline
            value class Fixed(val values: Int): Value {
                init {
                    if (values < 1) {
                        throw IllegalArgumentException("Values must be greater than 0")
                    }
                }
                override fun getValues(minValue: ChartValueCoordinate, maxValue: ChartValueCoordinate) = when (values) {
                    1 -> listOf(minValue)
                    else -> (0..<values).map { ChartValueCoordinate(minValue.value + (maxValue - minValue).value * it / (values - 1).toDouble()) }
                }
            }
        }

        data class GridLines(
            val showFirstLine: Boolean = true,
            val showLastLine: Boolean = true,
            val customization: DividerCustomization
        )

        data class DividerCustomization(
            val brush: Brush,
            val thickness: Dp = 2.dp,
            val cap: StrokeCap = Stroke.DefaultCap,
            val pathEffect: PathEffect? = null,
            @FloatRange val alpha: Float = 1f,
            val colorFilter: ColorFilter? = null,
            val blendMode: BlendMode = DefaultBlendMode
        ) {
            constructor(
                color: Color,
                thickness: Dp = 2.dp,
                cap: StrokeCap = Stroke.DefaultCap,
                pathEffect: PathEffect? = null,
                @FloatRange alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ): this(SolidColor(color), thickness, cap, pathEffect, alpha, colorFilter, blendMode)
        }
    }
}