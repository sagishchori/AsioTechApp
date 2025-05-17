package sagi.shchori.asiotechapp.ui.fragments.mainview

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.LinearLayoutManager
import sagi.shchori.asiotechapp.Logger
import sagi.shchori.asiotechapp.R
import sagi.shchori.asiotechapp.ui.viewmodels.MovieViewModel
import sagi.shchori.asiotechapp.databinding.FragmentMainBinding
import sagi.shchori.asiotechapp.extensions.smoothScrollItemToMiddle
import sagi.shchori.asiotechapp.ui.activities.FavoritesActivity
import sagi.shchori.asiotechapp.ui.models.Movie
import javax.inject.Inject


@AndroidEntryPoint
class MainFragment : Fragment(), OnMovieClickListener {

    private val viewModel: MovieViewModel by activityViewModels()

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var adapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.favorite -> {
                        val intent = Intent(activity, FavoritesActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }

        })

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                Logger.d("searchInput.onTextChanged: Text changed to '$searchText'")

                viewModel.searchMovies(searchText)
            }
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.searchInput.error = null
                }
            }
        })

        viewModel.movies.observe(viewLifecycleOwner) { movies ->

            Logger.d("movies.observe: movies = $movies")

            // Update the list anyway. When the clears his text field the list should be updated
            // even with no results to clear the previous search results
            adapter.updateMovies(movies)

            // In case of no results -> show error to text field
            if (movies == null && binding.searchInput.text.isNotEmpty()) {
                binding.searchInput.error = "No results yet"
            } else {

                // Remove the error from text field
                binding.searchInput.error = null
            }
        }

        viewModel.selectedPosition.observe(viewLifecycleOwner) {

            // Scroll the selected item to middle of screen
            binding.recyclerView.smoothScrollItemToMiddle(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMovieClicked(movie: Movie, position: Int) {

        // First, set the selected item to scroll the RecyclerView to center
        viewModel.setSelectedPosition(position)

        // Then, set selection to movie
        viewModel.selectMovie(movie)

        Logger.d("onMovieClicked(): movie = $movie in position $position")
    }

    override fun onFavoriteClicked(movieId: String, isFavorite: Boolean) {
        viewModel.setMovieAsFavorite(movieId, isFavorite)
    }
}