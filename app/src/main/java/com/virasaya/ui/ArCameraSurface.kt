package com.virasaya.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.ar.core.Session
import com.virasaya.ar.ArRenderer
import java.util.concurrent.atomic.AtomicBoolean
import com.virasaya.ar.ArTrackingStatus

private class ArGlView(context: Context, val renderer: ArRenderer) : GLSurfaceView(context) {
    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = false
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}

@Composable
fun ArCameraSurface(session: Session, onStatus: (ArTrackingStatus) -> Unit, modifier: Modifier = Modifier, onView: (GLSurfaceView, ArRenderer) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val viewState = remember(session) { mutableStateOf<ArGlView?>(null) }
    val renderer = remember(session) { ArRenderer(session, onStatus) { viewState.value?.display?.rotation ?: Surface.ROTATION_0 } }
    val sessionResumed = remember(session) { AtomicBoolean(false) }
    fun resumeSession() {
        val view = viewState.value ?: return
        if (!sessionResumed.compareAndSet(false, true)) return
        try {
            session.resume()
            view.onResume()
        } catch (error: Exception) {
            sessionResumed.set(false)
            onStatus(ArTrackingStatus(error = error.message ?: "Could not resume the AR camera."))
        }
    }
    fun pauseSession() {
        val view = viewState.value ?: return
        if (!sessionResumed.compareAndSet(true, false)) return
        view.onPause()
        try { session.pause() } catch (_: Exception) { }
    }
    AndroidView(
        modifier = modifier,
        factory = { context -> ArGlView(context, renderer).also { view ->
            viewState.value = view
            onView(view, renderer)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) resumeSession()
        } },
        update = { onView(it, renderer) },
    )
    DisposableEffect(lifecycle, session) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) resumeSession() else onStatus(ArTrackingStatus(error = "Camera permission was removed. Re-enable it or use 2D directions."))
                Lifecycle.Event.ON_PAUSE -> pauseSession()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            viewState.value?.queueEvent { renderer.release() }
            pauseSession()
            try { session.close() } catch (_: Exception) { }
        }
    }
}
