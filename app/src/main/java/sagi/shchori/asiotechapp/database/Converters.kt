package sagi.shchori.asiotechapp.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import sagi.shchori.asiotechapp.Logger
import sagi.shchori.asiotechapp.ui.models.Movie
import sagi.shchori.asiotechapp.ui.models.MovieDetails
import sagi.shchori.asiotechapp.ui.models.RatingItem

class Converters {

    @TypeConverter
    fun movieClassToString(movie: Movie): String {
        return Gson().toJson(movie)
    }

    @TypeConverter
    fun movieClassFromString(json: String): Movie {
        return Gson().fromJson(json, Movie::class.java)
    }

    @TypeConverter
    fun movieDetailsClassToString(details: MovieDetails?): String {
        return Gson().toJson(details ?: "")
    }

    @TypeConverter
    fun movieDetailsClassFromString(json: String): MovieDetails? {
        if (json == "\"\"") { // Gson().toJson("") results in "\"\""
            Logger.v("movieDetailsClassFromString: JSON was empty string, returning null MovieDetails.")

            return null
        }

        return try {
            Gson().fromJson(json, MovieDetails::class.java)
        } catch (ex: Exception) {
            null
        }
    }

    @TypeConverter
    fun ratingsItemClassToString(ratingItem: RatingItem): String {
        return Gson().toJson(ratingItem)
    }

    @TypeConverter
    fun ratingItemClassFromString(json: String): RatingItem {
        try {
            return Gson().fromJson(json, RatingItem::class.java)
        } catch (ex: Exception) {
            Logger.e("ratingItemClassFromString: Error parsing RatingItem from JSON. JSON: $json, Exception: $ex")

            throw ex
        }
    }
}