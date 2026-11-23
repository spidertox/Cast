package com.krcast.sender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { KrTheme { App() } }
    }

    override fun onResume() {
        super.onResume()
        ResumeTick.intValue++
    }
}

@Composable
private fun KrTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) {
        darkColorScheme(
            primary = KrBlue, onPrimary = Color.White, secondary = KrViolet,
            background = Color(0xFF0B0B14), surface = Color(0xFF16161F),
        )
    } else {
        lightColorScheme(
            primary = KrBlue, onPrimary = Color.White, secondary = KrViolet,
            background = Color(0xFFF4F5FA), surface = Color.White,
        )
    }
    MaterialTheme(colorScheme = scheme) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, content = content)
    }
}

@Composable
private fun App() {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            SplashScreen { nav.navigate("home") { popUpTo("splash") { inclusive = true } } }
        }
        composable("home") {
            HomeScreen(onCompat = { nav.navigate("compat") }, onDiag = { nav.navigate("diag") })
        }
        composable("compat") {
            InfoScreen("Compatibility Check", remember { DeviceInfo.compat(ctx) }, onBack = { nav.popBackStack() })
        }
        composable("diag") {
            InfoScreen("Diagnostics", remember { DeviceInfo.diag(ctx) }, onBack = { nav.popBackStack() }, copyable = true)
        }
    }
}
