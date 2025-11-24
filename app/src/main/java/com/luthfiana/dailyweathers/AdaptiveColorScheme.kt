package com.luthfiana.dailyweathers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

// Data class untuk tema adaptif
data class AdaptiveTheme(
    val primaryGradient: List<Color>,
    val secondaryGradient: List<Color>,
    val cardColor: Color,
    val textColor: Color,
    val secondaryTextColor: Color
)

// Manager untuk tema adaptif
object AdaptiveColorManager {
    private var currentTheme: AdaptiveTheme = getThemeForTime()

    fun updateTheme() {
        currentTheme = getThemeForTime()
    }

    fun getCurrentTheme(): AdaptiveTheme {
        return currentTheme
    }

    private fun getThemeForTime(): AdaptiveTheme {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when {
            hour in 5..9 -> morningTheme()      // Pagi: 05:00 - 10:00
            hour in 10..16 -> daytimeTheme()    // Siang: 10:00 - 17:00
            else -> eveningTheme()              // Sore/Malam: 17:00 - 05:00
        }
    }

    // Tema Pagi - Gradasi Hangat
    private fun morningTheme(): AdaptiveTheme {
        return AdaptiveTheme(
            primaryGradient = listOf(
                Color(0xFF87CEEB), // Biru Muda
                Color(0xFFFFF8E1)  // Kuning Pucat
            ),
            secondaryGradient = listOf(
                Color(0xFF64B5F6),
                Color(0xFFBBDEFB)
            ),
            cardColor = Color(0x99FFFFFF), // Putih transparan
            textColor = Color(0xFF1A237E), // Biru tua untuk kontras
            secondaryTextColor = Color(0xFF3949AB)
        )
    }

    // Tema Siang - Gradasi Cerah
    private fun daytimeTheme(): AdaptiveTheme {
        return AdaptiveTheme(
            primaryGradient = listOf(
                Color(0xFF4FC3F7), // Biru Cerah
                Color(0xFFFAFAFA)  // Putih/Abu-abu Sangat Terang
            ),
            secondaryGradient = listOf(
                Color(0xFF29B6F6),
                Color(0xFFE3F2FD)
            ),
            cardColor = Color(0xCCFFFFFF), // Putih lebih solid
            textColor = Color(0xFF0D47A1), // Biru sangat tua
            secondaryTextColor = Color(0xFF1976D2)
        )
    }

    // Tema Sore/Malam - Gradasi Dingin
    private fun eveningTheme(): AdaptiveTheme {
        return AdaptiveTheme(
            primaryGradient = listOf(
                Color(0xFF1976D2), // Biru Sedang
                Color(0xFF0D47A1)  // Biru Tua
            ),
            secondaryGradient = listOf(
                Color(0xFF1565C0),
                Color(0xFF0D47A1)
            ),
            cardColor = Color(0x4DFFFFFF), // Putih sangat transparan
            textColor = Color(0xFFE3F2FD), // Putih kebiruan
            secondaryTextColor = Color(0xFFBBDEFB)
        )
    }

    // Tema Malam (Dark Mode) - Untuk aksesibilitas
    fun nightTheme(): AdaptiveTheme {
        return AdaptiveTheme(
            primaryGradient = listOf(
                Color(0xFF0D1B2A), // Biru Sangat Tua
                Color(0xFF1B263B)  // Biru Tua Kehitaman
            ),
            secondaryGradient = listOf(
                Color(0xFF1B263B),
                Color(0xFF415A77)
            ),
            cardColor = Color(0x4DFFFFFF), // Putih sangat transparan
            textColor = Color(0xFFE0E1DD), // Putih keabuan
            secondaryTextColor = Color(0xFFBBDEFB)
        )
    }

    // Tema untuk mode aksesibilitas malam
    fun accessibilityNightTheme(): AdaptiveTheme {
        return AdaptiveTheme(
            primaryGradient = listOf(
                Color(0xFF000000), // Hitam solid
                Color(0xFF1A1A1A)  // Hitam keabuan
            ),
            secondaryGradient = listOf(
                Color(0xFF1A1A1A),
                Color(0xFF2D2D2D)
            ),
            cardColor = Color(0x4DFFFFFF),
            textColor = Color(0xFFFFFFFF),
            secondaryTextColor = Color(0xFFCCCCCC)
        )
    }
}

// Composable untuk mendapatkan tema yang sesuai
@Composable
fun rememberAdaptiveTheme(): AdaptiveTheme {
    val context = LocalContext.current
    var currentTheme by remember { mutableStateOf(AdaptiveColorManager.getCurrentTheme()) }

    // Deteksi mode sistem (sederhana - bisa dikembangkan lebih lanjut)
    val isSystemInDarkMode = isSystemInDarkMode(context)
    val isNightModeEnabled = isNightModeEnabled(context)

    return when {
        isNightModeEnabled -> AdaptiveColorManager.accessibilityNightTheme()
        isSystemInDarkMode -> AdaptiveColorManager.nightTheme()
        else -> currentTheme
    }
}

// Fungsi helper untuk mendeteksi dark mode sistem
private fun isSystemInDarkMode(context: android.content.Context): Boolean {
    val nightModeFlags = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
}

// Fungsi helper untuk mendeteksi night mode/aksesibilitas
private fun isNightModeEnabled(context: android.content.Context): Boolean {
    // Ini adalah implementasi sederhana, bisa disesuaikan dengan kebutuhan
    return try {
        val secureSettings = context.contentResolver
        android.provider.Settings.Secure.getInt(secureSettings, "night_display_activated", 0) == 1
    } catch (e: Exception) {
        false
    }
}