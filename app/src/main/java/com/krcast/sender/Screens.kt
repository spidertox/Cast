package com.krcast.sender

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val KrBlue = Color(0xFF1F6FFF)
val KrViolet = Color(0xFF8E3DFF)

/** Bumped from MainActivity.onResume so Home re-reads Wi-Fi state after returning from settings. */
val ResumeTick = mutableIntStateOf(0)

@Composable
fun Logo(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.24f))
            .background(Brush.linearGradient(listOf(KrBlue, KrViolet))),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "KR Cast logo",
            modifier = Modifier.requiredSize(size * 1.5f),
        )
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        t.animateTo(1f, tween(1200))
        onDone()
    }
    Column(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = (t.value * 2f).coerceAtMost(1f)
                scaleX = 0.9f + 0.1f * t.value
                scaleY = 0.9f + 0.1f * t.value
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Logo(96.dp)
        Text(
            "KR Cast",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text("Your Screen. Your Big Display.", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun HomeScreen(onCompat: () -> Unit, onDiag: () -> Unit) {
    val ctx = LocalContext.current
    val tick = ResumeTick.intValue
    val rows = remember(tick) { DeviceInfo.status(ctx) }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Logo(56.dp)
            Column(Modifier.padding(start = 14.dp)) {
                Text("KR Cast", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Your Screen. Your Big Display.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        InfoCard(rows)

        Button(
            onClick = {
                runCatching { ctx.startActivity(Intent(Settings.ACTION_CAST_SETTINGS)) }.onFailure {
                    Toast.makeText(
                        ctx, "Could not open system screen mirroring. Try Smart View from the Quick Panel.",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) { Text("CONNECT TO TV", fontWeight = FontWeight.Bold) }
        Text(
            "Opens your phone's system screen mirroring. KR Cast's own Miracast sender is not built yet.",
            style = MaterialTheme.typography.bodySmall, color = muted,
        )

        OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("Find Devices") }
        Text(
            "Disabled: Wi-Fi Direct discovery is the next component.",
            style = MaterialTheme.typography.bodySmall, color = muted,
        )

        OutlinedButton(onClick = onCompat, modifier = Modifier.fillMaxWidth()) { Text("Compatibility Check") }
        OutlinedButton(onClick = onDiag, modifier = Modifier.fillMaxWidth()) { Text("Diagnostics") }
    }
}

@Composable
fun InfoScreen(title: String, rows: List<Pair<String, String>>, onBack: () -> Unit, copyable: Boolean = false) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Back") }
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            InfoCard(rows)
            if (copyable) {
                Button(
                    onClick = {
                        val text = "KR Cast diagnostics\n" + rows.joinToString("\n") { "${it.first}: ${it.second}" }
                        (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("KR Cast diagnostics", text))
                        Toast.makeText(ctx, "Diagnostic report copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) { Text("Copy Diagnostic Report") }
            }
        }
    }
}

@Composable
private fun InfoCard(rows: List<Pair<String, String>>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            rows.forEach { (label, value) ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.42f))
                    Text(value, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.weight(0.58f))
                }
            }
        }
    }
}
