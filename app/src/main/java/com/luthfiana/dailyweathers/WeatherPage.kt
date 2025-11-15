package com.luthfiana.dailyweathers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.luthfiana.dailyweathers.api.ForecastResponse
import com.luthfiana.dailyweathers.api.NetworkResponse
import com.luthfiana.dailyweathers.api.WeatherModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.luthfiana.dailyweathers.api.WeatherInfoItem

@Composable
fun WeatherPage(viewModel: WeatherViewModel) {
    var city by remember { mutableStateOf("") }
    val weatherResult: NetworkResponse<WeatherModel>? by viewModel.weatherResult.collectAsStateWithLifecycle()
    val forecastResult: NetworkResponse<ForecastResponse>? by viewModel.forecastResult.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A6DFF),
                        Color(0xFF0D47A1)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Tambahkan scroll
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search Bar
            SearchBar(
                city = city,
                onCityChange = { city = it },
                onSearch = {
                    viewModel.getData(city)
                    keyboardController?.hide()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Weather Content
            when (val result = weatherResult) {
                is NetworkResponse.Error -> {
                    ErrorMessage(message = result.message ?: "Unknown error occurred")
                }
                is NetworkResponse.Loading -> {
                    LoadingIndicator()
                }
                is NetworkResponse.Success -> {
                    WeatherDetails(
                        data = result.data,
                        forecastData = forecastResult
                    )
                }
                null -> {
                    WelcomeMessage()
                }
            }
        }
    }
}

fun groupForecastByDay(forecast: ForecastResponse): List<DailyForecast> {
    val grouped = forecast.list.groupBy {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(parseDateTime(it.dtTxt))
    }

    return grouped.map { (date, items) ->

        val minTemp = items.minOf { it.main.temp }.toInt()
        val maxTemp = items.maxOf { it.main.temp }.toInt()

        val humidity = items.map { it.main.humidity }.average().toInt()

        val primaryItem = items.first()

        DailyForecast(
            date = date,
            icon = primaryItem.weather.firstOrNull()?.icon ?: "01d",
            minTemp = minTemp,
            maxTemp = maxTemp,
            humidity = humidity
        )
    }.take(7)
}

data class DailyForecast(
    val date: String,
    val icon: String,
    val minTemp: Int,
    val maxTemp: Int,
    val humidity: Int
)

