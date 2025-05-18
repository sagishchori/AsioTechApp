package sagi.shchori.asiotechapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sagi.shchori.asiotechapp.Logger
import sagi.shchori.asiotechapp.repositories.MovieRepository
import sagi.shchori.asiotechapp.ui.models.Movie
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    val favoriteMovies: LiveData<List<Movie>> = repository.getFavoriteMovies().asLiveData()

    private var toggleJob: Job? = null

    private val _selectedMovie = MutableLiveData<Movie?>()
    val selectedMovie: LiveData<Movie?> = _selectedMovie

    fun toggleFavoriteStatus(movie: Movie?) {
        toggleJob?.cancel() // This will ensure that only one toggle is running at a time, it is
        // applicable when the user "UNDO" the favorite removal action

        toggleJob = viewModelScope.launch {
            val movieToUpdate = movie ?: (_selectedMovie.value ?: return@launch)

            val newFavoriteState = !movieToUpdate.isFavorite

            Logger.d("toggleFavoriteStatus: Movie ID ${movieToUpdate.imdbID}, new state: $newFavoriteState")

            _selectedMovie.value?.isFavorite = newFavoriteState

            repository.updateFavoriteStatus(movieToUpdate.imdbID, newFavoriteState)
        }
    }

    fun selectMovie(movie: Movie) {
        Logger.d("selectMovie: Movie ID ${movie.imdbID}")

        _selectedMovie.postValue(movie)
    }
}