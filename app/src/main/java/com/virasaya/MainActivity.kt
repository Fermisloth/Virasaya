package com.virasaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.virasaya.ui.VirasayaApp
import com.virasaya.ui.theme.VirasayaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(0xFF11150F.toInt()), navigationBarStyle = SystemBarStyle.dark(0xFF11150F.toInt()))
        setContent { VirasayaTheme { VirasayaApp() } }
    }
}
