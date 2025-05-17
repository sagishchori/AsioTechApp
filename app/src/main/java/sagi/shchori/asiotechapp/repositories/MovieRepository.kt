package sagi.shchori.asiotechapp.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import sagi.shchori.asiotechapp.Logger
import sagi.shchori.asiotechapp.database.MovieDao
import sagi.shchori.asiotechapp.network.MovieResponse
import sagi.shchori.asiotechapp.network.NetworkModule
import sagi.shchori.asiotechapp.network.NetworkResult
import sagi.shchori.asiotechapp.network.OmdbApi
import sagi.shchori.asiotechapp.ui.models.Movie
import sagi.shchori.asiotechapp.ui.models.MovieDetails
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val api: OmdbApi,
    private val movieDao: MovieDao
) {

    /**
     * Search for a movie according to user input and return the result when ready
     *
     * @param query     The input of the user to search for
     */
    fun searchMovies(query: String): Flow<NetworkResult<MovieResponse>> = flow {
        Logger.d("searchMovies(): query = $query")

        emit(NetworkResult.Loading)

        Logger.v("searchMovies(): Emitting Loading")

        val favoriteSet = hashSetOf<String>()

        Logger.d("searchMovies(): Attempting to fetch movies from DB for query: '$query'")

        // First, check in the DB for the search word
        val dbResult = movieDao.searchMovies(query)
        if (dbResult.isNotEmpty()) {

            // Add the favorite movies to Set to check later
            dbResult.forEach {
                if (it.isFavorite) {
                    favoriteSet.add(it.imdbID)
                }
            }

            // The DB is filled with movies save for the search word
            emit(NetworkResult.Success(MovieResponse(dbResult, "", "")))

            Logger.i("searchMovies(): Emitting Success with ${dbResult.size} movies from DB")
        }

        Logger.d("searchMovies(): Attempting to fetch movies from network API for query: '$query'")

        // continue to search on the web
        val response = try {
            api.searchMovies(NetworkModule.apiKey, query)
        } catch (ex: Exception) {
            Logger.e("searchMovies(): Exception for query: '$query': $ex")

            if (dbResult.isNotEmpty()) {

                Logger.i("searchMovies(): Network call failed but DB had results.")

                // The network call failed but the DB returned a list
                return@flow
            } else {
                Logger.e("searchMovies(): Network call failed and DB had no results.")

                // Both the network and DB calls failed to fetch any data -> return a network error
                emit(NetworkResult.Error(Exception(ex.message)))
            }

            null
        }

        if (response?.isSuccessful == true) {
            Logger.i("searchMovies(): Network call was successful for query: '$query', Code: ${response.code()}")

            response.body()?.let {
                Logger.d("searchMovies(): Network response body: TotalResults: ${it.totalResults}, Items: ${it.search?.size ?: 0}")

                it.search?.let {    list ->

                    // If the lists are the same size no change in data -> return
                    if (list.size == dbResult.size && dbResult.isNotEmpty()) {
                        Logger.i("searchMovies(): No change in data. Returning flow")
                        return@flow
                    }

                    list.forEach {  movie ->

                        // Set for each item the query in order to fetch them later in case of no
                        // network
                        movie.searchWord = query

                        // Retain favorites
                        if (favoriteSet.contains(movie.imdbID)) {
                            movie.isFavorite = true

                            Logger.v("searchMovies(): Setting movie: ${movie.title} as favorite")
                        }
                    }

                    Logger.d("searchMovies(): Saving ${list.size} movies to DB for query: '$query'")

                    // If the result is successful need to save the result to DB
                    movieDao.insertMovies(it.search)

                    Logger.d("searchMovies(): Saved ${list.size} movies to DB for query: '$query'")
                }

                // Emit and update the ui accordingly
                emit(NetworkResult.Success(it))

                Logger.i("searchMovies(): Emitting Success with ${it.search?.size} movies from network")
            }
        } else {
            response?.errorBody()?.let {
                Logger.e("searchMovies(): Network call failed for query: '$query', Code: ${response.code()}, Error: ${it.string()}")

                emit(NetworkResult.Error(Exception(it.string())))
            }
        }
    }

    /**
     * Search for more details about a specific movie
     *
     * @param movie     The movie id to search for more details
     */
    fun movieDetails(movie: Movie): Flow<NetworkResult<MovieDetails>> = flow {
        Logger.d("movieDetails(): movie = $movie")

        emit(NetworkResult.Loading)

        Logger.d("movieDetails(): Attempting to fetch movie details from DB for movie: ${movie.title}, ID: ${movie.imdbID}")

        // First, check in the DB for the movie according to movie id
        val dbResult = movieDao.searchMovie(movie.imdbID)
        val movieFromDB: Movie? = if (dbResult.isEmpty()) {
            null
        } else {
            dbResult.first()
        }

        movieFromDB?.movieDetails?.let {
            Logger.i("movieDetails(): Emitting Success with movie details found in DB for movie: ${movie.title}, ID: ${movie.imdbID}, Title: ${it.title}")

            emit(NetworkResult.Success(it))
        }

        Logger.d("movieDetails(): Attempting to fetch movie details from network API for movie: ${movie.title}, ID: ${movie.imdbID}")

        val response = try {
            api.getMovieDetails("6b673588", movie.imdbID)
        } catch (ex: Exception) {
            Logger.e("movieDetails(): Exception for movie: ${movie.title}, ID: ${movie.imdbID}: $ex")

            if (dbResult.isNotEmpty() && movieFromDB?.movieDetails != null) {

                // In case the network call failed but there is a result from DB and its movie
                // details object is not null since this is the data to show
                return@flow
            } else if (dbResult.isNotEmpty() && movieFromDB?.movieDetails == null) {

                // In case the network call failed but there is a result from DB. The DB returned a
                // movie but with no movie details i.e. first time loading the movie details -> send
                // an Error result
                emit(NetworkResult.Error(Exception("Trying to fetch data for ${movieFromDB?.title} failed.")))
            } else {
                Logger.e("movieDetails(): Network call failed and DB had no results. Exception: $ex")

                // When both network and DB calls failed -> emit the exception and return null as
                // response
                emit(NetworkResult.Error(ex))
            }

            null
        }

        if (response?.isSuccessful == true) {
            response.body()?.let {

                Logger.d("movieDetails(): Successfuly fetched movie details for movie: ${movie.title}, ID: ${movie.imdbID}")

                // Update the movie details from the web
                movieFromDB?.movieDetails = it

                // If the result is successful save the movie to DB
                if (movieFromDB != null) {
                    Logger.d("movieDetails(): Saving movie details to DB for movie: ${movie.title}, ID: ${movie.imdbID}")

                    movieDao.insertMovie(movieFromDB)
                }

                // Emit and update the ui accordingly
                emit(NetworkResult.Success(it))
            }
        } else {
            response?.errorBody()?.let {
                emit(NetworkResult.Error(Exception(it.string())))
            }
        }
    }

    /**
     * Set a favorite movie into DB.
     */
    suspend fun setMovieAsFavorite(movieId: String, isFavorite: Boolean) {
        Logger.d("setMovieAsFavorite(): movieId = $movieId, isFavorite = $isFavorite")

        try {
            val dbResult = movieDao.searchMovie(movieId)
            val movieFromDB = dbResult[0]

            Logger.d("setMovieAsFavorite(): Found movie in DB: ${movieFromDB.title} as current favorite: ${movieFromDB.isFavorite}")

            movieFromDB.isFavorite = isFavorite

            Logger.d("setMovieAsFavorite(): Attempting to update movie in DB: ${movieFromDB.title} and ID: ${movieFromDB.imdbID}")

            movieDao.insertMovie(movieFromDB)

            Logger.d("setMovieAsFavorite(): Updated movie in DB: ${movieFromDB.title} and ID: ${movieFromDB.imdbID}")
        } catch (ex: Exception) {
            Logger.e("setMovieAsFavorite(): Error setting movie as favorite. Exception: $ex")
        }
    }

    fun getFavoriteMovies(): Flow<List<Movie>> {
        return movieDao.getFavoriteMovies()
    }

    // Optional: If you need to toggle favorite status from the favorites screen itself
    suspend fun updateFavoriteStatus(movieId: String, isFavorite: Boolean) {
        try {
            val dbResult = movieDao.searchMovie(movieId)
            val movieFromDB = dbResult[0]

            Logger.d("setMovieAsFavorite(): Found movie in DB: ${movieFromDB.title} as current favorite: ${movieFromDB.isFavorite}")

            movieFromDB.isFavorite = isFavorite

            Logger.d("setMovieAsFavorite(): Attempting to update movie in DB: ${movieFromDB.title} and ID: ${movieFromDB.imdbID}")

            movieDao.insertMovie(movieFromDB)

            Logger.d("setMovieAsFavorite(): Updated movie in DB: ${movieFromDB.title} and ID: ${movieFromDB.imdbID}")
        } catch (ex: Exception) {
            Logger.e("setMovieAsFavorite(): Error setting movie as favorite. Exception: $ex")
        }

//        movieDao.updateFavoriteStatus(movieId, isFavorite)
    }
}