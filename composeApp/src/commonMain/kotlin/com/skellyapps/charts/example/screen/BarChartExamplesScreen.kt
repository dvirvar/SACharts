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
import com.skellyapps.charts.example.view.bar.GroupedBarChartExample
import com.skellyapps.charts.example.view.bar.GroupedHorizontalBarChartExample
import com.skellyapps.charts.example.view.bar.SimpleBarChartExample
import com.skellyapps.charts.example.view.bar.SimpleHorizontalBarChartExample
import com.skellyapps.charts.example.view.bar.StackedBarChartExample
import com.skellyapps.charts.example.view.bar.StackedHorizontalBarChartExample

@Composable
internal fun BarChartExamplesScreen(navController: NavController) {
    Scaffold(Modifier.fillMaxSize().onMouseBackButton { navController.popBackStack() }, {
        TopAppBar({Text(Screen.BarChartExamples.name)}, navigationIcon = { IconButton({navController.popBackStack()}) { Icon(Icons.AutoMirrored.Default.ArrowBack, "Back") } })
    }) {
        LazyColumn(Modifier.fillMaxSize().padding(it), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                ColumnItem("Simple bar chart") {
                    SimpleBarChartExample()
                }
            }
            item {
                ColumnItem("Grouped bar chart") {
                    GroupedBarChartExample()
                }
            }
            item {
                ColumnItem("Stacked bar chart") {
                    StackedBarChartExample()
                }
            }
            item {
                ColumnItem("Simple horizontal bar chart") {
                    SimpleHorizontalBarChartExample()
                }
            }
            item {
                ColumnItem("Grouped horizontal bar chart") {
                    GroupedHorizontalBarChartExample()
                }
            }
            item {
                ColumnItem("Stacked horizontal bar chart") {
                    StackedHorizontalBarChartExample()
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