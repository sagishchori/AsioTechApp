package sagi.shchori.asiotechapp

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sagi.shchori.asiotechapp.database.AppDataBase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDataBase(@ApplicationContext appContext: Context) = AppDataBase.getDB(appContext)

    @Singleton
    @Provides
    fun provideLocationDao(db: AppDataBase) = db.movieDao()
}