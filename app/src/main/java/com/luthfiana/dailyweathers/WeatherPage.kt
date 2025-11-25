package com.luthfiana.dailyweathers

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luthfiana.dailyweathers.api.ForecastResponse
import com.luthfiana.dailyweathers.api.NetworkResponse
import com.luthfiana.dailyweathers.api.WeatherInfoItem
import com.luthfiana.dailyweathers.api.WeatherModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.LocalConfiguration
import com.luthfiana.dailyweathers.api.ForecastItem
import kotlinx.coroutines.delay

data class ActivityCondition(
    val activity: String,
    val iconRes: Int,
    val status: String,
    val statusColor: Color,
    val description: String,
    val timeSlots: List<TimeSlot>
)

data class TimeSlot(
    val time: String,
    val status: String,
    val statusColor: Color
)

data class AirQualityData(
    val index: Int,
    val status: String,
    val statusColor: Color,
    val description: String
)

data class WeatherTip(
    val icon: String,
    val iconColor: Color,
    val title: String,
    val description: String
)

data class DailyForecast(
    val date: String,
    val icon: String,
    val minTemp: Int,
    val maxTemp: Int,
    val humidity: Int
)

data class UVIndexData(
    val index: Double,
    val status: String,
    val statusColor: Color,
    val description: String,
    val trend: String
)

data class HumidityData(
    val percentage: Int,
    val status: String,
    val statusColor: Color,
    val description: String,
    val trend: String
)

data class SunTimesData(
    val sunrise: String,
    val sunset: String
)

data class MoonData(
    val moonrise: String,
    val moonset: String,
    val moonPhase: String
)

@Composable
fun WeatherPage(viewModel: WeatherViewModel) {
    var city by remember { mutableStateOf("") }
    var showSearchPopup by remember { mutableStateOf(false) }
    val weatherResult: NetworkResponse<WeatherModel>? by viewModel.weatherResult.collectAsStateWithLifecycle()
    val forecastResult: NetworkResponse<ForecastResponse>? by viewModel.forecastResult.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val adaptiveTheme = rememberAdaptiveTheme()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        if (weatherResult == null) {
            viewModel.requestCurrentLocation(context)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60 * 60 * 1000)
            AdaptiveColorManager.updateTheme()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = adaptiveTheme.primaryGradient
                )
            )
    ) {
        if (isLandscape) {
            // LAYOUT LANDSCAPE - Dibagi 2 bagian
            LandscapeLayout(
                weatherResult = weatherResult,
                forecastResult = forecastResult,
                viewModel = viewModel,
                adaptiveTheme = adaptiveTheme,
                context = context,
                onRefreshClick = {
                    viewModel.requestCurrentLocation(context)
                    AdaptiveColorManager.updateTheme()
                },
                onSearchClick = { showSearchPopup = true }
            )
        } else {
            // LAYOUT PORTRAIT - Tetap seperti semula
            PortraitLayout(
                weatherResult = weatherResult,
                forecastResult = forecastResult,
                viewModel = viewModel,
                adaptiveTheme = adaptiveTheme,
                context = context,
                onRefreshClick = {
                    viewModel.requestCurrentLocation(context)
                    AdaptiveColorManager.updateTheme()
                },
                onSearchClick = { showSearchPopup = true }
            )
        }

        if (showSearchPopup) {
            SearchPopup(
                city = city,
                onCityChange = { city = it },
                onSearch = {
                    if (city.isNotBlank()) {
                        viewModel.getData(city)
                        keyboardController?.hide()
                        showSearchPopup = false
                    }
                },
                onDismiss = {
                    keyboardController?.hide()
                    showSearchPopup = false
                },
                adaptiveTheme = adaptiveTheme,
                context = context
            )
        }
    }
}

