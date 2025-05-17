package sagi.shchori.asiotechapp.network

/**
 * A class to represent the result of an IO call either to a network or to a DataBase e.g. Room
 */
sealed class NetworkResult<out T> {

    data class Success<out T>(val data: T) : NetworkResult<T>()

    data class Error(val exception: Exception) : NetworkResult<Nothing>()

    data object Loading : NetworkResult<Nothing>()
}