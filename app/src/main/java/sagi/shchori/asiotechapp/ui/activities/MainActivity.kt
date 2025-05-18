package sagi.shchori.asiotechapp.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import sagi.shchori.asiotechapp.BaseActivity
import sagi.shchori.asiotechapp.R
import sagi.shchori.asiotechapp.databinding.ActivityMainBinding
import sagi.shchori.asiotechapp.ui.UiState
import sagi.shchori.asiotechapp.ui.fragments.mainview.MainFragment
import sagi.shchori.asiotechapp.ui.fragments.moviedetailsview.DetailSource
import sagi.shchori.asiotechapp.ui.fragments.moviedetailsview.MovieDetailsFragment
import sagi.shchori.asiotechapp.ui.viewmodels.MovieViewModel

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MovieViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {

            // In case of a screen rotation the fragment should be in the backstack so no need to
            // set it again to the View
            supportFragmentManager.findFragmentByTag("MainFragment")?.let {
                return@commit
            }

            setReorderingAllowed(true)
            this.replace(R.id.container, MainFragment::class.java, null,"MainFragment")
            addToBackStack("MainFragment")
        }

        viewModel.selectedMovie.observe(this) {

            supportFragmentManager.commit {

                // In case of a screen rotation the fragment should be in the backstack so no need to
                // set it again to the View
                supportFragmentManager.findFragmentByTag("DetailsFragment")?.let {
                    return@commit
                }

                // This is in the case of resetting the selected movie
                if (it == null) {
                    return@commit
                }

                add(
                    R.id.container,
                    MovieDetailsFragment.newInstance(DetailSource.MOVIE_LIST),
                    "DetailsFragment"
                )

                addToBackStack("DetailsFragment")
            }
        }

        viewModel.uiState.observe(this) {   state ->
            when(state) {
                is UiState.ERROR -> {
                    binding.progressbarContainer.visibility = View.GONE

                    showDialog("Error", state.error.toString())
                }

                UiState.IDLE -> {
                    binding.progressbarContainer.visibility = View.GONE
                }

                UiState.LOADING -> {
                    binding.progressbarContainer.visibility = View.VISIBLE
                }
            }
        }

        registerForBackPressToFinishActivity()
    }

    override fun registerForBackPressToFinishActivity() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (supportFragmentManager.backStackEntryCount > 1) {
                        supportFragmentManager.popBackStack()

                        // This will reset the selection each time the user goes out to the list
                        viewModel.selectMovie(null)
                    } else {
                        finish()
                    }
                }
            })
    }
}