package com.krcast.sender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color

private val KrColors = darkColorScheme(
    primary = Color(0xFF1F6FFF),
    secondary = Color(0xFF8E3DFF),
    background = Color(0xFF0B0B14),
    surface = Color(0xFF16161F),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = KrColors) { Home() } }
    }
}

@Composable
private fun Home() {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("KR Cast", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        Text("Your Screen. Your Big Display.", color = MaterialTheme.colorScheme.primary)
    }
}