@Composable
fun DailyForecastSection(forecastData: NetworkResponse<ForecastResponse>?) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-24).dp),
                colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                text = "Perkiraan Cuaca",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (forecastData) {
                is NetworkResponse.Success -> {
                    val daily = groupForecastByDay(forecastData.data)
                    DailyForecastList(daily)
                }

                else -> Text(
                    text = "Data tidak tersedia",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun DailyForecastList(list: List<DailyForecast>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        list.forEachIndexed { index, item ->
            DailyForecastRow(item, index)
        }
    }
}

@Composable
fun DailyForecastRow(item: DailyForecast, index: Int) {

    val dayName = when (index) {
        0 -> "Hari ini"
        1 -> "Besok"
        else -> SimpleDateFormat("EEE", Locale("id")).format(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(item.date)!!
        )
    }.replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // Hari
        Text(
            text = dayName,
            fontSize = 16.sp,
            color = Color.White
        )

        Row(verticalAlignment = Alignment.CenterVertically) {

            // Humidity
            Text(
                text = "${item.humidity}%",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Icon cuaca
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${item.icon}.png",
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Suhu Max - Min
            Text(
                text = "${item.maxTemp}° ${item.minTemp}°",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun WeatherDetails(data: WeatherModel, forecastData: NetworkResponse<ForecastResponse>?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location
        Text(
            text = data.cityName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = data.sys.country,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Temperature and Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${data.main.temp.toInt()}°",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            val weatherIcon = data.weather.firstOrNull()?.icon ?: "01d"
            AsyncImage(
                modifier = Modifier.size(80.dp),
                model = "https://openweathermap.org/img/wn/${weatherIcon}@2x.png",
                contentDescription = "Weather icon"
            )
        }

        // Weather Condition
        val weatherDescription = data.weather.firstOrNull()?.description ?: "Unknown"
        Text(
            text = weatherDescription.replaceFirstChar { it.uppercase() },
            fontSize = 20.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Feels Like
        Text(
            text = "Terasa seperti ${data.main.feelsLike.toInt()}°",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Weather Summary Card - YANG INI TIDAK BOLEH PAKAI PAGER
        WeatherSummaryCard(data = data, forecastData = forecastData)

        Spacer(modifier = Modifier.height(24.dp))

        // Hourly Forecast Section
        Text(
            text = "Prediksi Cuaca",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hourly Forecast Cards
        when (val forecast = forecastData) {
            is NetworkResponse.Success -> {
                HourlyForecast(forecastData = forecast.data)
            }
            is NetworkResponse.Loading -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            else -> {
                Text(
                    text = "Data prediksi tidak tersedia",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Precipitation Timeline Card
        PrecipitationTimelineCard(forecastData = forecastData)

        Spacer(modifier = Modifier.height(24.dp))

        // Weather Info Pager Card - HANYA SATU DI BAWAH
        WeatherInfoPagerCard(
            data = data,
            forecast = (forecastData as? NetworkResponse.Success)?.data
        )

        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.height(24.dp))

        DailyForecastSection(forecastData)

        Spacer(modifier = Modifier.height(24.dp))

    }
}

@Composable
fun WeatherInfoPagerCard(data: WeatherModel, forecast: ForecastResponse?) {

    val items = generateWeatherInfoItems(data, forecast)

    val pagerState = rememberPagerState(initialPage = 0) { items.size }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(26.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            // BISA GESER PER-HALAMAN
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->

                val item = items[page]

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // KIRI: ICON + TEKS
                    Column(modifier = Modifier.weight(1f)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(item.icon, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.description,
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 20.sp
                        )
                    }

                    // KANAN: ANGKA BESAR
                    Text(
                        text = item.value,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BULLET DINAMIS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(items.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (selected) Color.White
                                else Color.White.copy(alpha = 0.35f)
                            )
                    )
                }
            }
        }
    }
}

fun generateSmartWeatherTips(
    data: WeatherModel,
    forecastData: NetworkResponse<ForecastResponse>?
): List<WeatherTip> {

    val feels = data.main.feelsLike.toInt()
    val hum = data.main.humidity
    val wind = data.wind.speed
    val desc = data.weather.firstOrNull()?.description ?: ""

    val tips = mutableListOf<WeatherTip>()

    // 1. Humidity Heat Stress
    if (hum >= 70 && feels >= 32) {
        tips.add(
            WeatherTip(
                icon = "💧",
                iconColor = Color.Transparent,
                title = "Kelembapan Tinggi",
                description = "Kelembapan $hum% membuat suhu terasa lebih panas hingga $feels°C."
            )
        )
    }

    // 2. Hydration
    tips.add(
        WeatherTip(
            icon = "🥤",
            iconColor = Color.Transparent,
            title = "Tetap Terhidrasi",
            description = "Minumlah air minimal 2 liter hari ini untuk menjaga tubuh tetap segar."
        )
    )

    // 3. UV Protection
    if (desc.contains("clear", true)) {
        tips.add(
            WeatherTip(
                icon = "🌞",
                iconColor = Color.Transparent,
                title = "Sinar UV Tinggi",
                description = "Gunakan sunscreen & kacamata hitam bila keluar siang hari."
            )
        )
    }

    // 4. Rain probability
    if (desc.contains("rain", true)) {
        tips.add(
            WeatherTip(
                icon = "🌧️",
                iconColor = Color.Transparent,
                title = "Hujan Hari Ini",
                description = "Persiapkan payung atau jas hujan saat bepergian."
            )
        )
    }

    // 5. Wind
    if (wind > 6) {
        tips.add(
            WeatherTip(
                icon = "💨",
                iconColor = Color.Transparent,
                title = "Angin Kencang",
                description = "Kecepatan angin ${"%.1f".format(wind)} m/s, hati-hati berada di area terbuka."
            )
        )
    }

    return tips.take(6)
}

fun generateWeatherInfoItems(
    data: WeatherModel,
    forecast: ForecastResponse?
): List<WeatherInfoItem> {

    val feels = data.main.feelsLike.toInt()
    val hum = data.main.humidity
    val realTemp = data.main.temp.toInt()
    val wind = data.wind.speed
    val desc = data.weather.firstOrNull()?.description ?: ""

    return listOf(
        WeatherInfoItem(
            icon = "🌡️",
            title = "Suhu Besok Terasa Seperti",
            description = "Kelembapan membuat suhu tinggi terasa seperti $feels°C",
            value = "$feels°"
        ),
        WeatherInfoItem(
            icon = "💧",
            title = "Kelembapan Udara",
            description = "Kelembapan mencapai $hum%",
            value = "$hum%"
        ),
        WeatherInfoItem(
            icon = "💨",
            title = "Kecepatan Angin",
            description = "Angin bertiup ${"%.1f".format(wind)} m/s",
            value = "${"%.1f".format(wind)}"
        ),
        WeatherInfoItem(
            icon = "🌥️",
            title = "Kondisi Cuaca",
            description = desc.replaceFirstChar { it.uppercase() },
            value = "${realTemp}°"
        )
    )
}


@Composable
fun WeatherTipItem(tip: WeatherTip) {
    Row(
        modifier = Modifier
            .width(260.dp)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Icon kecil
        Text(
            text = tip.icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        Column {
            Text(
                text = tip.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = tip.description,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

data class WeatherTip(
    val icon: String,
    val iconColor: Color,
    val title: String,
    val description: String
)

@Composable
fun generateWeatherTipsWithIcons(data: WeatherModel, forecastData: NetworkResponse<ForecastResponse>?): List<WeatherTip> {
    val tips = mutableListOf<WeatherTip>()
    val feelsLike = data.main.feelsLike.toInt()
    val humidity = data.main.humidity
    val weatherDescription = data.weather.firstOrNull()?.description ?: ""
    val windSpeed = data.wind.speed

    // Tip 1: Berdasarkan suhu
    when {
        feelsLike >= 35 -> tips.add(WeatherTip(
            icon = "🌡️",
            iconColor = Color(0xFFFF6B6B),
            title = "Suhu Panas",
            description = "Terasa ${feelsLike}°C, hindari aktivitas di luar ruangan"
        ))
        feelsLike >= 30 -> tips.add(WeatherTip(
            icon = "☀️",
            iconColor = Color(0xFFFFA726),
            title = "Suhu Hangat",
            description = "Terasa ${feelsLike}°C, gunakan pakaian ringan"
        ))
        feelsLike <= 20 -> tips.add(WeatherTip(
            icon = "❄️",
            iconColor = Color(0xFF4FC3F7),
            title = "Suhu Dingin",
            description = "Terasa ${feelsLike}°C, gunakan jaket hangat"
        ))
        else -> tips.add(WeatherTip(
            icon = "🌤️",
            iconColor = Color(0xFF4DB6AC),
            title = "Suhu Normal",
            description = "Terasa ${feelsLike}°C, cuaca cukup nyaman"
        ))
    }

    // Tip 2: Berdasarkan kelembapan
    when {
        humidity >= 80 -> tips.add(WeatherTip(
            icon = "💧",
            iconColor = Color(0xFF29B6F6),
            title = "Lembap Tinggi",
            description = "Kelembapan ${humidity}%, banyak minum air"
        ))
        humidity <= 40 -> tips.add(WeatherTip(
            icon = "🏜️",
            iconColor = Color(0xFFFFB74D),
            title = "Udara Kering",
            description = "Kelembapan ${humidity}%, gunakan pelembap"
        ))
        else -> tips.add(WeatherTip(
            icon = "🌊",
            iconColor = Color(0xFF4DD0E1),
            title = "Kelembapan Normal",
            description = "Kelembapan ${humidity}%, cukup nyaman"
        ))
    }

    // Tip 3: Berdasarkan kondisi cuaca
    when {
        weatherDescription.contains("rain", ignoreCase = true) -> tips.add(WeatherTip(
            icon = "🌧️",
            iconColor = Color(0xFF7986CB),
            title = "Bawa Payung",
            description = "Hujan mungkin turun hari ini"
        ))
        weatherDescription.contains("thunderstorm", ignoreCase = true) -> tips.add(WeatherTip(
            icon = "⛈️",
            iconColor = Color(0xFF5C6BC0),
            title = "Waspada Badai",
            description = "Hindari area terbuka"
        ))
        weatherDescription.contains("clear", ignoreCase = true) -> tips.add(WeatherTip(
            icon = "🌞",
            iconColor = Color(0xFFFFCA28),
            title = "Gunakan Sunscreen",
            description = "Cuaca cerah, lindungi kulit"
        ))
        weatherDescription.contains("cloud", ignoreCase = true) -> tips.add(WeatherTip(
            icon = "☁️",
            iconColor = Color(0xFF90A4AE),
            title = "Berawan",
            description = "Tetap bawa payung jaga-jaga"
        ))
    }

    // Tip 4: Berdasarkan angin
    if (windSpeed > 5) {
        tips.add(WeatherTip(
            icon = "💨",
            iconColor = Color(0xFF80CBC4),
            title = "Angin Kencang",
            description = "${String.format("%.1f", windSpeed)} m/s, hati-hati"
        ))
    } else {
        tips.add(WeatherTip(
            icon = "🍃",
            iconColor = Color(0xFF81C784),
            title = "Angin Lembut",
            description = "Angin tenang, cuaca menyenangkan"
        ))
    }

    // Tip 5: Hidrasi (selalu ada)
    tips.add(WeatherTip(
        icon = "💧",
        iconColor = Color(0xFF4FC3F7),
        title = "Tetap Terhidrasi",
        description = "Minum 2L air hari ini"
    ))

    // Tip 6: UV Protection (jika cerah)
    if (weatherDescription.contains("clear", ignoreCase = true)) {
        tips.add(WeatherTip(
            icon = "🕶️",
            iconColor = Color(0xFFFF9800),
            title = "Proteksi UV",
            description = "Gunakan kacamata hitam"
        ))
    }

    return tips.take(6) // Maksimal 6 tips untuk scroll horizontal
}
@Composable
fun generateWeatherTips(data: WeatherModel, forecastData: NetworkResponse<ForecastResponse>?): List<String> {
    val tips = mutableListOf<String>()
    val feelsLike = data.main.feelsLike.toInt()
    val humidity = data.main.humidity
    val weatherDescription = data.weather.firstOrNull()?.description ?: ""

    // Tips berdasarkan feels like temperature
    when {
        feelsLike >= 35 -> tips.add("Suhu terasa sangat panas seperti ${feelsLike}°C, hindari aktivitas di luar ruangan pada siang hari")
        feelsLike >= 30 -> tips.add("Suhu terasa panas seperti ${feelsLike}°C, gunakan pakaian yang ringan dan nyaman")
        feelsLike <= 20 -> tips.add("Suhu terasa sejuk seperti ${feelsLike}°C, gunakan jaket atau pakaian hangat")
    }

    // Tips berdasarkan kelembapan
    when {
        humidity >= 80 -> tips.add("Kelembapan tinggi ${humidity}% membuat suhu terasa lebih panas, banyak minum air putih")
        humidity <= 40 -> tips.add("Kelembapan rendah ${humidity}%, kulit mungkin terasa kering, gunakan pelembap")
    }

    // Tips berdasarkan kondisi cuaca
    when {
        weatherDescription.contains("rain", ignoreCase = true) -> {
            tips.add("Bawalah payung atau jas hujan, hujan mungkin terjadi hari ini")

            // Versi sederhana tanpa prediksi waktu
            tips.add("Hujan diperkirakan turun sore hari, <2mm")
        }
        weatherDescription.contains("thunderstorm", ignoreCase = true) -> {
            tips.add("Waspada badai petir, hindari area terbuka dan benda logam")
        }
        weatherDescription.contains("clear", ignoreCase = true) -> {
            tips.add("Cuaca cerah, gunakan sunscreen untuk melindungi kulit")
        }
        weatherDescription.contains("cloud", ignoreCase = true) -> {
            tips.add("Hari ini berawan, tetap bawa payung untuk berjaga-jaga")
        }
    }

    // Tips berdasarkan angin
    val windSpeed = data.wind.speed
    if (windSpeed > 5) {
        tips.add("Angin cukup kencang ${String.format("%.1f", windSpeed)} m/s, hati-hati dengan benda terbang")
    }

    // Tips tambahan berdasarkan waktu
    tips.add("Minum setidaknya 2 liter air hari ini untuk menjaga hidrasi")

    return tips.take(4) // Batasi maksimal 4 tips
}
@Composable
fun getNextRainTime(forecastData: ForecastResponse): String? {
    val currentTime = System.currentTimeMillis()
    val upcomingForecasts = forecastData.list.filter { forecastItem ->
        val forecastTime = parseDateTime(forecastItem.dtTxt).time
        forecastTime >= currentTime && (forecastItem.pop ?: 0.0) > 0.3
    }.take(1)

    return upcomingForecasts.firstOrNull()?.let { forecast ->
        formatTimeShort(forecast.dtTxt)
    }
}

@Composable
fun WeatherSummaryCard(data: WeatherModel, forecastData: NetworkResponse<ForecastResponse>?) {
    // Calculate min and max temperature from forecast data
    val (minTemp, maxTemp) = when (forecastData) {
        is NetworkResponse.Success -> {
            val temps = forecastData.data.list.map { it.main.temp }
            val min = temps.minOrNull()?.toInt() ?: data.main.temp.toInt() - 2
            val max = temps.maxOrNull()?.toInt() ?: data.main.temp.toInt() + 2
            min to max
        }
        else -> {
            (data.main.temp.toInt() - 2) to (data.main.temp.toInt() + 2)
        }
    }

    val weatherDescription = data.weather.firstOrNull()?.description ?: "Unknown"
    val weatherSummary = when {
        weatherDescription.contains("thunderstorm", ignoreCase = true) -> "Badai petir"
        weatherDescription.contains("rain", ignoreCase = true) -> "Hujan"
        weatherDescription.contains("drizzle", ignoreCase = true) -> "Gerimis"
        weatherDescription.contains("snow", ignoreCase = true) -> "Salju"
        weatherDescription.contains("clear", ignoreCase = true) -> "Cerah"
        weatherDescription.contains("cloud", ignoreCase = true) -> "Berawan"
        else -> weatherDescription.replaceFirstChar { it.uppercase() }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Dynamic Weather Summary - HANYA TEKS SUMMARY BIASA
            Text(
                text = "$weatherSummary. Tinggi $maxTemp°C & rendah $minTemp°C",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
        }
    }
}
@Composable
fun PrecipitationTimelineCard(forecastData: NetworkResponse<ForecastResponse>?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Precipitation Timeline Title
            Text(
                text = "Kemungkinan Hujan",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Precipitation Timeline
            when (forecastData) {
                is NetworkResponse.Success -> {
                    RealPrecipitationTimeline(forecastData.data)
                }
                else -> {
                    PrecipitationTimelinePlaceholder()
                }
            }
        }
    }
}

@Composable
fun HourlyForecast(forecastData: ForecastResponse) {
    val currentTime = System.currentTimeMillis()

    // Filter untuk mendapatkan forecast mulai dari waktu sekarang
    val upcomingForecasts = forecastData.list.filter { forecastItem ->
        val forecastTime = parseDateTime(forecastItem.dtTxt).time
        forecastTime >= currentTime
    }.take(6) // Ambil 6 data berikutnya

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(upcomingForecasts) { forecastItem ->
            HourlyForecastItem(forecastItem = forecastItem)
        }
    }
}

@Composable
fun HourlyForecastItem(forecastItem: com.luthfiana.dailyweathers.api.ForecastItem) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Time
            Text(
                text = formatTime(forecastItem.dtTxt),
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            // Weather Icon
            val weatherIcon = forecastItem.weather.firstOrNull()?.icon ?: "01d"
            AsyncImage(
                modifier = Modifier.size(40.dp),
                model = "https://openweathermap.org/img/wn/${weatherIcon}.png",
                contentDescription = "Weather icon"
            )

            // Temperature
            Text(
                text = "${forecastItem.main.temp.toInt()}°",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // Precipitation Probability
            val pop = (forecastItem.pop ?: 0.0) * 100
            Text(
                text = "${pop.toInt()}%",
                fontSize = 12.sp,
                color = Color(0xFF64B5F6),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RealPrecipitationTimeline(forecastData: ForecastResponse) {
    val currentTime = System.currentTimeMillis()

    // Get next 6 forecast items
    val nextForecasts = forecastData.list.filter { forecastItem ->
        parseDateTime(forecastItem.dtTxt).time >= currentTime
    }.take(6)

    if (nextForecasts.isNotEmpty()) {
        Column {
            // Timeline labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                nextForecasts.forEach { forecast ->
                    Text(
                        text = formatTimeShort(forecast.dtTxt),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real precipitation probability bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                nextForecasts.forEach { forecast ->
                    val pop = (forecast.pop ?: 0.0) * 100
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Bar height based on actual precipitation probability
                        val barHeight = (pop / 100f) * 40f
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(maxOf(barHeight.dp, 4.dp))
                                .background(
                                    color = Color(0xFF64B5F6),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${pop.toInt()}%",
                            fontSize = 12.sp,
                            color = Color(0xFF64B5F6),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    } else {
        PrecipitationTimelinePlaceholder()
    }
}

@Composable
fun PrecipitationTimelinePlaceholder() {
    Column {
        // Timeline labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("21:00", "00:00", "03:00", "06:00", "09:00", "12:00").forEach { time ->
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Precipitation bars (placeholder)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            listOf(75, 35, 0, 50, 100, 100).forEach { percentage ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val barHeight = (percentage / 100f) * 40f
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(maxOf(barHeight.dp, 4.dp))
                            .background(
                                color = Color(0xFF64B5F6),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$percentage%",
                        fontSize = 12.sp,
                        color = Color(0xFF64B5F6),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Helper function to parse date time
private fun parseDateTime(dateTime: String): Date {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        inputFormat.parse(dateTime) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}

// Helper function to format time (show only hour)
private fun formatTime(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateTime.substring(11, 16) // Fallback: take HH:mm from string
    }
}

// Helper function to format short time
private fun formatTimeShort(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        val timeStr = outputFormat.format(date ?: Date())

        // If current time, show "Now"
        val currentTime = System.currentTimeMillis()
        val forecastTime = parseDateTime(dateTime).time
        if (forecastTime - currentTime < 3600000) { // Within 1 hour
            "Sekarang"
        } else {
            timeStr
        }
    } catch (e: Exception) {
        dateTime.substring(11, 16)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    city: String,
    onCityChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = city,
            onValueChange = onCityChange,
            label = {
                Text(
                    text = "Search for any location",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSearch,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading weather data...",
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ErrorMessage(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "😕",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun WelcomeMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌤️",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Daily Weather",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cari kota untuk melihat informasi cuaca",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}