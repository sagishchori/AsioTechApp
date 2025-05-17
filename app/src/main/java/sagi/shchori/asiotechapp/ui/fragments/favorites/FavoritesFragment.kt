package sagi.shchori.asiotechapp.ui.fragments.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import sagi.shchori.asiotechapp.Logger
import sagi.shchori.asiotechapp.databinding.FragmentFavoritesBinding
import sagi.shchori.asiotechapp.ui.models.Movie
import sagi.shchori.asiotechapp.ui.viewmodels.FavoritesViewModel

@AndroidEntryPoint
class FavoritesFragment : Fragment(), OnFavoriteMovieClickListener {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val favoritesViewModel: FavoritesViewModel by activityViewModels()
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter(emptyList(), this)

        binding.favoritesRecyclerView.apply {
            adapter = favoritesAdapter

            layoutManager = LinearLayoutManager(requireContext())

            setHasFixedSize(true)
        }

        Logger.d("FavoritesFragment.setupRecyclerView: RecyclerView setup complete.")
    }

    private fun observeViewModel() {
        favoritesViewModel.favoriteMovies.observe(viewLifecycleOwner) { movies ->
            favoritesAdapter.submitList(movies)

            if (movies.isEmpty()) {
                binding.favoritesRecyclerView.visibility = View.GONE
                binding.emptyFavoritesText.visibility = View.VISIBLE

                Logger.d("FavoritesFragment.favoriteMovies.observe: No favorites, showing empty state.")
            } else {
                binding.favoritesRecyclerView.visibility = View.VISIBLE
                binding.emptyFavoritesText.visibility = View.GONE

                Logger.d("FavoritesFragment.favoriteMovies.observe: Favorites found, showing list.")
            }
        }
    }

    override fun onFavoriteMovieClicked(movie: Movie) {
        Logger.d("FavoritesFragment.onFavoriteMovieClicked: Movie clicked: ${movie.title}, ID: ${movie.imdbID}")

        favoritesViewModel.selectMovie(movie)
    }

    override fun onToggleFavoriteClicked(movie: Movie) {
        Logger.d("FavoritesFragment.onToggleFavoriteClicked: Toggle favorite for: ${movie.title}, Current state: ${movie.isFavorite}")

        favoritesViewModel.toggleFavoriteStatus(movie)

        Snackbar
            .make(binding.root, "Removed '${movie.title}' from favorites", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                Logger.d("FavoritesFragment.UNDO_UN_FAVORITE: Undoing un-favorite for ${movie.title}")

                movie.isFavorite = !movie.isFavorite
                favoritesViewModel.toggleFavoriteStatus(movie) // This will re-favorite it
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.favoritesRecyclerView.adapter = null
        _binding = null
    }
}