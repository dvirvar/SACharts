@file:OptIn(ExperimentalSerializationApi::class)

package com.skellyapps.charts.example

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.skellyapps.charts.example.screen.BarChartExamplesScreen
import com.skellyapps.charts.example.screen.LineChartExamplesScreen
import com.skellyapps.charts.example.screen.MainScreen
import com.skellyapps.charts.example.screen.PieChartExamplesScreen
import com.skellyapps.charts.example.screen.Screen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Screen>()
        }
    }
}

@Composable
fun App() {
    MaterialTheme {
        val backStack = rememberNavBackStack(config, Screen.Main)
        NavDisplay(
            backStack,
            entryProvider = entryProvider {
                entry<Screen.Main> {
                    MainScreen(backStack)
                }
                entry<Screen.LineChartExamples> {
                    LineChartExamplesScreen(backStack)
                }
                entry<Screen.BarChartExamples> {
                    BarChartExamplesScreen(backStack)
                }
                entry<Screen.PieChartExamples> {
                    PieChartExamplesScreen(backStack)
                }
            }
        )
    }
}