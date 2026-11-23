package com.krcast.sender

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.view.Display
import kotlin.math.roundToInt

/** Reads what the phone really reports. Nothing here is simulated. No SSID, IP or other private data is read. */
object DeviceInfo {
    private const val AVC = "video/avc"

    private val avc: List<MediaCodecInfo> by lazy {
        MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.filter { it.isEncoder && AVC in it.supportedTypes }
    }

    private fun isHw(i: MediaCodecInfo) = !i.name.startsWith("c2.android.") && !i.name.startsWith("OMX.google.")

    private fun maxFps1080(i: MediaCodecInfo): Int? = runCatching {
        i.getCapabilitiesForType(AVC).videoCapabilities.getSupportedFrameRatesFor(1920, 1080).upper.toInt()
    }.getOrNull()

    private fun wifiOn(ctx: Context) =
        (ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled

    private fun p2p(ctx: Context) = ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)

    private fun network(ctx: Context): String {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return "No active network"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile data"
            else -> "Other"
        }
    }

    private fun screen(ctx: Context): String {
        val m = ctx.resources.displayMetrics
        val hz = (ctx.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager)
            .getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate
        return "${m.widthPixels} × ${m.heightPixels}" + (hz?.let { " @ ${it.roundToInt()} Hz" } ?: "")
    }

    /** Home dashboard rows. */
    fun status(ctx: Context): List<Pair<String, String>> = listOf(
        "Connection" to "Not connected",
        "Wi-Fi" to (if (wifiOn(ctx)) "On" else "Off"),
        "Wi-Fi Direct" to (if (p2p(ctx)) "Hardware present" else "Not supported"),
        "H.264 encoder" to when {
            avc.any { isHw(it) } -> "Hardware"
            avc.isNotEmpty() -> "Software only"
            else -> "Not found"
        },
        "Miracast sender" to "Not verified",
        "Last connected TV" to "None",
        "Screen" to screen(ctx),
    )

    /** Compatibility Check rows, classified as Confirmed / Public API / Requires system privileges / Not yet tested. */
    fun compat(ctx: Context): List<Pair<String, String>> {
        val hw = avc.filter { isHw(it) }
        return listOf(
            "Wi-Fi Direct hardware" to (if (p2p(ctx)) "Confirmed · hardware feature present" else "Not supported"),
            "Wi-Fi P2P API" to "Public Android API · not implemented in KR Cast yet",
            "Screen capture" to "Public API (MediaProjection, needs your consent) · not implemented yet",
            "H.264 hardware encoder" to
                (if (hw.isEmpty()) "Not found" else "Confirmed · " + hw.joinToString { it.name }),
            "1080p encode rate" to
                (hw.firstNotNullOfOrNull { maxFps1080(it) }?.let { "Encoder reports up to $it fps (not measured)" }
                    ?: "Not reported"),
            "Audio capture" to "Public API with limits · not yet tested",
            "Miracast sender (Wi-Fi Display)" to "Requires system privileges · no public API for third-party apps",
            "Samsung TV UA43UE84AHULXL" to "Not yet tested",
            "System screen mirroring" to "Available via system settings · not yet tested on this TV",
        )
    }

    /** Diagnostics rows (also used for the copied report). */
    fun diag(ctx: Context): List<Pair<String, String>> = listOf(
        "Phone" to "${Build.MANUFACTURER} ${Build.MODEL}",
        "Android" to "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        "Build" to Build.DISPLAY,
        "Wi-Fi" to (if (wifiOn(ctx)) "On" else "Off"),
        "Active network" to network(ctx),
        "Wi-Fi Direct" to (if (p2p(ctx)) "Hardware feature present" else "Not supported"),
        "Miracast capability" to "Not verifiable by apps (no public API)",
        "MediaProjection" to "Not requested yet (capture not implemented)",
        "H.264 encoders" to
            (if (avc.isEmpty()) "None" else avc.joinToString { it.name + (if (isHw(it)) " (HW)" else " (SW)") }),
        "Screen" to screen(ctx),
        "Connection errors" to "None recorded",
    )
}
