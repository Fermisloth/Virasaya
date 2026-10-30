package com.virasaya.ar

/** UI-visible status only; camera frames remain on the GL thread. */
data class ArTrackingStatus(
    val cameraTracking: Boolean = false,
    val anchorTracking: Boolean = false,
    val anchorPlaced: Boolean = false,
    val planeDetected: Boolean = false,
    val placementMissed: Boolean = false,
    val trackingIssue: ArTrackingIssue? = null,
    val error: String? = null,
) {
    val canShowGuidance: Boolean get() = cameraTracking && anchorTracking && error == null
}

enum class ArTrackingIssue { Initializing, InsufficientLight, InsufficientFeatures, ExcessiveMotion, CameraUnavailable, BadState }

fun trackingRecoveryMessage(issue: ArTrackingIssue?): String = when (issue) {
    ArTrackingIssue.Initializing, null -> "AR is starting. Hold the phone steady for a moment."
    ArTrackingIssue.InsufficientLight -> "Tracking needs more light. Move to a brighter area and check that camera access is enabled."
    ArTrackingIssue.InsufficientFeatures -> "Tracking needs more detail. Scan a textured floor or table; avoid blank surfaces."
    ArTrackingIssue.ExcessiveMotion -> "Tracking lost the scene. Move the phone more slowly."
    ArTrackingIssue.CameraUnavailable -> "The camera is busy in another app. Close it, then retry AR."
    ArTrackingIssue.BadState -> "AR tracking needs to restart. Return to 2D or restart AR."
}

enum class ArAvailabilityState { Checking, Supported, Unsupported, NeedsCameraPermission, NeedsServices, Ready, Error }

enum class ArSupportResult { Supported, Unsupported, Unknown }

fun ArSupportResult.toAvailabilityState(): ArAvailabilityState = when (this) {
    ArSupportResult.Supported -> ArAvailabilityState.Supported
    ArSupportResult.Unsupported -> ArAvailabilityState.Unsupported
    ArSupportResult.Unknown -> ArAvailabilityState.Error
}

val ArAvailabilityState.canAttemptSetup: Boolean
    get() = this == ArAvailabilityState.Supported || this == ArAvailabilityState.NeedsCameraPermission || this == ArAvailabilityState.NeedsServices

fun clampRouteStep(index: Int, stepCount: Int): Int = index.coerceIn(0, (stepCount - 1).coerceAtLeast(0))
