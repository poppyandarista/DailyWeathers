package com.luthfiana.dailyweathers.api

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("list")
    val list: List<ForecastItem>,
    @SerializedName("city")
    val city: ForecastCity
)

data class ForecastItem(
    @SerializedName("dt")
    val dt: Long,
    @SerializedName("main")
    val main: Main,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("dt_txt")
    val dtTxt: String,
    @SerializedName("pop")
    val pop: Double? = 0.0, // Probability of precipitation (0-1)
    @SerializedName("rain")
    val rain: Rain? = null // Rain volume for last 3 hours
)


data class ForecastCity(
    @SerializedName("name")
    val name: String,
    @SerializedName("country")
    val country: String
)

data class WeatherInfoItem(
    val icon: String,
    val title: String,
    val description: String,
    val value: String
)
