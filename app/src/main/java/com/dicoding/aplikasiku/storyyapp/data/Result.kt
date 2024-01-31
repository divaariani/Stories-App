package com.dicoding.aplikasiku.storyyapp.data

sealed class Result<out R> private constructor(){
    data class Success<out T>(val data: T) : com.dicoding.aplikasiku.storyyapp.data.Result<T>()
    data class Error(val error: String) : com.dicoding.aplikasiku.storyyapp.data.Result<Nothing>()
    object Loading : com.dicoding.aplikasiku.storyyapp.data.Result<Nothing>()
}