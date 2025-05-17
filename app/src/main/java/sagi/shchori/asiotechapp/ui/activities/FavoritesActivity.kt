package sagi.shchori.asiotechapp.ui.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import sagi.shchori.asiotechapp.ui.fragments.moviedetailsview.MovieDetailsFragment
import sagi.shchori.asiotechapp.R
import sagi.shchori.asiotechapp.databinding.ActivityFavoritesBinding
import sagi.shchori.asiotechapp.ui.fragments.favorites.FavoritesFragment
import sagi.shchori.asiotechapp.ui.fragments.moviedetailsview.DetailSource
import sagi.shchori.asiotechapp.ui.viewmodels.FavoritesViewModel

@AndroidEntryPoint
class FavoritesActivity : AppCompatActivity() {

    private val viewModel: FavoritesViewModel by viewModels()

    private lateinit var binding: ActivityFavoritesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {

                // In case of a screen rotation the fragment should be in the backstack so no need to
                // set it again to the View
                supportFragmentManager.findFragmentByTag("FavoritesFragment")?.let {
                    return@commit
                }

                setReorderingAllowed(true)

                replace(
                    R.id.favorites_fragment_container,
                    FavoritesFragment::class.java,
                    null,
                    "FavoritesFragment"
                )

                addToBackStack("FavoritesFragment")
            }
        }

        supportActionBar?.title = getString(R.string.favorites_title)

         supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
                    R.id.favorites_fragment_container,
                    MovieDetailsFragment.newInstance(DetailSource.FAVORITES),
                    "DetailsFragment"
                )
                addToBackStack("DetailsFragment")
            }
        }

        registerForBackPressToFinishActivity()
    }

    private fun registerForBackPressToFinishActivity() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (supportFragmentManager.backStackEntryCount > 1) {
                        supportFragmentManager.popBackStack()
                    } else {
                        finish()
                    }
                }
            })
    }

     override fun onSupportNavigateUp(): Boolean {
         onBackPressedDispatcher.onBackPressed()
         return true
     }
}
