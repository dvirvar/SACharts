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
import com.skellyapps.charts.example.view.pie.DynamicLabelsPieChartExample
import com.skellyapps.charts.example.view.pie.SimpleLabelsPieChartExample
import com.skellyapps.charts.example.view.pie.SimplePieChartExample

@Composable
internal fun PieChartExamplesScreen(navController: NavController) {
    Scaffold(Modifier.fillMaxSize().onMouseBackButton { navController.popBackStack() }, {
        TopAppBar({Text(Screen.PieChartExamples.name)}, navigationIcon = { IconButton({navController.popBackStack()}) { Icon(Icons.AutoMirrored.Default.ArrowBack, "Back") } })
    }) {
        LazyColumn(Modifier.fillMaxSize().padding(it), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                ColumnItem("Simple pie chart") {
                    SimplePieChartExample()
                }
            }
            item {
                ColumnItem("Simple labels pie chart") {
                    SimpleLabelsPieChartExample()
                }
            }
            item {
                ColumnItem("Dynamic labels pie chart") {
                    DynamicLabelsPieChartExample()
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