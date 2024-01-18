package com.ardrawing.sketchtrace.util

sealed class Resource<T>() {
    class Success<T> : Resource<T>()
    class Error<T>(message: String) : Resource<T>()
    class Loading<T>(val isLoading: Boolean = true) : Resource<T>()
}