@Composable
fun StickyHeader(
    weatherResult: NetworkResponse<WeatherModel>?,
    onRefreshClick: () -> Unit,
    onSearchClick: () -> Unit,
    adaptiveTheme: AdaptiveTheme,
    context: Context,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (val result = weatherResult) {
                is NetworkResponse.Success -> {
                    LocationAndDateInfo(
                        cityName = result.data.cityName,
                        countryCode = result.data.sys.country,
                        adaptiveTheme = adaptiveTheme,
                        modifier = Modifier.weight(1f),
                        context = context
                    )
                }
                else -> {
                    DefaultLocationAndDateInfo(
                        adaptiveTheme = adaptiveTheme,
                        modifier = Modifier.weight(1f),
                        context = context
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = adaptiveTheme.secondaryGradient
                            )
                        )
                        .clickable(
                            onClick = onRefreshClick,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                        .border(
                            width = 1.dp,
                            color = adaptiveTheme.textColor.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = context.getString(R.string.refresh),
                        tint = adaptiveTheme.textColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = adaptiveTheme.secondaryGradient
                            )
                        )
                        .clickable(
                            onClick = onSearchClick,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                        .border(
                            width = 1.dp,
                            color = adaptiveTheme.textColor.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = context.getString(R.string.search),
                        tint = adaptiveTheme.textColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LeftLandscapeContent(
    modifier: Modifier = Modifier,
    weatherResult: NetworkResponse<WeatherModel>?,
    forecastResult: NetworkResponse<ForecastResponse>?,
    viewModel: WeatherViewModel,
    adaptiveTheme: AdaptiveTheme,
    context: Context,
    onRefreshClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        StickyHeader(
            weatherResult = weatherResult,
            onRefreshClick = onRefreshClick,
            onSearchClick = onSearchClick,
            adaptiveTheme = adaptiveTheme,
            context = context
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val result = weatherResult) {
            is NetworkResponse.Error -> {
                ErrorMessage(
                    message = result.message ?: context.getString(R.string.unknown_error),
                    adaptiveTheme = adaptiveTheme
                )
            }
            is NetworkResponse.Loading -> {
                LoadingIndicator(adaptiveTheme = adaptiveTheme, context = context)
            }
            is NetworkResponse.Success -> {
                LandscapeWeatherContent(
                    data = result.data,
                    forecastData = forecastResult,
                    adaptiveTheme = adaptiveTheme,
                    context = context
                )
            }
            null -> {
                WelcomeMessage(adaptiveTheme = adaptiveTheme, context = context)
            }
        }
    }
}

@Composable
fun LandscapeWeatherContent(
    data: WeatherModel,
    forecastData: NetworkResponse<ForecastResponse>?,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        // Main Weather Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Temperature dan icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    val currentWeatherIcon = data.weather.firstOrNull()?.icon ?: "01d"
                    WeatherIconMapper.LocalWeatherIcon(
                        iconCode = currentWeatherIcon,
                        contentDescription = "Current weather",
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${data.main.temp.toInt()}°",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = adaptiveTheme.textColor
                        )

                        val weatherDescription = data.weather.firstOrNull()?.description ?: "Unknown"
                        Text(
                            text = translateWeatherDescription(weatherDescription, context),
                            fontSize = 16.sp,
                            color = adaptiveTheme.secondaryTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                HighLowTempSeparateBoxes(
                    highTemp = getMaxTemp(data, forecastData),
                    lowTemp = getMinTemp(data, forecastData),
                    adaptiveTheme = adaptiveTheme,
                    context = context
                )

                Spacer(modifier = Modifier.height(20.dp))

                WeatherInfoCard(data, adaptiveTheme, context)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // UV Index dan Humidity Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // UV Index Card
            UVIndexCard(
                modifier = Modifier.weight(1f),
                data = data,
                adaptiveTheme = adaptiveTheme,
                context = context
            )

            // Humidity Card (YANG DIPERBAIKI)
            HumidityCardImproved(
                modifier = Modifier.weight(1f),
                data = data,
                adaptiveTheme = adaptiveTheme,
                context = context
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hourly Forecast Section
        Text(
            text = context.getString(R.string.weather_forecast),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = adaptiveTheme.textColor,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hourly Forecast Cards
        when (val forecast = forecastData) {
            is NetworkResponse.Success -> {
                HourlyForecast(forecastData = forecast.data, adaptiveTheme = adaptiveTheme, context = context)
            }
            is NetworkResponse.Loading -> {
                CircularProgressIndicator(
                    color = adaptiveTheme.textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            else -> {
                Text(
                    text = context.getString(R.string.forecast_data_unavailable),
                    color = adaptiveTheme.secondaryTextColor,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RightLandscapeContent(
    modifier: Modifier = Modifier,
    weatherResult: NetworkResponse<WeatherModel>?,
    forecastResult: NetworkResponse<ForecastResponse>?,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        when (val result = weatherResult) {
            is NetworkResponse.Success -> {
                LandscapeRightContent(
                    data = result.data,
                    forecastData = forecastResult,
                    adaptiveTheme = adaptiveTheme,
                    context = context
                )
            }
            else -> {
                // Placeholder untuk bagian kanan saat loading/error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(adaptiveTheme.cardColor.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = context.getString(R.string.loading_weather_data),
                        color = adaptiveTheme.secondaryTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun LandscapeRightContent(
    data: WeatherModel,
    forecastData: NetworkResponse<ForecastResponse>?,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .padding(end = 16.dp)
            // Tambahkan extra bottom padding untuk menghindari navigation bar
            .padding(top = 26.dp), // 56.dp adalah tinggi typical navigation bar
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Precipitation Timeline
        PrecipitationTimelineCard(forecastData = forecastData, adaptiveTheme = adaptiveTheme, context = context)

        // Weather Info Pager
        WeatherInfoPagerCard(
            data = data,
            forecast = (forecastData as? NetworkResponse.Success)?.data,
            adaptiveTheme = adaptiveTheme,
            context = context
        )

        // Daily Forecast
        DailyForecastSection(forecastData, adaptiveTheme, context)

        // Sun Times
        SunTimesCard(data = data, adaptiveTheme = adaptiveTheme, context = context)

        // Moon Info
        MoonInfoCard(data = data, adaptiveTheme = adaptiveTheme, context = context)

        // Activity Conditions
        ActivityConditionsCard(data = data, forecastData = forecastData, adaptiveTheme = adaptiveTheme, context = context)

        // Air Quality
        AirQualityCard(data = data, adaptiveTheme = adaptiveTheme, context = context)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LocationAndDateInfo(
    cityName: String,
    countryCode: String,
    adaptiveTheme: AdaptiveTheme,
    context: Context,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = context.getString(R.string.location),
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 6.dp)
            )
            Text(
                text = "$cityName, ${getCountryName(countryCode, context)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = adaptiveTheme.textColor,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Spacer kecil antara daerah dan tanggal
        Spacer(modifier = Modifier.height(2.dp))

        // Tanggal hari ini dengan margin bawah yang minimal
        Text(
            text = getFormattedDate(context),
            fontSize = 14.sp,
            color = adaptiveTheme.secondaryTextColor,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

@Composable
fun DefaultLocationAndDateInfo(
    adaptiveTheme: AdaptiveTheme,
    context: Context,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        // Placeholder untuk nama kota dan negara dengan icon lokasi - TANPA ColorFilter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            // Icon lokasi kecil
            Image(
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = context.getString(R.string.location),
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 6.dp)
            )
            Text(
                text = context.getString(R.string.your_location),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = adaptiveTheme.textColor,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Tanggal hari ini dengan margin bawah yang dikurangi
        Text(
            text = getFormattedDate(context),
            fontSize = 14.sp,
            color = adaptiveTheme.secondaryTextColor,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

// Search Popup Composable - VERSION SIMPLE FIX
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPopup(
    city: String,
    onCityChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    // Gunakan warna yang lebih kontras untuk teks
    val textColor = if (adaptiveTheme.textColor == Color.White) {
        Color.White
    } else {
        Color.Black
    }

    val textFieldBg = if (adaptiveTheme.textColor == Color.White) {
        Color(0xFF2D2D2D) // Dark background untuk dark mode
    } else {
        Color(0xFFF5F5F5) // Light background untuk light mode
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onDismiss()
            }
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 100.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {},
            colors = CardDefaults.cardColors(
                containerColor = adaptiveTheme.cardColor.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header dengan icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = context.getString(R.string.search),
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = context.getString(R.string.search_location),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Search field dengan teks yang jelas
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        value = city,
                        onValueChange = onCityChange,
                        placeholder = {
                            Text(
                                text = context.getString(R.string.enter_city_name),
                                color = adaptiveTheme.secondaryTextColor,
                                fontSize = 16.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = textColor,
                            unfocusedBorderColor = adaptiveTheme.secondaryTextColor.copy(alpha = 0.5f),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = textColor,
                            focusedContainerColor = textFieldBg,
                            unfocusedContainerColor = textFieldBg,
                            disabledContainerColor = textFieldBg,
                            errorContainerColor = textFieldBg
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = adaptiveTheme.primaryGradient
                                )
                            )
                            .clickable { onSearch() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = context.getString(R.string.search),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = context.getString(R.string.tap_outside_to_close),
                    fontSize = 13.sp,
                    color = adaptiveTheme.secondaryTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun WeatherDetails(
    data: WeatherModel,
    forecastData: NetworkResponse<ForecastResponse>?,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Main Weather Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Temperature dan icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    val currentWeatherIcon = data.weather.firstOrNull()?.icon ?: "01d"
                    WeatherIconMapper.LocalWeatherIcon(
                        iconCode = currentWeatherIcon,
                        contentDescription = "Current weather",
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${data.main.temp.toInt()}°",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = adaptiveTheme.textColor
                        )

                        val weatherDescription = data.weather.firstOrNull()?.description ?: "Unknown"
                        Text(
                            text = translateWeatherDescription(weatherDescription, context),
                            fontSize = 18.sp,
                            color = adaptiveTheme.secondaryTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                HighLowTempSeparateBoxes(
                    highTemp = getMaxTemp(data, forecastData),
                    lowTemp = getMinTemp(data, forecastData),
                    adaptiveTheme = adaptiveTheme,
                    context = context
                )

                Spacer(modifier = Modifier.height(24.dp))

                WeatherInfoCard(data, adaptiveTheme, context)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Hourly Forecast Section
        Text(
            text = context.getString(R.string.weather_forecast),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = adaptiveTheme.textColor,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hourly Forecast Cards
        when (val forecast = forecastData) {
            is NetworkResponse.Success -> {
                HourlyForecast(forecastData = forecast.data, adaptiveTheme = adaptiveTheme, context = context)
            }
            is NetworkResponse.Loading -> {
                CircularProgressIndicator(
                    color = adaptiveTheme.textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            else -> {
                Text(
                    text = context.getString(R.string.forecast_data_unavailable),
                    color = adaptiveTheme.secondaryTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrecipitationTimelineCard(forecastData = forecastData, adaptiveTheme = adaptiveTheme, context = context)

        Spacer(modifier = Modifier.height(16.dp))

        WeatherInfoPagerCard(
            data = data,
            forecast = (forecastData as? NetworkResponse.Success)?.data,
            adaptiveTheme = adaptiveTheme,
            context = context
        )

        Spacer(modifier = Modifier.height(16.dp))

        DailyForecastSection(forecastData, adaptiveTheme, context)

        Spacer(modifier = Modifier.height(16.dp))

        // Card IKU
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UVIndexCard(
                modifier = Modifier.weight(1f),
                data = data,
                adaptiveTheme = adaptiveTheme,
                context = context
            )

            HumidityCard(
                modifier = Modifier.weight(1f),
                data = data,
                adaptiveTheme = adaptiveTheme,
                context = context
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SunTimesCard(data = data, adaptiveTheme = adaptiveTheme, context = context)

        Spacer(modifier = Modifier.height(16.dp))

        MoonInfoCard(data = data, adaptiveTheme = adaptiveTheme, context = context)

        Spacer(modifier = Modifier.height(16.dp))

        ActivityConditionsCard(data = data, forecastData = forecastData, adaptiveTheme = adaptiveTheme, context = context)

        Spacer(modifier = Modifier.height(16.dp))

        AirQualityCard(data = data, adaptiveTheme = adaptiveTheme, context = context)

        Spacer(modifier = Modifier.height(50.dp))
    }
}

// Composable untuk Sun Times Card (Fajar & Senja) - DIBAWAH CARD IKU
@Composable
fun SunTimesCard(data: WeatherModel, adaptiveTheme: AdaptiveTheme, context: Context) {
    val sunTimes = calculateSunTimes(data)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header dengan icon sunrise
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_sunrise),
                    contentDescription = context.getString(R.string.sunrise),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${context.getString(R.string.sunrise)} & ${context.getString(R.string.sunset)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = adaptiveTheme.textColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content - Fajar dan Senja dalam row dengan icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Fajar dengan icon matahari terbit
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_sun_rise),
                        contentDescription = context.getString(R.string.sunrise),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.sunrise),
                        fontSize = 14.sp,
                        color = adaptiveTheme.secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sunTimes.sunrise,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveTheme.textColor
                    )
                }

                // Garis pemisah vertikal
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(adaptiveTheme.textColor.copy(alpha = 0.3f))
                )

                // Senja dengan icon matahari terbenam
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_sun_set),
                        contentDescription = context.getString(R.string.sunset),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.sunset),
                        fontSize = 14.sp,
                        color = adaptiveTheme.secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sunTimes.sunset,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveTheme.textColor
                    )
                }
            }
        }
    }
}

// Composable untuk Moon Info Card - DIBAWAH CARD FAJAR & SENJA
@Composable
fun MoonInfoCard(data: WeatherModel, adaptiveTheme: AdaptiveTheme, context: Context) {
    val moonData = calculateMoonData(data)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header dengan icon bulan
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_moon),
                    contentDescription = context.getString(R.string.moon_info),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = context.getString(R.string.moon_info),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = adaptiveTheme.textColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bulan Terbit & Terbenam dengan icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bulan Terbit dengan icon bulan terbit
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_moon_rise),
                        contentDescription = context.getString(R.string.moon_rise),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.moon_rise),
                        fontSize = 14.sp,
                        color = adaptiveTheme.secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = moonData.moonrise,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveTheme.textColor
                    )
                }

                // Garis pemisah vertikal
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(adaptiveTheme.textColor.copy(alpha = 0.3f))
                )

                // Bulan Terbenam dengan icon bulan terbenam
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_moon_set),
                        contentDescription = context.getString(R.string.moon_set),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.moon_set),
                        fontSize = 14.sp,
                        color = adaptiveTheme.secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = moonData.moonset,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveTheme.textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fase Bulan dengan icon bulan yang dinamis
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Icon Bulan sesuai fase
                Image(
                    painter = painterResource(id = getMoonPhaseIcon(moonData.moonPhase)),
                    contentDescription = context.getString(R.string.moon_phase),
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = context.getString(R.string.moon_phase),
                        fontSize = 14.sp,
                        color = adaptiveTheme.secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = translateMoonPhase(moonData.moonPhase, context),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveTheme.textColor
                    )
                }
            }
        }
    }
}

// Composable untuk UV Index Card
@Composable
fun UVIndexCard(
    modifier: Modifier = Modifier,
    data: WeatherModel,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    val uvData = calculateUVIndex(data, context)

    Card(
        modifier = modifier
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header dengan icon dan title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.uv),
                    contentDescription = "UV Index",
                    tint = adaptiveTheme.textColor,   // ⬅ ini yang penting!
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = context.getString(R.string.uv_index),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = adaptiveTheme.textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // UV Index Value dan Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = uvData.status,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = uvData.statusColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uvData.trend,
                        fontSize = 12.sp,
                        color = adaptiveTheme.secondaryTextColor
                    )
                }

                Text(
                    text = "%.1f".format(uvData.index),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = adaptiveTheme.textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar dengan gradient warna UV
            CustomUVProgressBar(
                progress = (uvData.index / 11.0).toFloat(),
                progressColor = uvData.statusColor,
                adaptiveTheme = adaptiveTheme
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = uvData.description,
                fontSize = 12.sp,
                color = adaptiveTheme.secondaryTextColor,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun HumidityCardImproved(
    modifier: Modifier = Modifier,
    data: WeatherModel,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    val humidityData = calculateHumidityDataImproved(data, context)

    Card(
        modifier = modifier
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header dengan icon dan title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.humidity),
                    contentDescription = null,
                    tint = adaptiveTheme.textColor,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = context.getString(R.string.humidity),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = adaptiveTheme.textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trend dan Status (TANPA PERSENTASE)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = humidityData.trend,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = adaptiveTheme.secondaryTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = humidityData.status,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = humidityData.statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            CustomProgressBar(
                progress = humidityData.percentage / 100f,
                progressColor = humidityData.statusColor,
                adaptiveTheme = adaptiveTheme
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = humidityData.description,
                fontSize = 12.sp,
                color = adaptiveTheme.secondaryTextColor,
                lineHeight = 14.sp
            )
        }
    }
}

// Fungsi helper untuk humidity data yang diperbarui
private fun calculateHumidityDataImproved(data: WeatherModel, context: Context): HumidityData {
    val humidity = data.main.humidity
    val yesterdayHumidity = humidity - 5 // Simulasi data kemarin

    val trend = if (context.resources.configuration.locales[0].language == "id") {
        when {
            humidity > yesterdayHumidity + 2 -> context.getString(R.string.lebihtinggidarikemarin)
            humidity < yesterdayHumidity - 2 -> context.getString(R.string.lebihrendahdarikemarin)
            else -> context.getString(R.string.stabilsepertikemarin)
        }
    } else {
        when {
            humidity > yesterdayHumidity + 2 -> context.getString(R.string.lebihtinggidarikemarin)
            humidity < yesterdayHumidity - 2 -> context.getString(R.string.lebihrendahdarikemarin)
            else -> context.getString(R.string.stabilsepertikemarin)
        }
    }

    val description = if (context.resources.configuration.locales[0].language == "id") {
        when {
            humidity <= 30 -> context.getString(R.string.udarasangatkering)
            humidity <= 50 -> context.getString(R.string.tingkatkelembapan)
            humidity <= 70 -> context.getString(R.string.kelembapancukupnyaman)
            humidity <= 85 -> context.getString(R.string.udaralembab)
            else -> context.getString(R.string.tingkatkelembapan)
        }
    } else {
        when {
            humidity <= 30 -> context.getString(R.string.udarasangatkering)
            humidity <= 50 -> context.getString(R.string.tingkatkelembapan)
            humidity <= 70 -> context.getString(R.string.kelembapancukupnyaman)
            humidity <= 85 -> context.getString(R.string.udaralembab)
            else -> context.getString(R.string.tingkatkelembapan)
        }
    }

    return if (context.resources.configuration.locales[0].language == "id") {
        when {
            humidity <= 30 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.sangatkering),
                statusColor = Color(0xFFFF9800),
                description = description,
                trend = trend
            )
            humidity <= 50 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.nyaman),
                statusColor = Color(0xFF4CAF50),
                description = description,
                trend = trend
            )
            humidity <= 70 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.moderate),
                statusColor = Color(0xFFFFC107),
                description = description,
                trend = trend
            )
            humidity <= 85 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.high),
                statusColor = Color(0xFFACD5F6),
                description = description,
                trend = trend
            )
            else -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.veryhigh),
                statusColor = Color(0xFFF44336),
                description = description,
                trend = trend
            )
        }
    } else {
        when {
            humidity <= 30 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.sangatkering),
                statusColor = Color(0xFFFF9800),
                description = description,
                trend = trend
            )
            humidity <= 50 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.nyaman),
                statusColor = Color(0xFF4CAF50),
                description = description,
                trend = trend
            )
            humidity <= 70 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.moderate),
                statusColor = Color(0xFFFFC107),
                description = description,
                trend = trend
            )
            humidity <= 85 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.high),
                statusColor = Color(0xFFACD5F6),
                description = description,
                trend = trend
            )
            else -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.veryhigh),
                statusColor = Color(0xFFF44336),
                description = description,
                trend = trend
            )
        }
    }
}

