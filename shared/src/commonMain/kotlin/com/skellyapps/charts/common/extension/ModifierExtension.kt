package com.skellyapps.charts.common.extension

import androidx.compose.ui.Modifier

internal fun Modifier.`if`(condition: Boolean, then: Modifier): Modifier {
    return if (condition) {
        this then then
    } else {
        this
    }
}