package sagi.shchori.asiotechapp.ui.fragments.moviedetailsview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import sagi.shchori.asiotechapp.Logger
import sagi.shchori.asiotechapp.R
import sagi.shchori.asiotechapp.databinding.FragmentMovieDetailsBinding
import sagi.shchori.asiotechapp.ui.models.Movie
import sagi.shchori.asiotechapp.ui.viewmodels.FavoritesViewModel
import sagi.shchori.asiotechapp.ui.viewmodels.MovieViewModel

enum class DetailSource {
    MOVIE_LIST,
    FAVORITES
}

@AndroidEntryPoint
class MovieDetailsFragment : Fragment() {

    private val movieViewModel: MovieViewModel by activityViewModels()
    private val favoritesViewModel: FavoritesViewModel by activityViewModels()

    private lateinit var activeDetailViewModel: ViewModel
    private lateinit var source: DetailSource

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    // If using newInstance pattern:
    companion object {
        private const val ARG_SOURCE = "source_type"

        fun newInstance(source: DetailSource): MovieDetailsFragment {
            return MovieDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SOURCE, source.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val sourceName = it.getString(ARG_SOURCE) ?: DetailSource.MOVIE_LIST.name
            source = DetailSource.valueOf(sourceName)
        }

        activeDetailViewModel = when (source) {
            DetailSource.MOVIE_LIST -> movieViewModel
            DetailSource.FAVORITES -> favoritesViewModel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isFavoriteOn = false

        when(source) {
            DetailSource.MOVIE_LIST -> {
                (activeDetailViewModel as MovieViewModel).selectedMovie.observe(viewLifecycleOwner) { movie ->
                    Logger.d("selectedMovie.observe: Observed selected movie - ID: ${movie?.imdbID}, Title: '${movie?.title}', IsFavorite: ${movie?.isFavorite}")

                    updateUi(movie)
                }
            }

            DetailSource.FAVORITES -> {
                (activeDetailViewModel as FavoritesViewModel).selectedMovie.observe(viewLifecycleOwner) { movie ->
                    Logger.d("selectedMovie.observe: Observed selected movie - ID: ${movie?.imdbID}, Title: '${movie?.title}', IsFavorite: ${movie?.isFavorite}")

                    updateUi(movie)
                }
            }
        }

        binding.favorite.setOnClickListener {
            Logger.d("favorite.setOnClickListener: Favorite clicked")
            when(source) {
                DetailSource.MOVIE_LIST -> {
                    (activeDetailViewModel as MovieViewModel).addOrRemoveMovieAsFavorite()
                    setFavoriteImageResource((activeDetailViewModel as MovieViewModel).selectedMovie.value?.isFavorite)
                }

                DetailSource.FAVORITES -> {
                    (activeDetailViewModel as FavoritesViewModel).toggleFavoriteStatus(null)
                    setFavoriteImageResource((activeDetailViewModel as FavoritesViewModel).selectedMovie.value?.isFavorite)
                }
            }
        }
    }

    private fun updateUi(movie: Movie?) {
        binding.movieTitle.text = movie?.title
        binding.movieId.text = movie?.imdbID
        binding.movieYear.text = movie?.year
        binding.moviePlot.text = movie?.movieDetails?.plot

        Glide.with(requireContext())
            .load(movie?.poster)
            .placeholder(R.drawable.ic_image_not_supported_100)
            .into(binding.moviePoster)

        val isFavoriteOn = movie?.isFavorite ?: false

        setFavoriteImageResource(isFavoriteOn)
    }

    private fun setFavoriteImageResource(favorite: Boolean?) {
        if (favorite == true) {
            binding.favorite.setImageResource(R.drawable.ic_favorite_24)
        } else {
            binding.favorite.setImageResource(R.drawable.ic_favorite_border_24)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}