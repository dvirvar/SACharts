@file:OptIn(ExperimentalMaterial3Api::class)

package com.skellyapps.charts.example.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.skellyapps.charts.example.onMouseBackButton
import com.skellyapps.charts.example.view.line.AxesCustomizationLineChartExample
import com.skellyapps.charts.example.view.line.DividerCustomizationLineChartExample
import com.skellyapps.charts.example.view.line.FunctionalityLineChartExample
import com.skellyapps.charts.example.view.line.GridLineCustomizationLineChartExample
import com.skellyapps.charts.example.view.line.LineCustomizationLineChartExample
import com.skellyapps.charts.example.view.line.SimpleLineChartExample
import com.skellyapps.charts.example.view.line.SimpleTwoAxesLineChartExample

@Composable
internal fun LineChartExamplesScreen(navController: NavController) {
    Scaffold(Modifier.fillMaxSize().onMouseBackButton { navController.popBackStack() }, {
        TopAppBar({Text(Screen.LineChartExamples.name)}, navigationIcon = { IconButton({navController.popBackStack()}) { Icon(Icons.AutoMirrored.Default.ArrowBack, "Back") } })
    }) {
        LazyColumn(Modifier.fillMaxSize().padding(it), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                ColumnItem("Simple 1 vertical axis") {
                    SimpleLineChartExample()
                }
            }
            item {
                ColumnItem("Simple 2 vertical axes") {
                    SimpleTwoAxesLineChartExample()
                }
            }
            item {
                ColumnItem("Line customization") {
                    LineCustomizationLineChartExample()
                }
            }
            item {
                ColumnItem("Grid line customization") {
                    GridLineCustomizationLineChartExample()
                }
            }
            item {
                ColumnItem("Divider customization") {
                    DividerCustomizationLineChartExample()
                }
            }
            item {
                ColumnItem("Axes customization") {
                    AxesCustomizationLineChartExample()
                }
            }
            item {
                ColumnItem("Chart functionality") {
                    FunctionalityLineChartExample()
                }
            }
        }
    }
}

@Composable
private fun ColumnItem(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}