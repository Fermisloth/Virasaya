package com.virasaya.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import androidx.compose.runtime.collectAsState
import com.virasaya.R
import com.virasaya.data.Landmark
import com.virasaya.data.SampleLandmarks
import com.virasaya.navigation.Routes
import com.virasaya.navigation.WalkingNetwork
import com.virasaya.ui.theme.Ink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LandmarkPhoto(val resource: Int, val subtitle: String, val credit: String, val page: String, val license: String)

fun Landmark.photo() = when (id) {
    "naubat-khana" -> LandmarkPhoto(R.drawable.naubat_khana,"The drum house", "Radosław Botev · CC BY 3.0 PL", "https://commons.wikimedia.org/wiki/File:Naubat_Khana_Red_Fort_Delhi.jpg", "https://creativecommons.org/licenses/by/3.0/pl/deed.en")
    "diwan-i-aam" -> LandmarkPhoto(R.drawable.diwan_i_aam,"Hall of public audience", "Nahid Sultan · CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Diwan-i_Am,_Red_Fort,_New_Delhi_(02).jpg", "https://creativecommons.org/licenses/by-sa/4.0/")
    else -> LandmarkPhoto(R.drawable.lahori_gate,"A gateway to history", "Tamjeed Ahmed · CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Lahori_Gate_of_Red_Fort,_Delhi.jpg", "https://creativecommons.org/licenses/by-sa/4.0/")
}

fun openLink(context: Context, url: String) {
    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    catch (_: ActivityNotFoundException) { Toast.makeText(context,"No app is available to open this link",Toast.LENGTH_LONG).show() }
}

@Composable
fun VirasayaApp() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("visitor", Context.MODE_PRIVATE) }
    var savedText by rememberSaveable { mutableStateOf(preferences.getString("saved", "").orEmpty()) }
    val savedIds = remember(savedText) { savedText.split(',').filter { it.isNotBlank() }.toSet() }
    val toggleSaved: (String) -> Unit = { id ->
        val next = savedIds.toMutableSet().apply { if (!add(id)) remove(id) }
        savedText = next.joinToString(",")
        preferences.edit().putString("saved", savedText).apply()
    }
    val mapLoad by produceState<Result<WalkingNetwork>?>(null) {
        value = withContext(Dispatchers.IO) {
            runCatching { context.assets.open("map/network.tsv").bufferedReader().use { WalkingNetwork.parse(it.readText()) } }
        }
    }
    val nav = rememberNavController()
    Surface(Modifier.fillMaxSize(), color = Ink, contentColor = MaterialTheme.colorScheme.onBackground) {
      Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        NavHost(nav, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                ExploreScreen(savedIds, toggleSaved,
                    onDetails = { nav.navigate(Routes.details(it)) },
                    onMap = { nav.navigate(Routes.route("lahori-gate")) })
            }
            composable(Routes.DETAILS) { entry ->
                val landmark = SampleLandmarks.byId(entry.arguments?.getString("landmarkId"))
                if (landmark != null) DetailScreen(landmark, landmark.id in savedIds,
                    onSave = { toggleSaved(landmark.id) }, onBack = { nav.popBackStack() },
                    onMap = { nav.navigate(Routes.route(landmark.id)) })
            }
            composable(Routes.ROUTE) { entry ->
                val returnedStep by entry.savedStateHandle.getStateFlow("ar_return_step", -1).collectAsState()
                RouteScreen(
                    entry.arguments?.getString("startId") ?: "lahori-gate", mapLoad?.getOrNull(), mapLoad?.isFailure == true,
                    onBack = { nav.popBackStack() },
                    onArPreview = { start, destination, step -> nav.navigate(Routes.ar(start, destination, step)) },
                    restoredStepIndex = returnedStep,
                    onStepRestored = { entry.savedStateHandle["ar_return_step"] = -1 },
                )
            }
            composable(Routes.AR) { entry ->
                ArGuideScreen(
                    initialStart = entry.arguments?.getString("startId") ?: "lahori-gate",
                    destinationId = entry.arguments?.getString("destinationId") ?: "diwan-i-aam",
                    initialStep = entry.arguments?.getString("stepIndex")?.toIntOrNull() ?: 0,
                    network = mapLoad?.getOrNull(),
                    onExit = { step ->
                        nav.previousBackStackEntry?.savedStateHandle?.set("ar_return_step", step)
                        nav.popBackStack()
                    },
                )
            }
        }
      }
    }
}
