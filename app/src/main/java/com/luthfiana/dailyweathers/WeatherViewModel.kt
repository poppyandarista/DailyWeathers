package com.luthfiana.dailyweathers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luthfiana.dailyweathers.api.Constant
import com.luthfiana.dailyweathers.api.NetworkResponse
import com.luthfiana.dailyweathers.api.WeatherModel
import com.luthfiana.dailyweathers.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val weatherApi = RetrofitInstance.weatherApi

    private val _weatherResult = MutableStateFlow<NetworkResponse<WeatherModel>?>(null)
    val weatherResult: StateFlow<NetworkResponse<WeatherModel>?> = _weatherResult.asStateFlow()

    fun getData(city: String) {
        if (city.isBlank()) {
            _weatherResult.value = NetworkResponse.Error("Please enter a city name")
            return
        }

        _weatherResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try {
                val response = weatherApi.getWeather(city, Constant.apiKey)
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    if (weatherData != null) {
                        _weatherResult.value = NetworkResponse.Success(weatherData)
                    } else {
                        _weatherResult.value = NetworkResponse.Error("No weather data found")
                    }
                } else {
                    when (response.code()) {
                        401 -> _weatherResult.value = NetworkResponse.Error("Invalid API key")
                        404 -> _weatherResult.value = NetworkResponse.Error("City not found")
                        429 -> _weatherResult.value = NetworkResponse.Error("API rate limit exceeded")
                        else -> _weatherResult.value = NetworkResponse.Error("Error: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                _weatherResult.value = NetworkResponse.Error("Network error: ${e.message}")
            }
        }
    }
}