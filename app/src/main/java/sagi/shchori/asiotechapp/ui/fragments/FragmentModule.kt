package sagi.shchori.asiotechapp

import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext
import sagi.shchori.asiotechapp.ui.fragments.mainview.OnMovieClickListener

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    fun provideOnPurchaseItemButtonClickListener(
        @ActivityContext context: android.content.Context
    ): OnMovieClickListener {
        val fragment = (context as AppCompatActivity).supportFragmentManager.fragments[0]
        return fragment as OnMovieClickListener
    }
}