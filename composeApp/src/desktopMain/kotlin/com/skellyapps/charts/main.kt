package com.skellyapps.charts

import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        state = rememberWindowState(position = WindowPosition(Alignment.Center)),
        onCloseRequest = ::exitApplication,
        title = "SACharts",
    ) {
        App()
    }
}