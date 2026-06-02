@file:OptIn(ExperimentalFoundationApi::class)

package com.skellyapps.charts.example

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.onClick
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton

actual fun Modifier.onMouseBackButton(onClick: () -> Unit): Modifier {
    return this.onClick(matcher = PointerMatcher.mouse(PointerButton.Back), onClick = onClick)
}