package com.skellyapps.charts.example.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface Screen: NavKey {
    val name: String get() = Regex("((?<=\\p{Ll})\\p{Lu})|((?!\\A)\\p{Lu}(?>\\p{Ll}))").replace(this::class.simpleName!!) { " ${it.value}" }
    @Serializable
    object Main: Screen
    @Serializable
    object LineChartExamples: Screen
    @Serializable
    object BarChartExamples: Screen
    @Serializable
    object PieChartExamples: Screen
}