// Composable untuk Humidity Card
@Composable
fun HumidityCard(
    modifier: Modifier = Modifier,
    data: WeatherModel,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    val humidityData = calculateHumidityData(data, context)

    Card(
        modifier = modifier
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header dengan icon dan title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.humidity),
                    contentDescription = null,
                    tint = adaptiveTheme.textColor,   // ⬅ ini juga
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = context.getString(R.string.humidity),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = adaptiveTheme.textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Humidity Value dan Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = humidityData.status,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = humidityData.statusColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = humidityData.trend,
                        fontSize = 12.sp,
                        color = adaptiveTheme.secondaryTextColor
                    )
                }

                Text(
                    text = "${humidityData.percentage}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = adaptiveTheme.textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar dengan warna berdasarkan tingkat kelembapan
            CustomProgressBar(
                progress = humidityData.percentage / 100f,
                progressColor = humidityData.statusColor,
                adaptiveTheme = adaptiveTheme
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = humidityData.description,
                fontSize = 12.sp,
                color = adaptiveTheme.secondaryTextColor,
                lineHeight = 14.sp
            )
        }
    }
}

// Custom Progress Bar untuk UV Index
@Composable
fun CustomUVProgressBar(
    progress: Float,
    progressColor: Color,
    adaptiveTheme: AdaptiveTheme
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "UV Progress Animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(adaptiveTheme.secondaryTextColor.copy(alpha = 0.3f))
    ) {
        // Background gradient untuk UV
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50), // Hijau (rendah)
                            Color(0xFFFFC107), // Kuning (sedang)
                            Color(0xFFFF9800), // Orange (tinggi)
                            Color(0xFFF44336), // Merah (sangat tinggi)
                            Color(0xFF9C27B0)  // Ungu (ekstrem)
                        )
                    )
                )
        )

        // Progress indicator dengan animasi
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.8f))
        )

        // Border highlight
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                )
        )
    }
}

// Custom Progress Bar untuk Humidity
@Composable
fun CustomProgressBar(
    progress: Float,
    progressColor: Color,
    adaptiveTheme: AdaptiveTheme
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "Humidity Progress Animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(adaptiveTheme.secondaryTextColor.copy(alpha = 0.3f))
    ) {
        // Progress indicator dengan animasi
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            progressColor.copy(alpha = 0.7f),
                            progressColor
                        )
                    )
                )
        )

        // Border highlight
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                )
        )
    }
}

