package sagi.shchori.asiotechapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import kotlinx.coroutines.flow.Flow
import sagi.shchori.asiotechapp.ui.models.Movie

@Dao
@RewriteQueriesToDropUnusedColumns
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(moviesList: List<Movie>)

    @Query("SELECT * FROM movies WHERE imdbID = :movieId")
    suspend fun searchMovie(movieId: String): List<Movie>

    @Query("SELECT * FROM movies WHERE searchWord = :query")
    suspend fun searchMovies(query: String): List<Movie>

    @Query("SELECT * FROM movies WHERE isFavorite = 1")
    fun getFavoriteMovies(): Flow<List<Movie>>

    @Query("UPDATE movies SET isFavorite = :favorite WHERE imdbID = :movieId")
    suspend fun updateFavoriteStatus(movieId: String, favorite: Boolean)
}