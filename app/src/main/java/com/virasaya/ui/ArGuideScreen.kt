package com.virasaya.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.opengl.GLSurfaceView
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.virasaya.ar.ArAvailabilityState
import com.virasaya.ar.ArTrackingStatus
import com.virasaya.ar.ArSupportResult
import com.virasaya.ar.canAttemptSetup
import com.virasaya.ar.toAvailabilityState
import com.virasaya.ar.trackingRecoveryMessage
import com.virasaya.data.SampleLandmarks
import com.virasaya.navigation.WalkingNetwork
import com.virasaya.navigation.instructions
import com.virasaya.ui.theme.Ink
import com.virasaya.ui.theme.Muted
import com.virasaya.ui.theme.Orange
import com.virasaya.ui.theme.Panel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun ArGuideScreen(
    initialStart: String,
    destinationId: String,
    initialStep: Int,
    network: WalkingNetwork?,
    onExit: (Int) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val route = remember(network, initialStart, destinationId) { network?.route(initialStart, destinationId) }
    val destination = SampleLandmarks.byId(destinationId)
    val steps = remember(route, destination?.name) { route?.instructions(destination?.name.orEmpty()).orEmpty() }
    var stepIndex by rememberSaveable { mutableIntStateOf(initialStep.coerceIn(0, (steps.size - 1).coerceAtLeast(0))) }
    var availability by remember { mutableStateOf(ArAvailabilityState.Checking) }
    var availabilityAttempt by remember { mutableIntStateOf(0) }
    var availabilityMessage by remember { mutableStateOf("Checking whether AR is supported on this phone…") }
    var consent by rememberSaveable { mutableStateOf(false) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var permissionPermanentlyDenied by rememberSaveable { mutableStateOf(false) }
    var installRequested by rememberSaveable { mutableStateOf(true) }
    var session by remember { mutableStateOf<Session?>(null) }
    var sessionError by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf(ArTrackingStatus()) }
    var view by remember { mutableStateOf<GLSurfaceView?>(null) }
    var renderer by remember { mutableStateOf<com.virasaya.ar.ArRenderer?>(null) }
    var confirmedStep by rememberSaveable { mutableStateOf(false) }
    val activeStep = steps.getOrNull(stepIndex)

    LaunchedEffect(activity, availabilityAttempt) {
        if (activity == null) {
            availability = ArAvailabilityState.Error
            availabilityMessage = "AR needs an Android activity. Use the 2D route instead."
            return@LaunchedEffect
        }
        availability = ArAvailabilityState.Checking
        try {
            val coreAvailability = suspendCancellableCoroutine { continuation ->
                ArCoreApk.getInstance().checkAvailabilityAsync(activity) { result ->
                    if (continuation.isActive) continuation.resume(result)
                }
            }
            val supportResult = when {
                coreAvailability.isSupported -> ArSupportResult.Supported
                coreAvailability == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> ArSupportResult.Unsupported
                else -> ArSupportResult.Unknown
            }
            availability = supportResult.toAvailabilityState()
            availabilityMessage = when (supportResult) {
                ArSupportResult.Supported -> "ARCore is available. Camera access begins only after you continue."
                ArSupportResult.Unsupported -> "ARCore is not supported on this device. Your 2D route is ready."
                ArSupportResult.Unknown -> "ARCore support could not be confirmed. You can retry or continue with 2D directions."
            }
        } catch (error: Exception) {
            availability = ArAvailabilityState.Error
            availabilityMessage = error.message ?: "Could not check AR support. Your 2D route is ready."
        }
    }

    fun startSession() {
        if (activity == null) {
            sessionError = "AR is unavailable in this screen. Use the 2D route."
            return
        }
        try {
            when (ArCoreApk.getInstance().requestInstall(activity, installRequested)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    installRequested = false
                    availability = ArAvailabilityState.NeedsServices
                    availabilityMessage = "Finish installing or updating Google Play Services for AR, then tap Try again."
                    return
                }
                ArCoreApk.InstallStatus.INSTALLED -> installRequested = false
            }
            Session(context).also { arSession ->
                arSession.configure(arSession.config.apply {
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                    instantPlacementMode = Config.InstantPlacementMode.DISABLED
                })
                session = arSession
                availability = ArAvailabilityState.Ready
                sessionError = null
            }
        } catch (error: UnavailableUserDeclinedInstallationException) {
            installRequested = false
            availability = ArAvailabilityState.NeedsServices
            availabilityMessage = "AR services were not installed. Your route is still available in 2D."
        } catch (error: Exception) {
            sessionError = error.message ?: "Could not start the AR session. Return to the 2D route."
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            permissionPermanentlyDenied = false
            startSession()
        }
        else {
            availability = ArAvailabilityState.NeedsCameraPermission
            permissionPermanentlyDenied = activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) != true
            sessionError = if (!permissionPermanentlyDenied)
                "Camera access was declined. You can try again or continue with 2D directions."
            else "Camera access is off for Virasaya. Enable it in Android settings or continue with 2D directions."
        }
    }

    fun continueToCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            permissionPermanentlyDenied = false
            startSession()
        } else {
            permissionRequested = true
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    fun clearPlacement() {
        view?.queueEvent { renderer?.clearAnchor() }
        status = status.copy(anchorTracking = false, placementMissed = false)
    }

    Box(Modifier.fillMaxSize().background(Ink)) {
        session?.let { activeSession ->
            ArCameraSurface(activeSession, { status = it }, Modifier.fillMaxSize()) { glView, glRenderer ->
                view = glView
                renderer = glRenderer
            }
            Box(Modifier.fillMaxSize().pointerInput(confirmedStep, sessionError, status.cameraTracking) {
                detectTapGestures { position ->
                    if (position.y in (size.height * .18f)..(size.height * .70f) && confirmedStep && status.cameraTracking && sessionError == null && status.error == null) {
                        view?.queueEvent { renderer?.requestPlacement(position.x, position.y) }
                    }
                }
            }) {
                if (confirmedStep && status.cameraTracking && !status.anchorTracking && status.error == null) {
                    Surface(Modifier.align(Alignment.Center), color = Panel.copy(alpha = .78f), shape = MaterialTheme.shapes.medium) {
                        Text(
                            if (status.planeDetected) "Tap inside the tracked floor" else "Scan a textured floor until AR detects it",
                            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = Orange,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }

        Column(Modifier.fillMaxWidth().align(Alignment.TopCenter).background(Panel.copy(alpha = .94f)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundAction("back", "Use 2D directions", onClick = { onExit(stepIndex) })
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Local AR preview", style = MaterialTheme.typography.titleLarge)
                    Text("Align manually · no GPS positioning", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (activeStep != null) {
                Text("${SampleLandmarks.byId(initialStart)?.name} → ${destination?.name}", Modifier.padding(top = 12.dp), color = Orange, style = MaterialTheme.typography.labelMedium)
                Text("Step ${stepIndex + 1} of ${steps.size}: ${activeStep.text}", Modifier.padding(top = 4.dp), style = MaterialTheme.typography.titleMedium)
            } else Text("Route instructions are unavailable. Return to the 2D route.", Modifier.padding(top = 12.dp), color = Muted)
        }

        Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Panel.copy(alpha = .96f)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                session == null -> {
                    Text(availabilityMessage, color = Muted, style = MaterialTheme.typography.bodyMedium)
                    sessionError?.let { Text(it, color = Orange, style = MaterialTheme.typography.bodySmall) }
                    if (!consent && availability == ArAvailabilityState.Supported) {
                        Text("This application runs on Google Play Services for AR (ARCore), which is provided by Google and governed by the Google Privacy Policy. The camera tracks nearby surfaces while the preview is open.", color = Muted, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { openLink(context, "https://policies.google.com/privacy") }, contentPadding = PaddingValues(0.dp)) { Text("Google Privacy Policy") }
                        TextButton(onClick = { openLink(context, "https://developers.google.com/ar/develop/privacy-requirements") }, contentPadding = PaddingValues(0.dp)) { Text("ARCore privacy details") }
                        Button(onClick = { consent = true; continueToCamera() }, modifier = Modifier.fillMaxWidth()) { Text("Continue to camera") }
                    } else if (consent && availability.canAttemptSetup) {
                        Button(onClick = { continueToCamera() }, modifier = Modifier.fillMaxWidth()) {
                            Text(when (availability) {
                                ArAvailabilityState.NeedsServices -> "Check AR services again"
                                ArAvailabilityState.NeedsCameraPermission -> "Try camera again"
                                else -> if (permissionRequested) "Try AR again" else "Enable camera"
                            })
                        }
                        if (permissionPermanentlyDenied) {
                            OutlinedButton(onClick = {
                                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                                context.startActivity(settingsIntent)
                            }, modifier = Modifier.fillMaxWidth()) { Text("Open camera permission settings") }
                        }
                    } else if (availability == ArAvailabilityState.Checking) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    } else if (availability == ArAvailabilityState.Error) {
                        OutlinedButton(onClick = { availabilityAttempt++ }, modifier = Modifier.fillMaxWidth()) { Text("Check again") }
                    }
                }
                else -> {
                    val guidanceReady = confirmedStep && status.canShowGuidance
                    val statusText = when {
                        sessionError != null -> sessionError!!
                        status.error != null -> "${status.error} Use the 2D route or retry AR."
                        !status.cameraTracking -> trackingRecoveryMessage(status.trackingIssue)
                        status.anchorPlaced && !status.anchorTracking -> "The floor anchor lost tracking, so the arrow is hidden. Move your hand out of view and slowly scan the floor; tap the tracked floor again to place a new marker."
                        !status.anchorTracking -> if (!status.planeDetected && confirmedStep && status.placementMissed) "No large tracked floor plane is available at that tap. Slowly scan a textured floor, keep hands and small objects out of view, then tap again." else if (!status.planeDetected && confirmedStep) "No large tracked floor plane yet. Slowly scan a well-lit, textured floor; the arrow appears only after AR tracks a broad floor surface." else if (!status.planeDetected) "No large tracked floor plane yet. Slowly scan a well-lit, textured floor; keep hands and small objects out of view." else if (confirmedStep && status.placementMissed) "That tap missed the tracked floor plane. Tap inside the floor area, away from hands or small objects." else if (confirmedStep) "Tap inside the tracked floor plane to place the marker." else "Confirm this step before placing a marker."
                        guidanceReady -> "Marker tracked. Rotate it to match the walkway you can see."
                        else -> "Tracking is recovering. Guidance is paused."
                    }
                    Text(statusText, color = if (guidanceReady) Orange else Muted, style = MaterialTheme.typography.bodyMedium)
                    if (status.anchorTracking && status.cameraTracking) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { renderer?.let { r -> view?.queueEvent { r.rotate(-15f) } } }, modifier = Modifier.weight(1f)) { Text("Rotate left") }
                            OutlinedButton(onClick = { renderer?.let { r -> view?.queueEvent { r.rotate(15f) } } }, modifier = Modifier.weight(1f)) { Text("Rotate right") }
                            OutlinedButton(onClick = { clearPlacement(); confirmedStep = false }, modifier = Modifier.weight(1f)) { Text("Realign") }
                        }
                    }
                    if (status.error != null || sessionError != null) {
                        OutlinedButton(onClick = { session = null; status = ArTrackingStatus(); sessionError = null; availability = ArAvailabilityState.Supported }, modifier = Modifier.fillMaxWidth()) { Text("Restart AR setup") }
                    }
                    if (!confirmedStep) Button(onClick = { clearPlacement(); confirmedStep = true }, modifier = Modifier.fillMaxWidth()) { Text("Confirm current step") }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            if (stepIndex > 0) { clearPlacement(); confirmedStep = false; stepIndex-- }
                        }, enabled = stepIndex > 0, modifier = Modifier.weight(1f)) { Text("Previous") }
                        Button(onClick = {
                            if (stepIndex < steps.lastIndex) { clearPlacement(); confirmedStep = false; stepIndex++ }
                            else onExit(stepIndex)
                        }, modifier = Modifier.weight(1f)) { Text(if (stepIndex < steps.lastIndex) "Next step" else "Finish") }
                    }
                }
            }
            TextButton(onClick = { onExit(stepIndex) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Use 2D directions") }
        }
    }
}