// Composable untuk High/Low Temperature dalam kotak terpisah
@Composable
fun HighLowTempSeparateBoxes(highTemp: Int, lowTemp: Int, adaptiveTheme: AdaptiveTheme, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // High Temperature Box
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = adaptiveTheme.cardColor
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = context.getString(R.string.high),
                    fontSize = 14.sp,
                    color = adaptiveTheme.secondaryTextColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${highTemp}°C",
                    fontSize = 18.sp,
                    color = adaptiveTheme.textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Low Temperature Box
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = adaptiveTheme.cardColor
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = context.getString(R.string.low),
                    fontSize = 14.sp,
                    color = adaptiveTheme.secondaryTextColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${lowTemp}°C",
                    fontSize = 18.sp,
                    color = adaptiveTheme.textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Composable untuk Weather Info dalam satu card dengan pembatas
@Composable
fun WeatherInfoCard(data: WeatherModel, adaptiveTheme: AdaptiveTheme, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            WeatherInfoItemInCard(
                title = context.getString(R.string.humidity),
                value = "${data.main.humidity}%",
                adaptiveTheme = adaptiveTheme,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(adaptiveTheme.textColor.copy(alpha = 0.3f))
            )

            WeatherInfoItemInCard(
                title = context.getString(R.string.wind),
                value = "${data.wind.speed.toInt()} km/h",
                adaptiveTheme = adaptiveTheme,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(adaptiveTheme.textColor.copy(alpha = 0.3f))
            )

            WeatherInfoItemInCard(
                title = context.getString(R.string.rain),
                value = "24%",
                adaptiveTheme = adaptiveTheme,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WeatherInfoItemInCard(
    title: String,
    value: String,
    adaptiveTheme: AdaptiveTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = adaptiveTheme.secondaryTextColor,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            color = adaptiveTheme.textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// Composable untuk Activity Conditions Card
@Composable
fun ActivityConditionsCard(
    data: WeatherModel,
    forecastData: NetworkResponse<ForecastResponse>?,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    val activities = generateActivityConditions(data, forecastData, context)
    val pagerState = rememberPagerState(initialPage = 0) { activities.size }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val activity = activities[page]
                ActivityConditionItem(activity = activity, adaptiveTheme = adaptiveTheme, context = context)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(activities.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (selected) adaptiveTheme.textColor
                                else adaptiveTheme.textColor.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityConditionItem(activity: ActivityCondition, adaptiveTheme: AdaptiveTheme, context: Context) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = activity.iconRes),
                contentDescription = activity.activity,
                modifier = Modifier.size(32.dp),
                colorFilter = ColorFilter.tint(adaptiveTheme.textColor) // ⬅️ INI YANG DITAMBAHKAN
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = activity.activity,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = adaptiveTheme.textColor,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = activity.status,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = activity.statusColor
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(
            text = activity.description,
            fontSize = 14.sp,
            color = adaptiveTheme.secondaryTextColor,
            lineHeight = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                activity.timeSlots.forEach { timeSlot ->
                    Text(
                        text = timeSlot.time,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = adaptiveTheme.textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Status dengan gambar PNG
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                activity.timeSlots.forEach { timeSlot ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Gambar status sesuai dengan status
                        val statusImageRes = when (timeSlot.status) {
                            context.getString(R.string.good) -> R.drawable.ic_good
                            context.getString(R.string.fair) -> R.drawable.ic_neutral
                            context.getString(R.string.poor) -> R.drawable.ic_bad
                            else -> R.drawable.ic_neutral
                        }

                        Image(
                            painter = painterResource(id = statusImageRes),
                            contentDescription = timeSlot.status,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = timeSlot.status,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = adaptiveTheme.textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AirQualityCard(data: WeatherModel, adaptiveTheme: AdaptiveTheme, context: Context) {
    val airQuality = calculateAirQuality(data, context)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = context.getString(R.string.air_quality),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = adaptiveTheme.textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = airQuality.status,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = airQuality.statusColor
                )

                Text(
                    text = "${airQuality.index}%",
                    fontSize = 14.sp,
                    color = adaptiveTheme.secondaryTextColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gunakan Custom Progress Bar untuk IKU
            CustomProgressBar(
                progress = airQuality.index / 100f,
                progressColor = airQuality.statusColor,
                adaptiveTheme = adaptiveTheme
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = airQuality.description,
                fontSize = 14.sp,
                color = adaptiveTheme.secondaryTextColor,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun PrecipitationTimelineCard(forecastData: NetworkResponse<ForecastResponse>?, adaptiveTheme: AdaptiveTheme, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = context.getString(R.string.precipitation_chance),
                fontSize = 18.sp,
                color = adaptiveTheme.textColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (forecastData) {
                is NetworkResponse.Success -> {
                    RealPrecipitationTimeline(forecastData.data, adaptiveTheme)
                }
                else -> {
                    PrecipitationTimelinePlaceholder(adaptiveTheme)
                }
            }
        }
    }
}

@Composable
fun RealPrecipitationTimeline(forecastData: ForecastResponse, adaptiveTheme: AdaptiveTheme) {
    val currentTime = System.currentTimeMillis()
    val twentyFourHoursLater = currentTime + 24 * 60 * 60 * 1000

    val nextForecasts = forecastData.list.filter { forecastItem ->
        val forecastTime = parseDateTime(forecastItem.dtTxt).time
        forecastTime in currentTime..twentyFourHoursLater
    }.take(6)

    if (nextForecasts.isNotEmpty()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                nextForecasts.forEach { forecast ->
                    Text(
                        text = formatTimeShort(forecast.dtTxt),
                        fontSize = 12.sp,
                        color = adaptiveTheme.secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
        PrecipitationTimelinePlaceholder(adaptiveTheme)
    }
}

@Composable
fun PrecipitationTimelinePlaceholder(adaptiveTheme: AdaptiveTheme) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("21:00", "00:00", "03:00", "06:00", "09:00", "12:00").forEach { time ->
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = adaptiveTheme.secondaryTextColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

@Composable
fun WeatherInfoPagerCard(
    data: WeatherModel,
    forecast: ForecastResponse?,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    val items = generateWeatherInfoItems(data, forecast, context)
    val pagerState = rememberPagerState(initialPage = 0) { items.size }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val item = items[page]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.icon, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                color = adaptiveTheme.secondaryTextColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.description,
                            fontSize = 14.sp,
                            color = adaptiveTheme.textColor,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 20.sp
                        )
                    }

                    Text(
                        text = item.value,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveTheme.textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(items.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (selected) adaptiveTheme.textColor
                                else adaptiveTheme.textColor.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun DailyForecastSection(forecastData: NetworkResponse<ForecastResponse>?, adaptiveTheme: AdaptiveTheme, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = adaptiveTheme.cardColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = context.getString(R.string.daily_forecast),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = adaptiveTheme.textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (forecastData) {
                is NetworkResponse.Success -> {
                    val daily = groupForecastByDay(forecastData.data)
                    DailyForecastList(daily, adaptiveTheme, context)
                }
                else -> Text(
                    text = context.getString(R.string.forecast_data_unavailable),
                    color = adaptiveTheme.secondaryTextColor
                )
            }
        }
    }
}

@Composable
fun DailyForecastList(list: List<DailyForecast>, adaptiveTheme: AdaptiveTheme, context: Context) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        list.forEachIndexed { index, item ->
            DailyForecastRow(item, index, adaptiveTheme, context)
        }
    }
}

@Composable
fun DailyForecastRow(item: DailyForecast, index: Int, adaptiveTheme: AdaptiveTheme, context: Context) {
    val dayName = when (index) {
        0 -> context.getString(R.string.today)
        1 -> context.getString(R.string.tomorrow)
        else -> SimpleDateFormat("EEE", Locale.getDefault()).format(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(item.date)!!
        )
    }.replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayName,
            fontSize = 16.sp,
            color = adaptiveTheme.textColor
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${item.humidity}%",
                color = adaptiveTheme.secondaryTextColor,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            WeatherIconMapper.LocalWeatherIcon(
                iconCode = item.icon,
                contentDescription = "Weather icon",
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "${item.maxTemp}° ${item.minTemp}°",
                color = adaptiveTheme.textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun HourlyForecast(forecastData: ForecastResponse, adaptiveTheme: AdaptiveTheme, context: Context) {
    val currentTime = System.currentTimeMillis()

    val upcomingForecasts = forecastData.list.filter { forecastItem ->
        val forecastTime = parseDateTime(forecastItem.dtTxt).time
        val twentyFourHoursLater = currentTime + 24 * 60 * 60 * 1000
        forecastTime in currentTime..twentyFourHoursLater
    }.take(8)

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(upcomingForecasts) { forecastItem ->
            HourlyForecastItem(forecastItem = forecastItem, adaptiveTheme = adaptiveTheme, context = context)
        }
    }
}

@Composable
fun HourlyForecastItem(
    forecastItem: com.luthfiana.dailyweathers.api.ForecastItem,
    adaptiveTheme: AdaptiveTheme,
    context: Context
) {
    val isHighlighted = formatTimeShort(forecastItem.dtTxt) == "Sekarang"

    // Warna yang sangat kontras - hitam untuk teks saat hover
    val containerBg = if (isHighlighted) Color.White else adaptiveTheme.cardColor
    val timeColor = if (isHighlighted) Color.Black else adaptiveTheme.textColor
    val tempColor = if (isHighlighted) Color.Black else adaptiveTheme.textColor

    Card(
        modifier = Modifier
            .width(65.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = formatTime(forecastItem.dtTxt),
                fontSize = 12.sp,
                color = timeColor,
                fontWeight = FontWeight.Medium
            )

            val weatherIcon = forecastItem.weather.firstOrNull()?.icon ?: "01d"
            WeatherIconMapper.LocalWeatherIcon(
                iconCode = weatherIcon,
                contentDescription = "Weather icon",
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = "${forecastItem.main.temp.toInt()}°",
                fontSize = 16.sp,
                color = tempColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Helper Functions yang Diperbarui
private fun getCurrentDate(context: Context): String {
    val locale = if (context.resources.configuration.locales[0].language == "id") {
        Locale("id", "ID")
    } else {
        Locale.getDefault()
    }
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", locale)
    return dateFormat.format(Date())
}

private fun getFormattedDate(context: Context): String {
    val locale = if (context.resources.configuration.locales[0].language == "id") {
        Locale("id", "ID")
    } else {
        Locale.getDefault()
    }
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
    return dateFormat.format(Date())
}

private fun getCountryName(countryCode: String, context: Context): String {
    val countryNames = if (context.resources.configuration.locales[0].language == "id") {
        mapOf(
            "ID" to "Indonesia",
            "US" to "Amerika Serikat",
            "GB" to "Inggris",
            "DE" to "Jerman",
            "FR" to "Prancis",
            "JP" to "Jepang",
            "KR" to "Korea Selatan",
            "CN" to "China",
            "IN" to "India",
            "BR" to "Brazil",
            "RU" to "Rusia",
            "IT" to "Italia",
            "ES" to "Spanyol",
            "CA" to "Kanada",
            "AU" to "Australia",
            "MX" to "Meksiko",
            "NL" to "Belanda",
            "SE" to "Swedia",
            "NO" to "Norwegia",
            "DK" to "Denmark",
            "FI" to "Finlandia",
            "SG" to "Singapura",
            "MY" to "Malaysia",
            "TH" to "Thailand",
            "VN" to "Vietnam",
            "PH" to "Filipina"
        )
    } else {
        mapOf(
            "ID" to "Indonesia",
            "US" to "United States",
            "GB" to "United Kingdom",
            "DE" to "Germany",
            "FR" to "France",
            "JP" to "Japan",
            "KR" to "South Korea",
            "CN" to "China",
            "IN" to "India",
            "BR" to "Brazil",
            "RU" to "Russia",
            "IT" to "Italy",
            "ES" to "Spain",
            "CA" to "Canada",
            "AU" to "Australia",
            "MX" to "Mexico",
            "NL" to "Netherlands",
            "SE" to "Sweden",
            "NO" to "Norway",
            "DK" to "Denmark",
            "FI" to "Finland",
            "SG" to "Singapore",
            "MY" to "Malaysia",
            "TH" to "Thailand",
            "VN" to "Vietnam",
            "PH" to "Philippines"
        )
    }
    return countryNames[countryCode.uppercase()] ?: countryCode
}

private fun getMaxTemp(data: WeatherModel, forecastData: NetworkResponse<ForecastResponse>?): Int {
    return when (forecastData) {
        is NetworkResponse.Success -> {
            val temps = forecastData.data.list.map { it.main.temp }
            temps.maxOrNull()?.toInt() ?: data.main.temp.toInt() + 2
        }
        else -> data.main.temp.toInt() + 2
    }
}

private fun getMinTemp(data: WeatherModel, forecastData: NetworkResponse<ForecastResponse>?): Int {
    return when (forecastData) {
        is NetworkResponse.Success -> {
            val temps = forecastData.data.list.map { it.main.temp }
            temps.minOrNull()?.toInt() ?: data.main.temp.toInt() - 2
        }
        else -> data.main.temp.toInt() - 2
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

fun generateActivityConditions(
    data: WeatherModel,
    forecastData: NetworkResponse<ForecastResponse>?,
    context: Context
): List<ActivityCondition> {
    val temp = data.main.temp.toInt()
    val humidity = data.main.humidity
    val weatherDesc = data.weather.firstOrNull()?.description ?: ""
    val windSpeed = data.wind.speed

    val timeSlots = when (forecastData) {
        is NetworkResponse.Success -> {
            generateTimeSlotsFromForecast(forecastData.data, context)
        }
        else -> {
            generateDynamicTimeSlots(context)
        }
    }

    return listOf(
        ActivityCondition(
            activity = context.getString(R.string.running),
            iconRes = R.drawable.ic_running,
            status = calculateRunningStatus(temp, humidity, weatherDesc, windSpeed, context),
            statusColor = getStatusColor(calculateRunningStatus(temp, humidity, weatherDesc, windSpeed, context)),
            description = generateRunningDescription(temp, humidity, weatherDesc, windSpeed, context),
            timeSlots = timeSlots
        ),
        ActivityCondition(
            activity = context.getString(R.string.cycling),
            iconRes = R.drawable.ic_cycling,
            status = calculateCyclingStatus(temp, humidity, weatherDesc, windSpeed, context),
            statusColor = getStatusColor(calculateCyclingStatus(temp, humidity, weatherDesc, windSpeed, context)),
            description = generateCyclingDescription(temp, humidity, weatherDesc, windSpeed, context),
            timeSlots = timeSlots.map {
                it.copy(status = calculateTimeSlotStatus(it.time, "cycling", data, forecastData, context))
            }
        ),
        ActivityCondition(
            activity = context.getString(R.string.gardening),
            iconRes = R.drawable.ic_gardening,
            status = calculateGardeningStatus(temp, humidity, weatherDesc, windSpeed, context),
            statusColor = getStatusColor(calculateGardeningStatus(temp, humidity, weatherDesc, windSpeed, context)),
            description = generateGardeningDescription(temp, humidity, weatherDesc, windSpeed, context),
            timeSlots = timeSlots.map {
                it.copy(status = calculateTimeSlotStatus(it.time, "gardening", data, forecastData, context))
            }
        )
    )
}

private fun generateTimeSlotsFromForecast(forecastData: ForecastResponse, context: Context): List<TimeSlot> {
    val currentTime = System.currentTimeMillis()

    // Ambil 3 forecast berikutnya dengan interval 2 jam
    val upcomingForecasts = forecastData.list.filter { forecastItem ->
        val forecastTime = parseDateTime(forecastItem.dtTxt).time
        forecastTime >= currentTime // Hanya forecast yang belum lewat
    }.take(3)

    return if (upcomingForecasts.isNotEmpty()) {
        upcomingForecasts.mapIndexed { index, forecast ->
            val time = formatTimeForActivity(forecast.dtTxt, context)
            val status = calculateForecastStatus(forecast, context)
            TimeSlot(time, status, getStatusColor(status))
        }
    } else {
        // Fallback: jika tidak ada forecast, gunakan waktu dinamis dari sekarang
        generateDynamicTimeSlots(context)
    }
}

// Fungsi baru untuk menghasilkan timeslot dinamis berdasarkan waktu saat ini
private fun generateDynamicTimeSlots(context: Context): List<TimeSlot> {
    val calendar = Calendar.getInstance()
    val timeSlots = mutableListOf<TimeSlot>()

    // Generate 3 timeslot dengan interval 2 jam dari sekarang
    for (i in 0..2) {
        calendar.add(Calendar.HOUR_OF_DAY, if (i == 0) 0 else 2)

        val timeFormat = SimpleDateFormat("HH.mm", Locale.getDefault())
        val time = if (i == 0) {
            if (context.resources.configuration.locales[0].language == "id") "Sekarang" else "Now"
        } else {
            timeFormat.format(calendar.time)
        }

        // Untuk fallback, kita beri status "Baik" sebagai default
        timeSlots.add(TimeSlot(time, context.getString(R.string.good), getStatusColor(context.getString(R.string.good))))
    }

    return timeSlots
}

private fun formatTimeForActivity(dateTime: String, context: Context): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH.mm", Locale.getDefault())
        val date = inputFormat.parse(dateTime)

        val currentTime = System.currentTimeMillis()
        val forecastTime = date?.time ?: currentTime

        // Jika forecast dalam 1 jam ke depan, tampilkan "Sekarang" atau "Now"
        if (Math.abs(forecastTime - currentTime) < 3600000) {
            if (context.resources.configuration.locales[0].language == "id") "Sekarang" else "Now"
        } else {
            outputFormat.format(date ?: Date())
        }
    } catch (e: Exception) {
        dateTime.substring(11, 16).replace(':', '.')
    }
}

private fun calculateForecastStatus(forecast: com.luthfiana.dailyweathers.api.ForecastItem, context: Context): String {
    val temp = forecast.main.temp.toInt()
    val humidity = forecast.main.humidity
    val weatherDesc = forecast.weather.firstOrNull()?.description ?: ""
    val pop = forecast.pop ?: 0.0

    return when {
        // Kondisi Buruk
        pop > 0.7 || weatherDesc.contains("rain", ignoreCase = true) -> context.getString(R.string.poor)
        temp >= 35 || temp <= 5 -> context.getString(R.string.poor)
        humidity >= 85 -> context.getString(R.string.poor)
        weatherDesc.contains("storm", ignoreCase = true) -> context.getString(R.string.poor)

        // Kondisi Sedang
        pop > 0.3 -> context.getString(R.string.fair)
        temp >= 30 || temp <= 10 -> context.getString(R.string.fair)
        humidity >= 70 -> context.getString(R.string.fair)
        weatherDesc.contains("cloud", ignoreCase = true) -> context.getString(R.string.fair)

        // Kondisi Baik (default)
        else -> context.getString(R.string.good)
    }
}

private fun calculateTimeSlotStatus(
    time: String,
    activity: String,
    data: WeatherModel,
    forecastData: NetworkResponse<ForecastResponse>?,
    context: Context
): String {
    return when (activity) {
        "running" -> {
            when {
                time == "Sekarang" || time == "Now" -> calculateRunningStatus(
                    data.main.temp.toInt(),
                    data.main.humidity,
                    data.weather.firstOrNull()?.description ?: "",
                    data.wind.speed,
                    context
                )
                else -> context.getString(R.string.good)
            }
        }
        "cycling" -> {
            when {
                time == "Sekarang" || time == "Now" -> calculateCyclingStatus(
                    data.main.temp.toInt(),
                    data.main.humidity,
                    data.weather.firstOrNull()?.description ?: "",
                    data.wind.speed,
                    context
                )
                else -> context.getString(R.string.good)
            }
        }
        "gardening" -> {
            when {
                time == "Sekarang" || time == "Now" -> calculateGardeningStatus(
                    data.main.temp.toInt(),
                    data.main.humidity,
                    data.weather.firstOrNull()?.description ?: "",
                    data.wind.speed,
                    context
                )
                else -> context.getString(R.string.good)
            }
        }
        else -> context.getString(R.string.good)
    }
}

private fun generateRunningDescription(
    temp: Int,
    humidity: Int,
    weatherDesc: String,
    windSpeed: Double,
    context: Context
): String {
    val condition = getRunningCondition(temp, humidity, weatherDesc, windSpeed, context)
    return context.getString(
        R.string.running_description,
        temp,
        humidity,
        condition
    )
}

private fun generateCyclingDescription(
    temp: Int,
    humidity: Int,
    weatherDesc: String,
    windSpeed: Double,
    context: Context
): String {
    val condition = getCyclingCondition(temp, humidity, weatherDesc, windSpeed, context)
    return context.getString(
        R.string.cycling_description,
        temp,
        windSpeed,
        condition
    )
}

private fun generateGardeningDescription(
    temp: Int,
    humidity: Int,
    weatherDesc: String,
    windSpeed: Double,
    context: Context
): String {
    val condition = getGardeningCondition(temp, humidity, weatherDesc, windSpeed, context)
    return context.getString(
        R.string.gardening_description,
        temp,
        humidity,
        condition
    )
}

private fun getRunningCondition(
    temp: Int,
    humidity: Int,
    weatherDesc: String,
    windSpeed: Double,
    context: Context
): String {
    return when {
        temp > 32 -> context.getString(R.string.too_hot_for_running)
        temp < 18 -> context.getString(R.string.too_cold_for_running)
        humidity > 80 -> context.getString(R.string.high_humidity_uncomfortable)
        humidity < 60 -> context.getString(R.string.fair_for_running)
        else -> context.getString(R.string.ideal_for_running)
    }
}

private fun getCyclingCondition(
    temp: Int,
    humidity: Int,
    weatherDesc: String,
    windSpeed: Double,
    context: Context
): String {
    return when {
        weatherDesc.contains("rain", true) -> context.getString(R.string.rain_unsafe_for_cycling)
        windSpeed > 25 -> context.getString(R.string.strong_wind_dangerous)
        windSpeed < 10 -> context.getString(R.string.good_conditions_for_cycling)
        else -> context.getString(R.string.fair_conditions_for_cycling)
    }
}

private fun getGardeningCondition(
    temp: Int,
    humidity: Int,
    weatherDesc: String,
    windSpeed: Double,
    context: Context
): String {
    return when {
        temp < 10 -> context.getString(R.string.freezing_temperature_damages_plants)
        weatherDesc.contains("storm", true) -> context.getString(R.string.storm_can_damage_plants)
        humidity in 40..70 -> context.getString(R.string.good_time_for_gardening)
        else -> context.getString(R.string.fair_conditions_for_gardening)
    }
}


fun calculateRunningStatus(temp: Int, humidity: Int, weatherDesc: String, windSpeed: Double, context: Context): String {
    return when {
        weatherDesc.contains("rain", ignoreCase = true) -> context.getString(R.string.poor)
        temp >= 35 || temp <= 5 -> context.getString(R.string.poor)
        humidity >= 85 -> context.getString(R.string.poor)
        windSpeed > 8 -> context.getString(R.string.poor)
        temp >= 30 || temp <= 10 || humidity >= 70 -> context.getString(R.string.fair)
        else -> context.getString(R.string.good)
    }
}

fun calculateCyclingStatus(temp: Int, humidity: Int, weatherDesc: String, windSpeed: Double, context: Context): String {
    return when {
        weatherDesc.contains("rain", ignoreCase = true) -> context.getString(R.string.poor)
        temp >= 38 || temp <= 0 -> context.getString(R.string.poor)
        windSpeed > 10 -> context.getString(R.string.poor)
        temp >= 32 || temp <= 5 || humidity >= 75 -> context.getString(R.string.fair)
        else -> context.getString(R.string.good)
    }
}

fun calculateGardeningStatus(temp: Int, humidity: Int, weatherDesc: String, windSpeed: Double, context: Context): String {
    return when {
        temp <= 0 -> context.getString(R.string.poor)
        weatherDesc.contains("storm", ignoreCase = true) -> context.getString(R.string.poor)
        temp >= 40 -> context.getString(R.string.fair)
        else -> context.getString(R.string.good)
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Baik", "Good" -> Color(0xFF4CAF50)
        "Sedang", "Fair" -> Color(0xFFFFC107)
        "Buruk", "Poor" -> Color(0xFFF44336)
        else -> Color.White
    }
}

fun calculateAirQuality(data: WeatherModel, context: Context): AirQualityData {
    val humidity = data.main.humidity
    val pressure = data.main.pressure
    val windSpeed = data.wind.speed

    var score = 50

    if (windSpeed in 2.0..6.0) score += 20
    if (pressure in 1010..1020) score += 15
    if (humidity in 40..60) score += 15

    val normalizedScore = score.coerceIn(0, 100)

    return if (context.resources.configuration.locales[0].language == "id") {
        when {
            normalizedScore >= 80 -> AirQualityData(
                index = normalizedScore,
                status = context.getString(R.string.healthy),
                statusColor = Color(0xFF4CAF50),
                description = context.getString(R.string.air_quality_good)
            )
            normalizedScore >= 60 -> AirQualityData(
                index = normalizedScore,
                status = context.getString(R.string.moderate),
                statusColor = Color(0xFFFFC107),
                description = context.getString(R.string.air_quality_moderate),
            )
            normalizedScore >= 40 -> AirQualityData(
                index = normalizedScore,
                status = context.getString(R.string.unhealthy_for_sensitive_groups),
                statusColor = Color(0xFFFF9800),
                description = context.getString(R.string.unhealthy_for_sensitive_groups),
            )
            else -> AirQualityData(
                index = normalizedScore,
                status = context.getString(R.string.unhealthy),
                statusColor = Color(0xFFF44336),
                description = context.getString(R.string.air_quality_unhealthy),
            )
        }
    } else {
        when {
            normalizedScore >= 80 -> AirQualityData(
                index = normalizedScore,
                status = context.getString(R.string.healthy),
                statusColor = Color(0xFF4CAF50),
                description = context.getString(R.string.air_quality_good),
            )
            normalizedScore >= 60 -> AirQualityData(
                index = normalizedScore,
                status = context.getString(R.string.moderate),
                statusColor = Color(0xFFFFC107),
                description = context.getString(R.string.air_quality_moderate),
            )
            normalizedScore >= 40 -> AirQualityData(
                index = normalizedScore,
                status = context.getString(R.string.unhealthy_for_sensitive_groups),
                statusColor = Color(0xFFFF9800),
                description = context.getString(R.string.air_quality_unhealthy_sensitive),
            )
            else -> AirQualityData(
                index = normalizedScore,
                status = context.getString(R.string.unhealthy),
                statusColor = Color(0xFFF44336),
                description = context.getString(R.string.air_quality_unhealthy),
            )
        }
    }
}

fun generateWeatherInfoItems(
    data: WeatherModel,
    forecast: ForecastResponse?,
    context: Context
): List<WeatherInfoItem> {
    val feels = data.main.feelsLike.toInt()
    val hum = data.main.humidity
    val realTemp = data.main.temp.toInt()
    val wind = data.wind.speed
    val desc = data.weather.firstOrNull()?.description ?: ""

    return listOf(
        WeatherInfoItem(
            icon = "🌡️",
            title = context.getString(R.string.feels_like_tomorrow),
            description = context.getString(R.string.humidity_makes_feel_like, feels),
            value = "$feels°"
        ),
        WeatherInfoItem(
            icon = "💧",
            title = context.getString(R.string.air_humidity),
            description = context.getString(R.string.humidity_reaches, hum),
            value = "$hum%"
        ),
        WeatherInfoItem(
            icon = "💨",
            title = context.getString(R.string.wind_speed),
            description = context.getString(R.string.wind_blowing, wind),
            value = "${"%.1f".format(wind)}"
        ),
        WeatherInfoItem(
            icon = "🌥️",
            title = context.getString(R.string.weather_condition),
            description = translateWeatherDescription(desc, context),
            value = "${realTemp}°"
        )
    )
}

private fun translateWeatherDescription(description: String, context: Context): String {
    return when {
        description.contains("clear", ignoreCase = true) -> context.getString(R.string.clear_sky)
        description.contains("few clouds", ignoreCase = true) -> context.getString(R.string.few_clouds)
        description.contains("scattered clouds", ignoreCase = true) -> context.getString(R.string.scattered_clouds)
        description.contains("broken clouds", ignoreCase = true) -> context.getString(R.string.broken_clouds)
        description.contains("overcast clouds", ignoreCase = true) -> context.getString(R.string.overcast_clouds)
        description.contains("light rain", ignoreCase = true) -> context.getString(R.string.light_rain)
        description.contains("moderate rain", ignoreCase = true) -> context.getString(R.string.moderate_rain)
        description.contains("heavy rain", ignoreCase = true) -> context.getString(R.string.heavy_rain)
        description.contains("thunderstorm", ignoreCase = true) -> context.getString(R.string.thunderstorm)
        description.contains("snow", ignoreCase = true) -> context.getString(R.string.snow)
        description.contains("mist", ignoreCase = true) -> context.getString(R.string.mist)
        description.contains("shower rain", ignoreCase = true) -> context.getString(R.string.light_rain)
        description.contains("rain", ignoreCase = true) -> context.getString(R.string.moderate_rain)
        description.contains("cloud", ignoreCase = true) -> context.getString(R.string.scattered_clouds)
        else -> description
    }
}
// Fungsi untuk menerjemahkan fase bulan
private fun translateMoonPhase(moonPhase: String, context: Context): String {
    return if (context.resources.configuration.locales[0].language == "id") {
        when {
            moonPhase.contains("baru", ignoreCase = true) -> context.getString(R.string.new_moon)
            moonPhase.contains("sabit awal", ignoreCase = true) -> context.getString(R.string.waxing_crescent)
            moonPhase.contains("paruh awal", ignoreCase = true) -> context.getString(R.string.first_quarter)
            moonPhase.contains("cembung", ignoreCase = true) -> context.getString(R.string.waxing_gibbous)
            moonPhase.contains("purnama", ignoreCase = true) -> context.getString(R.string.full_moon)
            moonPhase.contains("cembung akhir", ignoreCase = true) -> context.getString(R.string.waning_gibbous)
            moonPhase.contains("paruh akhir", ignoreCase = true) -> context.getString(R.string.last_quarter)
            moonPhase.contains("sabit akhir", ignoreCase = true) -> context.getString(R.string.waning_crescent)
            else -> moonPhase
        }
    } else {
        when {
            moonPhase.contains("Bulan Baru") -> context.getString(R.string.new_moon)
            moonPhase.contains("Bulan Sabit Awal") -> context.getString(R.string.waxing_crescent)
            moonPhase.contains("Bulan Paruh Awal") -> context.getString(R.string.first_quarter)
            moonPhase.contains("Bulan Cembung Awal") -> context.getString(R.string.waxing_gibbous)
            moonPhase.contains("Bulan Purnama") -> context.getString(R.string.full_moon)
            moonPhase.contains("Bulan Cembung Akhir") -> context.getString(R.string.waning_gibbous)
            moonPhase.contains("Bulan Paruh Akhir") -> context.getString(R.string.last_quarter)
            moonPhase.contains("Bulan Sabit Akhir") -> context.getString(R.string.waning_crescent)
            else -> moonPhase
        }
    }
}

// Helper functions untuk UV Index
private fun calculateUVIndex(data: WeatherModel, context: Context): UVIndexData {
    // Simulasi perhitungan UV index berdasarkan data cuaca
    val temp = data.main.temp.toInt()
    val hourOfDay = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toInt()

    // Base UV calculation (sederhana)
    var uvIndex = when {
        hourOfDay in 10..14 -> (temp / 10.0) + 2.0 // Peak hours
        hourOfDay in 7..9 || hourOfDay in 15..17 -> (temp / 12.0) + 1.0 // Moderate hours
        else -> (temp / 15.0) // Low hours
    }

    // Adjust for weather condition
    val weatherDesc = data.weather.firstOrNull()?.description ?: ""
    val cloudAdjustment = when {
        weatherDesc.contains("clear", ignoreCase = true) -> 1.0
        weatherDesc.contains("cloud", ignoreCase = true) -> 0.7
        weatherDesc.contains("rain", ignoreCase = true) -> 0.5
        else -> 0.8
    }

    uvIndex *= cloudAdjustment

    // Ensure within reasonable bounds
    uvIndex = uvIndex.coerceIn(0.0, 11.0)

    return if (context.resources.configuration.locales[0].language == "id") {
        when {
            uvIndex <= 2.0 -> UVIndexData(
                index = uvIndex,
                status = context.getString(R.string.low),
                statusColor = Color(0xFF4CAF50),
                description = context.getString(R.string.amantanpaperlindungan),
                trend = context.getString(R.string.stable),
            )
            uvIndex <= 5.0 -> UVIndexData(
                index = uvIndex,
                status = context.getString(R.string.moderate),
                statusColor = Color(0xFFFFC107),
                description = context.getString(R.string.uv_moderate),
                trend = context.getString(R.string.stable),
            )
            uvIndex <= 7.0 -> UVIndexData(
                index = uvIndex,
                status = context.getString(R.string.high),
                statusColor = Color(0xFFFF9800),
                description = context.getString(R.string.uv_high),
                trend = context.getString(R.string.lebihtinggidarikemarin)
            )
            uvIndex <= 10.0 -> UVIndexData(
                index = uvIndex,
                status = context.getString(R.string.veryhigh),
                statusColor = Color(0xFFF44336),
                description = context.getString(R.string.uv_very_high),
                trend = context.getString(R.string.uv_very_high),
            )
            else -> UVIndexData(
                index = uvIndex,
                status = context.getString(R.string.extrem),
                statusColor = Color(0xFF9C27B0),
                description = context.getString(R.string.uv_extreme),
                trend = context.getString(R.string.levelberbahaya)
            )
        }
    } else {
        when {
            uvIndex <= 2.0 -> UVIndexData(
                index = uvIndex,
                status = context.getString(R.string.low),
                statusColor = Color(0xFF4CAF50),
                description = context.getString(R.string.amantanpaperlindungan),
                trend = context.getString(R.string.stable),
            )
            uvIndex <= 5.0 -> UVIndexData(
                index = uvIndex,
                status = context.getString(R.string.moderate),
                statusColor = Color(0xFFFFC107),
                description = context.getString(R.string.uv_moderate),
                trend = context.getString(R.string.currently_high),
            )
            uvIndex <= 7.0 -> UVIndexData(
                index = uvIndex,
                status = context.getString(R.string.high),
                statusColor = Color(0xFFFF9800),
                description = context.getString(R.string.uv_high),
                trend = context.getString(R.string.higher_than_yesterday),
            )
            uvIndex <= 10.0 -> UVIndexData(
                index = uvIndex,
                status = context.getString(R.string.veryhigh),
                statusColor = Color(0xFFF44336),
                description = context.getString(R.string.uv_very_high),
                trend = context.getString(R.string.veryhigh)
            )
            else -> UVIndexData(
                index = uvIndex,
                status = context.getString(R.string.extrem),
                statusColor = Color(0xFF9C27B0),
                description = context.getString(R.string.uv_extreme),
                trend = context.getString(R.string.levelberbahaya),
            )
        }
    }
}

// Helper functions untuk Humidity
private fun calculateHumidityData(data: WeatherModel, context: Context): HumidityData {
    val humidity = data.main.humidity
    val yesterdayHumidity = humidity - 5 // Simulasi data kemarin

    val trend = if (context.resources.configuration.locales[0].language == "id") {
        when {
            humidity > yesterdayHumidity + 2 -> context.getString(R.string.lebihtinggidarikemarin)
            humidity < yesterdayHumidity - 2 -> context.getString(R.string.lebihrendahdarikemarin)
            else -> context.getString(R.string.stabilsepertikemarin)
        }
    } else {
        when {
            humidity > yesterdayHumidity + 2 -> context.getString(R.string.lebihtinggidarikemarin)
            humidity < yesterdayHumidity - 2 -> context.getString(R.string.lebihrendahdarikemarin)
            else -> context.getString(R.string.stabilsepertikemarin)
        }
    }

    return if (context.resources.configuration.locales[0].language == "id") {
        when {
            humidity <= 30 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.sangatkering),
                statusColor = Color(0xFFFF9800),
                description = context.getString(R.string.udarasangatkering),
                trend = trend
            )
            humidity <= 50 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.nyaman),
                statusColor = Color(0xFF4CAF50),
                description = context.getString(R.string.tingkatkelembapan),
                trend = trend
            )
            humidity <= 70 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.moderate),
                statusColor = Color(0xFFFFC107),
                description = context.getString(R.string.kelembapancukupnyaman),
                trend = trend
            )
            humidity <= 85 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.high),
                statusColor = Color(0xFFACD5F6),
                description = context.getString(R.string.udaralembab),
                trend = trend
            )
            else -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.veryhigh),
                statusColor = Color(0xFFF44336),
                description = context.getString(R.string.udarasangatlembab),
                trend = trend
            )
        }
    } else {
        when {
            humidity <= 30 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.sangatkering),
                statusColor = Color(0xFFFF9800),
                description = context.getString(R.string.udarasangatkering),
                trend = trend
            )
            humidity <= 50 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.nyaman),
                statusColor = Color(0xFF4CAF50),
                description = context.getString(R.string.tingkatkelembapan),
                trend = trend
            )
            humidity <= 70 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.moderate),
                statusColor = Color(0xFFFFC107),
                description = context.getString(R.string.kelembapancukupnyaman),
                trend = trend
            )
            humidity <= 85 -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.high),
                statusColor = Color(0xFFACD5F6),
                description = context.getString(R.string.udaralembab),
                trend = trend
            )
            else -> HumidityData(
                percentage = humidity,
                status = context.getString(R.string.veryhigh),
                statusColor = Color(0xFFF44336),
                description = context.getString(R.string.udarasangatlembab),
                trend = trend
            )
        }
    }
}

