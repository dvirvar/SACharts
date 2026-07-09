package com.skellyapps.charts.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skellyapps.charts.example.screen.BarChartExamplesScreen
import com.skellyapps.charts.example.screen.LineChartExamplesScreen
import com.skellyapps.charts.example.screen.MainScreen
import com.skellyapps.charts.example.screen.PieChartExamplesScreen
import com.skellyapps.charts.example.screen.Screen

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController, Screen.Main, Modifier.fillMaxSize()) {
            composable<Screen.Main> {
                MainScreen(navController)
            }
            composable<Screen.LineChartExamples> {
                LineChartExamplesScreen(navController)
            }
            composable<Screen.BarChartExamples> {
                BarChartExamplesScreen(navController)
            }
            composable<Screen.PieChartExamples> {
                PieChartExamplesScreen(navController)
            }
        }
    }
}