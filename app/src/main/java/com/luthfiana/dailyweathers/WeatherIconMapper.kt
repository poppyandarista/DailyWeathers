package com.luthfiana.dailyweathers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import com.luthfiana.dailyweathers.R

object WeatherIconMapper {

    // Mapping dari kode icon OpenWeatherMap ke drawable lokal
    fun mapWeatherIcon(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.drawable.img_sun
            "01n" -> R.drawable.img_moon_stars
            "02d", "03d", "04d" -> R.drawable.img_cloudy
            "02n", "03n", "04n" -> R.drawable.img_clouds
            "09d", "09n", "10d", "10n" -> R.drawable.img_rain
            "11d", "11n" -> R.drawable.img_thunder
            "13d", "13n" -> R.drawable.img_snow
            "50d", "50n" -> R.drawable.img_fog
            else -> R.drawable.img_sun
        }
    }

    // Mapping untuk icon hujan kecil
    fun getRainIcon(): Int {
        return R.drawable.img_sub_rain
    }

    // Composable function untuk menggunakan icon lokal dengan warna asli
    @Composable
    fun LocalWeatherIcon(
        iconCode: String,
        contentDescription: String = "Weather icon",
        modifier: Modifier = Modifier,
        tint: Color? = null // Opsional, jika ingin memberikan tint
    ) {
        val iconRes = mapWeatherIcon(iconCode)

        if (tint != null) {
            // Jika ada tint, gunakan ColorFilter
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = modifier,
                colorFilter = ColorFilter.tint(tint)
            )
        } else {
            // Jika tidak ada tint, tampilkan warna asli
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
    }
}