private fun parseDateTime(dateTime: String): Date {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        inputFormat.parse(dateTime) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}

private fun formatTime(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateTime.substring(11, 16)
    }
}

private fun formatTimeShort(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        val timeStr = outputFormat.format(date ?: Date())

        val currentTime = System.currentTimeMillis()
        val forecastTime = parseDateTime(dateTime).time
        if (Math.abs(forecastTime - currentTime) < 3600000) {
            "Sekarang"
        } else {
            timeStr
        }
    } catch (e: Exception) {
        dateTime.substring(11, 16)
    }
}

// Helper function untuk menghitung waktu matahari
private fun calculateSunTimes(data: WeatherModel): SunTimesData {
    // Konversi timestamp UNIX ke waktu lokal
    val sunriseTime = formatTimeFromTimestamp(data.sys.sunrise * 1000L)
    val sunsetTime = formatTimeFromTimestamp(data.sys.sunset * 1000L)

    return SunTimesData(
        sunrise = sunriseTime,
        sunset = sunsetTime
    )
}

// Helper function untuk menghitung data bulan yang lebih akurat
private fun calculateMoonData(data: WeatherModel): MoonData {
    // Perhitungan fase bulan berdasarkan tanggal (sederhana)
    val calendar = Calendar.getInstance()
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)

    // Simulasi perhitungan fase bulan berdasarkan tanggal
    val moonPhase = calculateMoonPhase(day, month, year)

    // Simulasi waktu bulan terbit dan terbenam berdasarkan lokasi dan tanggal
    val currentTime = System.currentTimeMillis()
    val moonriseTime = calculateMoonRiseTime(currentTime, data.coord.lat, data.coord.lon)
    val moonsetTime = calculateMoonSetTime(currentTime, data.coord.lat, data.coord.lon)

    return MoonData(
        moonrise = moonriseTime,
        moonset = moonsetTime,
        moonPhase = moonPhase
    )
}

