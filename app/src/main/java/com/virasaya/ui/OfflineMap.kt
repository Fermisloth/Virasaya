package com.virasaya.ui

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream

private const val MAP_ORIGIN = "https://appassets.androidplatform.net"

@SuppressLint("SetJavaScriptEnabled")
private class MapWebView(context: Context) : WebView(context) {
    private var ready = false
    private var currentState = "{}"
    init {
        setBackgroundColor(android.graphics.Color.rgb(232,233,223))
        settings.javaScriptEnabled = true
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.blockNetworkLoads = true
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                val uri = request.url
                val name = uri.lastPathSegment.orEmpty()
                val allowed = setOf("index.html", "map.js", "map.css", "ol.js", "ol.css", "site-data.js")
                if (uri.scheme == "https" && uri.host == "appassets.androidplatform.net" && uri.path == "/map/$name" && name in allowed) {
                    val mime = when (name.substringAfterLast('.')) { "html" -> "text/html"; "css" -> "text/css"; else -> "application/javascript" }
                    return WebResourceResponse(mime, "UTF-8", context.assets.open("map/$name"))
                }
                return WebResourceResponse("text/plain", "UTF-8", 403, "Forbidden", emptyMap(), ByteArrayInputStream(byteArrayOf()))
            }
            override fun onPageFinished(view: WebView, url: String) {
                ready = true
                evaluateJavascript("window.setMapState($currentState)", null)
            }
        }
        loadUrl("$MAP_ORIGIN/map/index.html")
    }
    fun updateState(json: String) {
        if (currentState == json) return
        currentState = json
        if (ready) evaluateJavascript("window.setMapState($json)", null)
    }
}

@Composable
fun OfflineMap(state: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { MapWebView(it) }, modifier = modifier.clipToBounds(), onReset = null,
        onRelease = { it.stopLoading(); it.destroy() },
        update = { it.updateState(state) }
    )
}
