package com.skellyapps.charts.example.screen

import kotlinx.serialization.Serializable

internal sealed interface Screen {
    val name: String get() = Regex("((?<=\\p{Ll})\\p{Lu})|((?!\\A)\\p{Lu}(?>\\p{Ll}))").replace(this::class.simpleName!!) { " ${it.value}" }
    @Serializable
    object Main: Screen
    @Serializable
    object LineChartExamples: Screen
    @Serializable
    object BarChartExamples: Screen
}