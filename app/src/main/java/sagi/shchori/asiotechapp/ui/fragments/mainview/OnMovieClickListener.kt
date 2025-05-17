package sagi.shchori.asiotechapp.ui.fragments.mainview

import sagi.shchori.asiotechapp.ui.models.Movie

interface OnMovieClickListener {

    fun onMovieClicked(movie: Movie, position: Int)

    fun onFavoriteClicked(movieId: String, isFavorite: Boolean)
}