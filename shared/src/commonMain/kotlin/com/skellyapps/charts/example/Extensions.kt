package com.skellyapps.charts.example

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.util.fastRoundToInt
import com.skellyapps.charts.common.model.ChartValueCoordinate
import kotlin.jvm.JvmName
import kotlin.math.max
import kotlin.math.pow

internal fun Double.roundToDecimals(decimals: Int): Double {
    val divider = 10.0.pow(decimals)
    return (this * divider).fastRoundToInt() / divider
}

internal inline fun ChartValueCoordinate.roundToDecimals(decimals: Int): Double {
    return value.roundToDecimals(decimals)
}

@JvmName("arrowValueStepperUInt")
internal fun Modifier.arrowValueStepper(text: String, defaultValue: UInt, minValue: UInt = 0U, textChange: (String) -> Unit): Modifier {
    return this.onPreviewKeyEvent {
        if (it.type == KeyEventType.KeyDown) {
            if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                var r = (text.toUIntOrNull() ?: defaultValue)
                if (it.key == Key.DirectionUp) {
                    r += 1U
                } else {
                    r = max(r - 1U, minValue)
                }
                textChange(r.toString())
                true
            } else {
                false
            }
        } else {
            false
        }
    }
}
@JvmName("arrowValueStepperFloat")
internal fun Modifier.arrowValueStepper(text: String, defaultValue: Float, minValue: Float = 0f, textChange: (String) -> Unit): Modifier {
    return this.onPreviewKeyEvent {
        if (it.type == KeyEventType.KeyDown) {
            if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                var r = (text.toFloatOrNull() ?: defaultValue)
                if (it.key == Key.DirectionUp) {
                    r += 1f
                } else {
                    r = max(r - 1f, minValue)
                }
                textChange(r.toString())
                true
            } else {
                false
            }
        } else {
            false
        }
    }
}
@JvmName("arrowValueStepperDouble")
internal fun Modifier.arrowValueStepper(text: String, defaultValue: Double, minValue: Double = 0.0, textChange: (String) -> Unit): Modifier {
    return this.onPreviewKeyEvent {
        if (it.type == KeyEventType.KeyDown) {
            if (it.key == Key.DirectionUp || it.key == Key.DirectionDown) {
                var r = (text.toDoubleOrNull() ?: defaultValue)
                if (it.key == Key.DirectionUp) {
                    r += 1.0
                } else {
                    r = max(r - 1.0, minValue)
                }
                textChange(r.toString())
                true
            } else {
                false
            }
        } else {
            false
        }
    }
}