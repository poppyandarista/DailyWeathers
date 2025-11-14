package com.luthfiana.dailyweathers.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    // Endpoint OpenWeather untuk current weather
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // untuk mendapatkan suhu dalam Celsius
    ) : Response<WeatherModel>
}