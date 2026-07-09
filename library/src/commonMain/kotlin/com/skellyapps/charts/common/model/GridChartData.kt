package com.skellyapps.charts.common.model

import androidx.annotation.FloatRange
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

/**
 * Namespace configuration containing structural data modeling paradigms used to style and map grid charts.
 */
class GridChartData {
    /**
     * Common attributes of an axis
     *
     * @param gridLines Grid lines settings
     * @param dividerCustomization
     */
    sealed interface Axis {
        val gridLines: GridLines?
        val dividerCustomization: DividerCustomization?

        interface XAxis: Axis

        /**
         * Common attributes of y-axis
         *
         * @param offset Padding of the values from the start(x) and end(y) of the axis' viewport
         * @param minValue Minimum value of the axis, if null it will be calculated from the data
         * @param maxValue Maximum value of the axis, if null it will be calculated from the data
         */
        interface YAxis: Axis {
            val offset: DpOffset
            val minValue: Double?
            val maxValue: Double?
        }
        /** How and how much value labels to show. */
        sealed interface Value {
            fun getValues(minValue: ChartValueCoordinate, maxValue: ChartValueCoordinate): List<ChartValueCoordinate>
            /** Generates labels from minimum to maximum value by steps.
             *
             * Step must be greater than 0.
             *
             * For example:
             * ```
             * Minimum value: 0
             * Maximum value: 10
             * Step: 3
             * Result: [0, 3, 6, 9]
             * ```
             */
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
            /** Generates a fixed amount of labels from minimum to maximum value,
             *
             * By this formula: (0..<values).map { minValue + (maxValue - minValue) * it / (values - 1).toDouble() }.
             *
             * Values must be greater than 0.
             *
             * For example:
             * ```
             * Minimum value: 0
             * Maximum value: 10
             * Values: 1
             * Result: [0]
             * Or
             * Values: 5
             * Result: [0, 2.5, 5, 7.5, 10]
             * ```
             */
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
        /** Grid lines settings of an axis.
         *
         * @param showFirstLine Show first line in a grid
         * @param showLastLine Show last line in a grid
         * @param customization Customization settings of the lines in a grid
         */
        data class GridLines(
            val showFirstLine: Boolean = true,
            val showLastLine: Boolean = true,
            val customization: DividerCustomization
        )

        /**
         * @param brush The color or fill to be applied to the divider
         * @param thickness Thickness of the divider
         * @param cap Treatment applied to the ends of the divider segment
         * @param pathEffect Optional effect or pattern to apply to the divider
         * @param alpha Alpha to be applied to the [brush] from 0.0f to 1.0f representing fully transparent to fully opaque respectively
         * @param colorFilter [ColorFilter] to apply to the [brush]
         * @param blendMode The blending algorithm to apply to the [brush]
         */
        data class DividerCustomization(
            val brush: Brush,
            val thickness: Dp = 2.dp,
            val cap: StrokeCap = Stroke.DefaultCap,
            val pathEffect: PathEffect? = null,
            @param:FloatRange(0.0, 1.0) val alpha: Float = 1f,
            val colorFilter: ColorFilter? = null,
            val blendMode: BlendMode = DefaultBlendMode
        ) {
            constructor(
                color: Color,
                thickness: Dp = 2.dp,
                cap: StrokeCap = Stroke.DefaultCap,
                pathEffect: PathEffect? = null,
                @FloatRange(0.0, 1.0) alpha: Float = 1f,
                colorFilter: ColorFilter? = null,
                blendMode: BlendMode = DefaultBlendMode,
            ): this(SolidColor(color), thickness, cap, pathEffect, alpha, colorFilter, blendMode)
        }
    }
}