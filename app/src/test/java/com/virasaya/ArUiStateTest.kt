package com.virasaya

import com.virasaya.ar.ArTrackingStatus
import com.virasaya.ar.ArAvailabilityState
import com.virasaya.ar.ArSupportResult
import com.virasaya.ar.ArTrackingIssue
import com.virasaya.ar.canAttemptSetup
import com.virasaya.ar.clampRouteStep
import com.virasaya.ar.toAvailabilityState
import com.virasaya.ar.trackingRecoveryMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArUiStateTest {
    @Test fun unknownSupportCheckIsRetryableRatherThanReportedAsUnsupported() {
        assertEquals(ArAvailabilityState.Supported, ArSupportResult.Supported.toAvailabilityState())
        assertEquals(ArAvailabilityState.Unsupported, ArSupportResult.Unsupported.toAvailabilityState())
        assertEquals(ArAvailabilityState.Error, ArSupportResult.Unknown.toAvailabilityState())
    }

    @Test fun arSetupIsAvailableOnlyAfterSupportAndCanRetryInstallOrPermission() {
        assertTrue(ArAvailabilityState.Supported.canAttemptSetup)
        assertTrue(ArAvailabilityState.NeedsCameraPermission.canAttemptSetup)
        assertTrue(ArAvailabilityState.NeedsServices.canAttemptSetup)
        assertFalse(ArAvailabilityState.Checking.canAttemptSetup)
        assertFalse(ArAvailabilityState.Unsupported.canAttemptSetup)
        assertFalse(ArAvailabilityState.Error.canAttemptSetup)
    }

    @Test fun guidanceRequiresBothCameraAndAnchorTracking() {
        assertFalse(ArTrackingStatus().canShowGuidance)
        assertFalse(ArTrackingStatus(cameraTracking = true).canShowGuidance)
        assertFalse(ArTrackingStatus(anchorTracking = true).canShowGuidance)
        assertTrue(ArTrackingStatus(cameraTracking = true, anchorTracking = true).canShowGuidance)
    }

    @Test fun rendererFailureAlwaysHidesGuidance() {
        assertFalse(ArTrackingStatus(cameraTracking = true, anchorTracking = true, error = "camera unavailable").canShowGuidance)
    }

    @Test fun pausedTrackingHasCauseSpecificRecoveryGuidance() {
        assertTrue(trackingRecoveryMessage(ArTrackingIssue.InsufficientLight).contains("brighter"))
        assertTrue(trackingRecoveryMessage(ArTrackingIssue.InsufficientFeatures).contains("textured"))
        assertTrue(trackingRecoveryMessage(ArTrackingIssue.ExcessiveMotion).contains("slowly"))
        assertTrue(trackingRecoveryMessage(ArTrackingIssue.CameraUnavailable).contains("another app"))
        assertTrue(trackingRecoveryMessage(ArTrackingIssue.BadState).contains("restart"))
    }

    @Test fun restoredStepIsBoundedByCurrentRoute() {
        assertEquals(0, clampRouteStep(-4, 6))
        assertEquals(3, clampRouteStep(8, 4))
        assertEquals(0, clampRouteStep(8, 0))
    }
}
