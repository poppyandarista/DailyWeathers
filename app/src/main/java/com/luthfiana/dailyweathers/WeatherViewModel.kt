package com.luthfiana.dailyweathers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luthfiana.dailyweathers.api.Constant
import com.luthfiana.dailyweathers.api.ForecastResponse
import com.luthfiana.dailyweathers.api.NetworkResponse
import com.luthfiana.dailyweathers.api.RetrofitInstance
import com.luthfiana.dailyweathers.api.WeatherModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val weatherApi = RetrofitInstance.weatherApi

    private val _weatherResult = MutableStateFlow<NetworkResponse<WeatherModel>?>(null)
    val weatherResult: StateFlow<NetworkResponse<WeatherModel>?> = _weatherResult.asStateFlow()

    private val _forecastResult = MutableStateFlow<NetworkResponse<ForecastResponse>?>(null)
    val forecastResult: StateFlow<NetworkResponse<ForecastResponse>?> = _forecastResult.asStateFlow()

    fun getData(city: String) {
        if (city.isBlank()) {
            _weatherResult.value = NetworkResponse.Error("Please enter a city name")
            return
        }

        _weatherResult.value = NetworkResponse.Loading
        _forecastResult.value = NetworkResponse.Loading

        viewModelScope.launch {
            try {
                // Get current weather
                val weatherResponse = weatherApi.getWeather(city, Constant.apiKey)
                if (weatherResponse.isSuccessful) {
                    val weatherData = weatherResponse.body()
                    if (weatherData != null) {
                        _weatherResult.value = NetworkResponse.Success(weatherData)
                        // Get forecast data
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

    private suspend fun getForecastData(city: String) {
        try {
            val forecastResponse = weatherApi.getForecast(city, Constant.apiKey, count = 40)
            if (forecastResponse.isSuccessful) {
                val forecastData = forecastResponse.body()
                if (forecastData != null) {
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

    private fun handleErrorResponse(code: Int) {
        when (code) {
            401 -> _weatherResult.value = NetworkResponse.Error("Invalid API key")
            404 -> _weatherResult.value = NetworkResponse.Error("City not found")
            429 -> _weatherResult.value = NetworkResponse.Error("API rate limit exceeded")
            else -> _weatherResult.value = NetworkResponse.Error("Error: $code")
        }
    }
}