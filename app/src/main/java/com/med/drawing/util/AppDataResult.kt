package com.med.drawing.util

sealed class AppDataResult<T>() {
    class Success<T> : AppDataResult<T>()
    class Error<T>(message: String) : AppDataResult<T>()
    class Loading<T>(val isLoading: Boolean = true) : AppDataResult<T>()
}