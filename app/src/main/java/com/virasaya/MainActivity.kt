package com.virasaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.virasaya.data.SampleLandmarks
import com.virasaya.navigation.Routes
import com.virasaya.ui.theme.VirasayaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VirasayaTheme { VirasayaApp() } }
    }
}

@Composable
private fun VirasayaApp() {
    val navController = rememberNavController()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = SampleLandmarks.all.firstOrNull { it.id == selectedId }

    NavHost(navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            Shell("Virasaya") {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Explore Red Fort", style = MaterialTheme.typography.headlineMedium)
                    Text("A simple heritage guide prototype. Recognition and navigation are not implemented yet.")
                    Button(onClick = { navController.navigate(Routes.SELECT) }, modifier = Modifier.fillMaxWidth()) { Text("Select a landmark") }
                }
            }
        }
        composable(Routes.SELECT) {
            Shell("Choose a landmark", onBack = { navController.popBackStack() }) {
                LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { Text("Manual selection", style = MaterialTheme.typography.titleLarge) }
                    item { Text("Pilot landmarks; historical content is pending source verification.") }
                    items(SampleLandmarks.all) { landmark ->
                        OutlinedButton(onClick = { selectedId = landmark.id; navController.navigate(Routes.details(landmark.id)) }, modifier = Modifier.fillMaxWidth()) { Text(landmark.name) }
                    }
                }
            }
        }
        composable(Routes.DETAILS) {
            Shell(selected?.name ?: "Landmark", onBack = { navController.popBackStack() }) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Confirmed landmark", style = MaterialTheme.typography.labelLarge)
                    Text(selected?.name ?: "No landmark selected", style = MaterialTheme.typography.headlineMedium)
                    HorizontalDivider()
                    Text(selected?.historyStatus ?: "No content available")
                    Button(onClick = { navController.navigate(Routes.ROUTE) }, modifier = Modifier.fillMaxWidth()) { Text("Choose destination") }
                }
            }
        }
        composable(Routes.ROUTE) {
            Shell("Route", onBack = { navController.popBackStack() }) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Route placeholder", style = MaterialTheme.typography.headlineMedium)
                    Text("Route calculation, map data, distances, and directions are not implemented.")
                    OutlinedButton(onClick = { navController.popBackStack(Routes.HOME, false) }, modifier = Modifier.fillMaxWidth()) { Text("Return home") }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Shell(title: String, onBack: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(title) }, navigationIcon = {
            if (onBack != null) IconButton(onClick = onBack) { Text("‹") }
        })
    }) { padding -> Column(Modifier.fillMaxSize().padding(padding)) { content() } }
}
