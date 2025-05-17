package sagi.shchori.asiotechapp.ui.models

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "movies")
data class Movie(
    @ColumnInfo("title")
    @SerializedName("Title") val title: String,
    @ColumnInfo("year")
    @SerializedName("Year") val year: String,
    @PrimaryKey
    @ColumnInfo("imdbID")
    @SerializedName("imdbID") val imdbID: String,
    @ColumnInfo("type")
    @SerializedName("Type") val type: String,
    @ColumnInfo("poster")
    @SerializedName("Poster") val poster: String
) {

    @ColumnInfo("searchWord")
    var searchWord: String = ""

    @ColumnInfo("movieDetails")
    @Nullable
    var movieDetails: MovieDetails? = null

    @ColumnInfo("isFavorite")
    var isFavorite: Boolean = false

    var isSelected = false
}