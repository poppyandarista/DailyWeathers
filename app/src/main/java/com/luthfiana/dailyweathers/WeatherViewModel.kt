package com.luthfiana.dailyweathers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.luthfiana.dailyweathers.api.Constant
import com.luthfiana.dailyweathers.api.ForecastResponse
import com.luthfiana.dailyweathers.api.NetworkResponse
import com.luthfiana.dailyweathers.api.RetrofitInstance
import com.luthfiana.dailyweathers.api.WeatherModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.Calendar

class WeatherViewModel : ViewModel() {

    private val weatherApi = RetrofitInstance.weatherApi
    private var fusedLocationClient: FusedLocationProviderClient? = null

    private val _weatherResult = MutableStateFlow<NetworkResponse<WeatherModel>?>(null)
    val weatherResult: StateFlow<NetworkResponse<WeatherModel>?> = _weatherResult.asStateFlow()

    private val _forecastResult = MutableStateFlow<NetworkResponse<ForecastResponse>?>(null)
    val forecastResult: StateFlow<NetworkResponse<ForecastResponse>?> = _forecastResult.asStateFlow()

    private val _showSearchBar = MutableStateFlow(false)
    val showSearchBar: StateFlow<Boolean> = _showSearchBar.asStateFlow()

    private val _currentLocation = MutableStateFlow<String?>(null)
    val currentLocation: StateFlow<String?> = _currentLocation.asStateFlow()

    fun toggleSearchBar() {
        _showSearchBar.value = !_showSearchBar.value
    }

    fun hideSearchBar() {
        _showSearchBar.value = false
    }

    // Fungsi untuk get data by city name
    fun getData(city: String) {
        if (city.isBlank()) {
            _weatherResult.value = NetworkResponse.Error("Please enter a city name")
            return
        }

        _weatherResult.value = NetworkResponse.Loading
        _forecastResult.value = NetworkResponse.Loading

        viewModelScope.launch {
            try {
                val weatherResponse = weatherApi.getWeather(city, Constant.apiKey)
                if (weatherResponse.isSuccessful) {
                    val weatherData = weatherResponse.body()
                    if (weatherData != null) {
                        _weatherResult.value = NetworkResponse.Success(weatherData)
                        _currentLocation.value = "${weatherData.cityName}, ${weatherData.sys.country}"
                        getForecastData(city)
                    } else {
                        _weatherResult.value = NetworkResponse.Error("No weather data found")
                    }
                } else {
                    handleErrorResponse(weatherResponse.code())
                }
            } catch (e: Exception) {
                _weatherResult.value = NetworkResponse.Error("Network error: ${e.message}")
            }
        }
    }

    // Fungsi untuk get data by coordinates
    fun getDataByLocation(lat: Double, lon: Double) {
        _weatherResult.value = NetworkResponse.Loading
        _forecastResult.value = NetworkResponse.Loading

        viewModelScope.launch {
            try {
                val weatherResponse = weatherApi.getWeatherByCoordinates(lat, lon, Constant.apiKey)
                if (weatherResponse.isSuccessful) {
                    val weatherData = weatherResponse.body()
                    if (weatherData != null) {
                        _weatherResult.value = NetworkResponse.Success(weatherData)
                        _currentLocation.value = "${weatherData.cityName}, ${weatherData.sys.country}"
                        getForecastDataByLocation(lat, lon)
                    } else {
                        _weatherResult.value = NetworkResponse.Error("No weather data found")
                    }
                } else {
                    handleErrorResponse(weatherResponse.code())
                }
            } catch (e: Exception) {
                _weatherResult.value = NetworkResponse.Error("Network error: ${e.message}")
            }
        }
    }

    // Fungsi untuk request lokasi dengan Fused Location Provider
    fun requestCurrentLocation(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Cek permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Jika permission tidak diberikan, fallback ke Klaten
            getData("Klaten")
            return
        }

        viewModelScope.launch {
            try {
                // Coba dapatkan last known location terlebih dahulu
                val lastLocation = getLastKnownLocation(context)
                if (lastLocation != null) {
                    getDataByLocation(lastLocation.latitude, lastLocation.longitude)
                } else {
                    // Jika last location tidak ada, request location update
                    requestLocationUpdate(context)
                }
            } catch (e: Exception) {
                // Jika error, fallback ke Klaten
                getData("Klaten")
            }
        }
    }

    // Fungsi untuk request location update
    private suspend fun requestLocationUpdate(context: Context) {
        return suspendCancellableCoroutine { continuation ->
            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        getDataByLocation(location.latitude, location.longitude)
                        fusedLocationClient?.removeLocationUpdates(this)
                        continuation.resume(Unit)
                    }
                }
            }

            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                10000
            ).build()

            try {
                fusedLocationClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )?.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }

            // Timeout setelah 15 detik
            continuation.invokeOnCancellation {
                fusedLocationClient?.removeLocationUpdates(locationCallback)
            }
        }
    }

    // Fungsi untuk mendapatkan lokasi terakhir yang diketahui
    private suspend fun getLastKnownLocation(context: Context): Location? {
        return suspendCancellableCoroutine { continuation ->
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                    continuation.resume(location)
                }?.addOnFailureListener { exception ->
                    continuation.resume(null)
                }
            } else {
                continuation.resume(null)
            }
        }
    }

    private suspend fun getForecastData(city: String) {
        try {
            // Ambil 40 data untuk 5 hari ke depan (bukan hanya 24 jam)
            val forecastResponse = weatherApi.getForecast(city, Constant.apiKey, count = 40)
            if (forecastResponse.isSuccessful) {
                val forecastData = forecastResponse.body()
                if (forecastData != null) {
                    // KIRIM SEMUA DATA TANPA FILTER - biar WeatherPage yang handle
                    _forecastResult.value = NetworkResponse.Success(forecastData)
                } else {
                    _forecastResult.value = NetworkResponse.Error("No forecast data found")
                }
            } else {
                _forecastResult.value = NetworkResponse.Error("Forecast data unavailable")
            }
        } catch (e: Exception) {
            _forecastResult.value = NetworkResponse.Error("Forecast error: ${e.message}")
        }
    }

    private suspend fun getForecastDataByLocation(lat: Double, lon: Double) {
        try {
            val forecastResponse = weatherApi.getForecastByCoordinates(lat, lon, Constant.apiKey, count = 40)
            if (forecastResponse.isSuccessful) {
                val forecastData = forecastResponse.body()
                if (forecastData != null) {
                    // KIRIM SEMUA DATA TANPA FILTER - biar WeatherPage yang handle
                    _forecastResult.value = NetworkResponse.Success(forecastData)
                } else {
                    _forecastResult.value = NetworkResponse.Error("No forecast data found")
                }
            } else {
                _forecastResult.value = NetworkResponse.Error("Forecast data unavailable")
            }
        } catch (e: Exception) {
            _forecastResult.value = NetworkResponse.Error("Forecast error: ${e.message}")
        }
    }

    // Tambahkan di class WeatherViewModel
    fun refreshData(context: Context) {
        _weatherResult.value = NetworkResponse.Loading
        _forecastResult.value = NetworkResponse.Loading
        requestCurrentLocation(context)
    }

    private fun handleErrorResponse(code: Int) {
        when (code) {
            401 -> _weatherResult.value = NetworkResponse.Error("Invalid API key")
            404 -> _weatherResult.value = NetworkResponse.Error("City not found")
            429 -> _weatherResult.value = NetworkResponse.Error("API rate limit exceeded")
            else -> _weatherResult.value = NetworkResponse.Error("Error: $code")
        }
    }
}