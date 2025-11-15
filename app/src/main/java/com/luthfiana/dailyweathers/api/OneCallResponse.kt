package com.luthfiana.dailyweathers.api

import com.google.gson.annotations.SerializedName

data class OneCallResponse(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("timezone_offset")
    val timezoneOffset: Int,
    @SerializedName("hourly")
    val hourly: List<HourlyForecast>,
    @SerializedName("daily")
    val daily: List<DailyForecast>
)

data class HourlyForecast(
    @SerializedName("dt")
    val dt: Long,
    @SerializedName("temp")
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("pressure")
    val pressure: Int,
    @SerializedName("humidity")
    val humidity: Int,
    @SerializedName("dew_point")
    val dewPoint: Double,
    @SerializedName("uvi")
    val uvi: Double,
    @SerializedName("clouds")
    val clouds: Int,
    @SerializedName("visibility")
    val visibility: Int,
    @SerializedName("wind_speed")
    val windSpeed: Double,
    @SerializedName("wind_deg")
    val windDeg: Int,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("pop")
    val pop: Double, // Probability of precipitation (0-1)
    @SerializedName("rain")
    val rain: Rain? = null
)

data class DailyForecast(
    @SerializedName("dt")
    val dt: Long,
    @SerializedName("temp")
    val temp: Temperature,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("pop")
    val pop: Double
)

data class Temperature(
    @SerializedName("day")
    val day: Double,
    @SerializedName("min")
    val min: Double,
    @SerializedName("max")
    val max: Double,
    @SerializedName("night")
    val night: Double,
    @SerializedName("eve")
    val eve: Double,
    @SerializedName("morn")
    val morn: Double
)

data class Rain(
    @SerializedName("1h")
    val oneHour: Double? = 0.0
)