// Fungsi untuk menghitung fase bulan (sederhana)
private fun calculateMoonPhase(day: Int, month: Int, year: Int): String {
    // Perhitungan sederhana fase bulan berdasarkan tanggal
    val daysInCycle = 29.53
    val knownNewMoon = 6 // Contoh: new moon pada tanggal 6 bulan ini

    val daysSinceNewMoon = (day - knownNewMoon) % daysInCycle.toInt()

    return when {
        daysSinceNewMoon < 1 -> "Bulan Baru"
        daysSinceNewMoon < 7 -> "Bulan Sabit Awal"
        daysSinceNewMoon < 8 -> "Bulan Paruh Awal"
        daysSinceNewMoon < 14 -> "Bulan Cembung Awal"
        daysSinceNewMoon < 15 -> "Bulan Purnama"
        daysSinceNewMoon < 22 -> "Bulan Cembung Akhir"
        daysSinceNewMoon < 23 -> "Bulan Paruh Akhir"
        daysSinceNewMoon < 29 -> "Bulan Sabit Akhir"
        else -> "Bulan Baru"
    }
}

// Fungsi untuk menghitung waktu bulan terbit (sederhana)
private fun calculateMoonRiseTime(currentTime: Long, lat: Double, lon: Double): String {
    // Simulasi perhitungan bulan terbit berdasarkan lokasi
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentTime

    // Tambahkan offset berdasarkan lokasi dan tanggal
    val hourOffset = (lon / 15).toInt() // Perkiraan berdasarkan longitude
    calendar.add(Calendar.HOUR_OF_DAY, 18 + hourOffset) // Bulan biasanya terbit malam hari

    val dateFormat = SimpleDateFormat("HH.mm", Locale.getDefault())
    return dateFormat.format(calendar.time)
}

