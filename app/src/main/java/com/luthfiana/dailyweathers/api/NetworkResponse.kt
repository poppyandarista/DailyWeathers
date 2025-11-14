package com.luthfiana.dailyweathers.api

sealed class NetworkResponse<out T> {
    data class Success<out T>(val data: T) : NetworkResponse<T>()
    data class Error(val message: String? = null) : NetworkResponse<Nothing>() // Ubah jadi String
    object Loading : NetworkResponse<Nothing>()
}