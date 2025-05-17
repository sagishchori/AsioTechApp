package sagi.shchori.asiotechapp.network

import com.google.gson.annotations.SerializedName
import sagi.shchori.asiotechapp.ui.models.Movie

data class MovieResponse(
    @SerializedName("Search") val search: List<Movie>,
    val totalResults: String,
    @SerializedName("Response") val response: String
)