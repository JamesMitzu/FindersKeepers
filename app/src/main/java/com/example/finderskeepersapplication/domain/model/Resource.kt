package com.example.finderskeepersapplication.domain.model

/** Represents the three common states of an asynchronous operation. */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}
