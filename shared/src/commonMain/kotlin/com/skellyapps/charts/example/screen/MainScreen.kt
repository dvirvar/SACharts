@file:OptIn(ExperimentalMaterial3Api::class)

package com.skellyapps.charts.example.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.navigation.NavController

private val exampleScreens = listOf(Screen.LineChartExamples, Screen.BarChartExamples, Screen.PieChartExamples)

@Composable
internal fun MainScreen(navController: NavController) {
    Scaffold(Modifier.fillMaxSize(), {
        TopAppBar({Text(Screen.Main.name)})
    }) {
        FlowRow(Modifier.fillMaxSize().padding(it), Arrangement.spacedBy(6.dp)) {
            exampleScreens.fastForEach {
                Button({navController.navigate(it)}) {
                    Text(it.name)
                }
            }
        }
    }
}