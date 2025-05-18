package sagi.shchori.asiotechapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import sagi.shchori.asiotechapp.Logger
import sagi.shchori.asiotechapp.network.NetworkResult
import sagi.shchori.asiotechapp.repositories.MovieRepository
import sagi.shchori.asiotechapp.ui.UiState
import sagi.shchori.asiotechapp.ui.models.Movie
import javax.inject.Inject


@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _selectedMovie = MutableLiveData<Movie?>()
    val selectedMovie: LiveData<Movie?> get() = _selectedMovie

    private val _uiState = MutableLiveData<UiState<*>>()
    val uiState: LiveData<UiState<*>> = _uiState

    private val _selectedPosition = MutableLiveData(-1)
    val selectedPosition: LiveData<Int> get() = _selectedPosition

    private val lastSearchQuery = MutableLiveData("")

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> get() = _movies

    private var searchJob: Job? = null

    /**
     * Search for a movie according to user input
     *
     * @param query     The user input
     */
    fun searchMovies(query: String) {
        Logger.d("searchMovies(): query = $query")

        lastSearchQuery.postValue(query)

        searchJob?.cancel()

        Logger.d("searchMovies(): searchJob?.cancel()")

        val errorHandler = CoroutineExceptionHandler { _, exception ->
            when(exception) {
                is Exception -> {
                    _uiState.postValue(UiState.ERROR(exception.message))
                }
            }
        }

        if (query.isBlank()) {
            Logger.d("searchMovies(): query.isBlank()")

            _movies.postValue(emptyList())

            _uiState.postValue(UiState.IDLE)

            return

        }

        searchJob = viewModelScope.launch(errorHandler) {

            delay(400) // This is for when a user continues to enter characters so the
            // search will be canceled for the previous search hence, no extra calls to the API for
            // no reason

            // In case there is a new search -> reset the selected movie
            _selectedPosition.postValue(-1)

            repository.searchMovies(query)
                .onStart {
                    _uiState.postValue(UiState.LOADING)

                    Logger.d("searchMovies Flow: UIState set to LOADING.")
                }
                .catch {
                    Logger.e("searchMovies Flow: CATCH block for query: '$query'")

                    _uiState.postValue(UiState.ERROR(it.message ?: "Error during movie search"))
                }
                .collect {    result ->
                    when(result) {
                        is NetworkResult.Error -> {
                            _uiState.postValue(UiState.ERROR(result.exception.message))
                        }

                        is NetworkResult.Loading -> {
                            _uiState.postValue(UiState.LOADING)
                        }

                        is NetworkResult.Success -> {
                            _uiState.postValue(UiState.IDLE)

                            result.data.search?.let { searchList ->
                                if (_selectedMovie.value != null) {
                                    searchList.forEachIndexed { index, item ->
                                        if (item.imdbID == _selectedMovie.value!!.imdbID) {
                                            item.isSelected = true
                                            _selectedPosition.postValue(index)
                                        }
                                    }
                                } else {
                                    if (_selectedPosition.value != null && _selectedPosition.value > -1) {
                                        searchList[_selectedPosition.value].isSelected = true
                                    }
                                }
                            }

                            _movies.postValue(result.data.search)
                        }
                    }
                }
        }
    }

    /**
     * Search for a specific movie details
     *
     * @param movie   The movie received from the user's clicked item on the [MainFragment]
     */
    private fun getMovieDetails(movie: Movie) {
        Logger.d("getMovieDetails: Fetching details for movie ID: ${movie.imdbID}, Title: ${movie.title}")

        val errorHandler = CoroutineExceptionHandler { _, exception ->
            when(exception) {
                is Exception -> {
                    _uiState.postValue(UiState.ERROR(exception.message))
                }
            }
        }

        viewModelScope.launch(errorHandler) {

            // Get more details by using the movie id to get more data for a specific movie
            repository.movieDetails(movie)
                .onStart {
                    _uiState.postValue(UiState.LOADING)

                    Logger.d("getMovieDetails Flow: UIState set to LOADING.")
                }
                .catch {
                    Logger.e("getMovieDetails Flow: CATCH block for movie ID: ${movie.imdbID}")

                    _uiState.postValue(UiState.ERROR(it.message ?: "Error during movie details fetch"))
                }
                .collect { result ->
                    when(result) {
                        is NetworkResult.Error -> {
                            _uiState.postValue(UiState.ERROR(result.exception.message))
                        }

                        NetworkResult.Loading -> {
                            _uiState.postValue(UiState.LOADING)
                        }

                        is NetworkResult.Success -> {
                            movie.movieDetails = result.data

                            _selectedMovie.postValue(movie)

                            Logger.d("getMovieDetails Flow: Updated _selectedMovie with new details for ID: ${result.data.imdbID}")

                            _uiState.postValue(UiState.IDLE)
                        }
                    }
                }
        }
    }

    fun selectMovie(movie: Movie?) {
        Logger.d("selectMovie: Called. Movie Title: ${movie?.title}, ID: ${movie?.imdbID}")

        movie?.let { mov ->
            viewModelScope.launch(Dispatchers.IO) {

                // It is not mandatory, it just allow the user to experience the snapping to center
                delay(1000)

                // This will ensure loading the movie data first and then transfer the user to
                // MovieDetailsFragment
                getMovieDetails(mov)
            }
        }

        // This resets the selected movie when a user go back to movies list
        _selectedMovie.value = null
    }

    /**
     * This function is being called from the main view since the user sets a movie as favorite from
     * the list. It also creates separation from movie details view.
     */
    fun setMovieAsFavorite(movieId: String, isFavorite: Boolean) {
        Logger.d("setMovieAsFavorite: Movie ID: $movieId, isFavorite: $isFavorite")

        viewModelScope.launch {
            repository.setMovieAsFavorite(movieId, isFavorite)
        }
    }

    /**
     * This function is being called from the movie details view because when a user enters this
     * view and click on favorite he adds it to its favorite list. It also creates a separation from
     * setting favorite from the main view.
     */
    fun addOrRemoveMovieAsFavorite() {
        Logger.d("addOrRemoveMovieAsFavorite: Called")

        viewModelScope.launch {

            _selectedMovie.value?.let { selectedMovie ->

                _movies.value?.forEach {    movieFromList ->

                    // Search for the selected movie in the movie list to change the isFavorite
                    // value
                    if (movieFromList.imdbID == selectedMovie.imdbID) {
                        movieFromList.isFavorite = !selectedMovie.isFavorite

                        _selectedMovie.value?.isFavorite = selectedMovie.isFavorite

                        _movies.postValue(_movies.value)

                        // Once it was found -> break
                        return@forEach
                    }
                }

                setMovieAsFavorite(selectedMovie.imdbID, selectedMovie.isFavorite)

                Logger.d("addOrRemoveMovieAsFavorite called to set movie as favorite: Movie ID: ${selectedMovie.imdbID}, isFavorite: ${!selectedMovie.isFavorite}")
            }
        }
    }

    /**
     * Set the RecyclerView's position of the selected item
     */
    fun setSelectedPosition(position: Int) {
        Logger.d("setSelectedPosition: position = $position")

        _selectedPosition.postValue(position)
    }
}