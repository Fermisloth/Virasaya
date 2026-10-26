package com.virasaya.ar

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import com.google.ar.core.InstantPlacementPoint
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.TrackingFailureReason
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** Small camera-background and one procedural, plane-anchored arrow renderer. */
class ArRenderer(
    private val session: Session,
    private val onStatus: (ArTrackingStatus) -> Unit,
    private val displayRotation: () -> Int,
) : GLSurfaceView.Renderer {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val angle = AtomicReference(0f)
    @Volatile private var pendingHit: Pair<Float, Float>? = null
    @Volatile private var placementMissed = false
    @Volatile private var failed: String? = null
    private var anchor: com.google.ar.core.Anchor? = null
    private var instantPlacementPoint: InstantPlacementPoint? = null
    private var cameraProgram = 0
    private var arrowProgram = 0
    private var cameraPositionAttribute = -1
    private var cameraUvAttribute = -1
    private var cameraSamplerUniform = -1
    private var arrowPositionAttribute = -1
    private var arrowMvpUniform = -1
    private var arrowColorUniform = -1
    private var cameraTexture = 0
    private var surfaceWidth = 1
    private var surfaceHeight = 1
    private var lastDisplayRotation = -1
    private var lastDisplayWidth = -1
    private var lastDisplayHeight = -1
    private var lastStatusAt = 0L
    private var lastPlaneCheckAt = 0L
    private var lastStatus = ArTrackingStatus()
    private val view = FloatArray(16)
    private val projection = FloatArray(16)
    private val model = FloatArray(16)
    private val rotated = FloatArray(16)
    private val worldModel = FloatArray(16)
    private val viewModel = FloatArray(16)
    private val mvp = FloatArray(16)
    private val cameraQuad = floatBuffer(floatArrayOf(-1f,-1f, 1f,-1f, -1f,1f, 1f,1f))
    private val cameraUv = floatBuffer(FloatArray(8))
    private val arrow = floatBuffer(floatArrayOf(
        -.025f,.012f, .13f,  .025f,.012f,.13f,  -.025f,.012f,-.09f,
        -.025f,.012f,-.09f,  .025f,.012f,.13f,  .025f,.012f,-.09f,
        -.075f,.014f,-.07f,  .075f,.014f,-.07f,  0f,.014f,-.20f,
    ))

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        try {
            GLES20.glClearColor(0f, 0f, 0f, 1f)
            cameraProgram = program(CAMERA_VERTEX, CAMERA_FRAGMENT)
            arrowProgram = program(ARROW_VERTEX, ARROW_FRAGMENT)
            cameraPositionAttribute = GLES20.glGetAttribLocation(cameraProgram, "a_Position")
            cameraUvAttribute = GLES20.glGetAttribLocation(cameraProgram, "a_TexCoord")
            cameraSamplerUniform = GLES20.glGetUniformLocation(cameraProgram, "u_Texture")
            arrowPositionAttribute = GLES20.glGetAttribLocation(arrowProgram, "a_Position")
            arrowMvpUniform = GLES20.glGetUniformLocation(arrowProgram, "u_Mvp")
            arrowColorUniform = GLES20.glGetUniformLocation(arrowProgram, "u_Color")
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            cameraTexture = textures[0]
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            session.setCameraTextureNames(intArrayOf(cameraTexture))
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        } catch (error: Exception) {
            failed = error.message ?: "Could not prepare the AR camera renderer."
            publish(ArTrackingStatus(error = failed))
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceWidth = width.coerceAtLeast(1)
        surfaceHeight = height.coerceAtLeast(1)
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (failed != null) return
        try {
            val rotation = displayRotation()
            if (rotation != lastDisplayRotation || surfaceWidth != lastDisplayWidth || surfaceHeight != lastDisplayHeight) {
                session.setDisplayGeometry(rotation, surfaceWidth, surfaceHeight)
                lastDisplayRotation = rotation
                lastDisplayWidth = surfaceWidth
                lastDisplayHeight = surfaceHeight
            }
            val frame = session.update()
            drawCamera(frame)
            val camera = frame.camera
            val cameraOk = camera.trackingState == TrackingState.TRACKING
            val trackingIssue = if (cameraOk) null else when (camera.trackingFailureReason) {
                TrackingFailureReason.NONE -> ArTrackingIssue.Initializing
                TrackingFailureReason.INSUFFICIENT_LIGHT -> ArTrackingIssue.InsufficientLight
                TrackingFailureReason.INSUFFICIENT_FEATURES -> ArTrackingIssue.InsufficientFeatures
                TrackingFailureReason.EXCESSIVE_MOTION -> ArTrackingIssue.ExcessiveMotion
                TrackingFailureReason.CAMERA_UNAVAILABLE -> ArTrackingIssue.CameraUnavailable
                TrackingFailureReason.BAD_STATE -> ArTrackingIssue.BadState
            }
            var planeDetected = lastStatus.planeDetected
            if (cameraOk) {
                camera.getViewMatrix(view, 0)
                camera.getProjectionMatrix(projection, 0, .1f, 100f)
                placeRequestedAnchor(frame)
                val now = System.currentTimeMillis()
                if (now - lastPlaneCheckAt >= 250L) {
                    lastPlaneCheckAt = now
                    val planes = session.getAllTrackables(Plane::class.java)
                    val horizontalTracked = planes.count {
                        it.type == Plane.Type.HORIZONTAL_UPWARD_FACING && it.trackingState == TrackingState.TRACKING
                    }
                    planeDetected = horizontalTracked > 0
                }
                anchor?.let { current ->
                    if (current.trackingState == TrackingState.TRACKING) drawArrow(current)
                }
            }
            val anchorOk = anchor?.trackingState == TrackingState.TRACKING
            val anchorProvisional = instantPlacementPoint?.trackingMethod ==
                InstantPlacementPoint.TrackingMethod.SCREENSPACE_WITH_APPROXIMATE_DISTANCE
            if (!cameraOk) planeDetected = false
            publish(ArTrackingStatus(cameraOk, anchorOk, planeDetected, anchorProvisional = anchorProvisional, placementMissed = placementMissed, trackingIssue = trackingIssue))
        } catch (error: Exception) {
            failed = error.message ?: "The AR camera session stopped unexpectedly."
            publish(ArTrackingStatus(error = failed))
        }
    }

    fun requestPlacement(x: Float, y: Float) { placementMissed = false; pendingHit = x to y }
    fun rotate(byDegrees: Float) { angle.set((angle.get() + byDegrees) % 360f) }
    fun clearAnchor() {
        pendingHit = null
        placementMissed = false
        // The view may be disposed while its queued GL event is draining and
        // the owning Session is already closed. Detaching is best-effort then.
        try { anchor?.detach() } catch (_: Exception) { }
        anchor = null
        instantPlacementPoint = null
        angle.set(0f)
    }
    fun release() { clearAnchor() }

    private fun placeRequestedAnchor(frame: Frame) {
        val point = pendingHit ?: return
        pendingHit = null
        if (frame.camera.trackingState != TrackingState.TRACKING) return
        val hits = frame.hitTest(point.first, point.second)
        val planeHit = hits.firstOrNull { result ->
            val trackable = result.trackable
            trackable is Plane && trackable.trackingState == TrackingState.TRACKING &&
                trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING && trackable.isPoseInPolygon(result.hitPose)
        }
        // When plane fitting has not converged yet, ARCore can place a provisional
        // +Y-up point from the tap and refine its depth as the user scans the floor.
        val hit = planeHit ?: runCatching {
            frame.hitTestInstantPlacement(point.first, point.second, INSTANT_PLACEMENT_DISTANCE)
                .firstOrNull { it.trackable is InstantPlacementPoint }
        }.getOrNull()
        if (hit == null) {
            placementMissed = true
            return
        }
        placementMissed = false
        anchor?.detach()
        anchor = hit.createAnchor()
        instantPlacementPoint = hit.trackable as? InstantPlacementPoint
    }

    private fun drawCamera(frame: Frame) {
        frame.transformCoordinates2d(Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, cameraQuad, Coordinates2d.TEXTURE_NORMALIZED, cameraUv)
        cameraQuad.position(0)
        cameraUv.position(0)
        GLES20.glDepthMask(false)
        GLES20.glUseProgram(cameraProgram)
        GLES20.glEnableVertexAttribArray(cameraPositionAttribute)
        GLES20.glVertexAttribPointer(cameraPositionAttribute, 2, GLES20.GL_FLOAT, false, 0, cameraQuad)
        GLES20.glEnableVertexAttribArray(cameraUvAttribute)
        GLES20.glVertexAttribPointer(cameraUvAttribute, 2, GLES20.GL_FLOAT, false, 0, cameraUv)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture)
        GLES20.glUniform1i(cameraSamplerUniform, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDepthMask(true)
    }

    private fun drawArrow(current: com.google.ar.core.Anchor) {
        current.pose.toMatrix(model, 0)
        Matrix.setRotateM(rotated, 0, angle.get(), 0f, 1f, 0f)
        Matrix.multiplyMM(worldModel, 0, model, 0, rotated, 0)
        Matrix.multiplyMM(viewModel, 0, view, 0, worldModel, 0)
        Matrix.multiplyMM(mvp, 0, projection, 0, viewModel, 0)
        GLES20.glUseProgram(arrowProgram)
        GLES20.glEnableVertexAttribArray(arrowPositionAttribute)
        arrow.position(0)
        GLES20.glVertexAttribPointer(arrowPositionAttribute, 3, GLES20.GL_FLOAT, false, 0, arrow)
        GLES20.glUniformMatrix4fv(arrowMvpUniform, 1, false, mvp, 0)
        GLES20.glUniform4f(arrowColorUniform, .98f, .31f, .12f, 1f)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 9)
    }

    private fun publish(value: ArTrackingStatus) {
        val now = System.currentTimeMillis()
        if (value != lastStatus && now - lastStatusAt >= 200L) {
            lastStatus = value
            lastStatusAt = now
            mainHandler.post { onStatus(value) }
        }
    }

    private fun floatBuffer(values: FloatArray): FloatBuffer = ByteBuffer.allocateDirect(values.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(values); position(0) }

    private fun program(vertex: String, fragment: String): Int {
        val vs = compile(GLES20.GL_VERTEX_SHADER, vertex)
        val fs = compile(GLES20.GL_FRAGMENT_SHADER, fragment)
        return GLES20.glCreateProgram().also { id ->
            GLES20.glAttachShader(id, vs); GLES20.glAttachShader(id, fs); GLES20.glLinkProgram(id)
            val result = IntArray(1); GLES20.glGetProgramiv(id, GLES20.GL_LINK_STATUS, result, 0)
            check(result[0] != 0) { GLES20.glGetProgramInfoLog(id) }
            GLES20.glDeleteShader(vs); GLES20.glDeleteShader(fs)
        }
    }

    private fun compile(type: Int, source: String): Int = GLES20.glCreateShader(type).also { shader ->
        GLES20.glShaderSource(shader, source); GLES20.glCompileShader(shader)
        val result = IntArray(1); GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, result, 0)
        check(result[0] != 0) { GLES20.glGetShaderInfoLog(shader) }
    }

    private companion object {
        const val INSTANT_PLACEMENT_DISTANCE = 1.5f
        const val CAMERA_VERTEX = "attribute vec4 a_Position; attribute vec2 a_TexCoord; varying vec2 v_TexCoord; void main(){gl_Position=a_Position;v_TexCoord=a_TexCoord;}"
        const val CAMERA_FRAGMENT = "#extension GL_OES_EGL_image_external : require\nprecision mediump float; uniform samplerExternalOES u_Texture; varying vec2 v_TexCoord; void main(){gl_FragColor=texture2D(u_Texture,v_TexCoord);}"
        const val ARROW_VERTEX = "attribute vec4 a_Position; uniform mat4 u_Mvp; void main(){gl_Position=u_Mvp*a_Position;}"
        const val ARROW_FRAGMENT = "precision mediump float; uniform vec4 u_Color; void main(){gl_FragColor=u_Color;}"
    }
}