// Fungsi untuk menghitung waktu bulan terbenam (sederhana)
private fun calculateMoonSetTime(currentTime: Long, lat: Double, lon: Double): String {
    // Simulasi perhitungan bulan terbenam berdasarkan lokasi
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentTime

    // Tambahkan offset berdasarkan lokasi dan tanggal
    val hourOffset = (lon / 15).toInt()
    calendar.add(Calendar.HOUR_OF_DAY, 6 + hourOffset) // Bulan biasanya terbenam pagi hari

    val dateFormat = SimpleDateFormat("HH.mm", Locale.getDefault())
    return dateFormat.format(calendar.time)
}

// Helper function untuk format waktu dari timestamp
private fun formatTimeFromTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("HH.mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

// Fungsi untuk mendapatkan icon bulan berdasarkan fase
private fun getMoonPhaseIcon(moonPhase: String): Int {
    return when {
        moonPhase.contains("baru", ignoreCase = true) -> R.drawable.ic_moon_new
        moonPhase.contains("sabit awal", ignoreCase = true) -> R.drawable.ic_moon_crescent_waxing
        moonPhase.contains("paruh awal", ignoreCase = true) -> R.drawable.ic_moon_first_quarter
        moonPhase.contains("cembung", ignoreCase = true) -> R.drawable.ic_moon_waxing_gibbous
        moonPhase.contains("purnama", ignoreCase = true) -> R.drawable.ic_moon_full
        moonPhase.contains("cembung akhir", ignoreCase = true) -> R.drawable.ic_moon_waning_gibbous
        moonPhase.contains("paruh akhir", ignoreCase = true) -> R.drawable.ic_moon_last_quarter
        moonPhase.contains("sabit akhir", ignoreCase = true) -> R.drawable.ic_moon_crescent_waning
        else -> R.drawable.ic_moon
    }
}

@Composable
fun LoadingIndicator(adaptiveTheme: AdaptiveTheme, context: Context) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = adaptiveTheme.textColor,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = context.getString(R.string.loading_weather_data),
            color = adaptiveTheme.textColor,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ErrorMessage(message: String, adaptiveTheme: AdaptiveTheme) {
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
            color = adaptiveTheme.textColor,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun LandscapeLayout(
    weatherResult: NetworkResponse<WeatherModel>?,
    forecastResult: NetworkResponse<ForecastResponse>?,
    viewModel: WeatherViewModel,
    adaptiveTheme: AdaptiveTheme,
    context: Context,
    onRefreshClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        // BAGIAN KIRI - 50%
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            LeftLandscapeContent(
                modifier = Modifier
                    .fillMaxSize(),
                weatherResult = weatherResult,
                forecastResult = forecastResult,
                viewModel = viewModel,
                adaptiveTheme = adaptiveTheme,
                context = context,
                onRefreshClick = onRefreshClick,
                onSearchClick = onSearchClick
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // BAGIAN KANAN - 50%
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            RightLandscapeContent(
                modifier = Modifier
                    .fillMaxSize(),
                weatherResult = weatherResult,
                forecastResult = forecastResult,
                adaptiveTheme = adaptiveTheme,
                context = context
            )
        }
    }
}

@Composable
fun PortraitLayout(
    weatherResult: NetworkResponse<WeatherModel>?,
    forecastResult: NetworkResponse<ForecastResponse>?,
    viewModel: WeatherViewModel,
    adaptiveTheme: AdaptiveTheme,
    context: Context,
    onRefreshClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(Color.Transparent)
        )

        StickyHeader(
            weatherResult = weatherResult,
            onRefreshClick = onRefreshClick,
            onSearchClick = onSearchClick,
            adaptiveTheme = adaptiveTheme,
            context = context
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val result = weatherResult) {
                is NetworkResponse.Error -> {
                    ErrorMessage(
                        message = result.message ?: context.getString(R.string.unknown_error),
                        adaptiveTheme = adaptiveTheme
                    )
                }
                is NetworkResponse.Loading -> {
                    LoadingIndicator(adaptiveTheme = adaptiveTheme, context = context)
                }
                is NetworkResponse.Success -> {
                    WeatherDetails(
                        data = result.data,
                        forecastData = forecastResult,
                        adaptiveTheme = adaptiveTheme,
                        context = context
                    )
                }
                null -> {
                    WelcomeMessage(adaptiveTheme = adaptiveTheme, context = context)
                }
            }
        }
    }
}

@Composable
fun WelcomeMessage(adaptiveTheme: AdaptiveTheme, context: Context) {
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
            text = context.getString(R.string.app_name),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = adaptiveTheme.textColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = context.getString(R.string.search_city_for_weather),
            fontSize = 16.sp,
            color = adaptiveTheme.secondaryTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}