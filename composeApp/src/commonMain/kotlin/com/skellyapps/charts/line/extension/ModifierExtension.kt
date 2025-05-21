package com.skellyapps.charts.line.extension

import androidx.compose.ui.Modifier

fun Modifier.`if`(condition: Boolean, then: Modifier): Modifier {
    return if (condition) {
        this then then
    } else {
        this
    }
}