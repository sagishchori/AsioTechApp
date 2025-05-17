package sagi.shchori.asiotechapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sagi.shchori.asiotechapp.Logger
import sagi.shchori.asiotechapp.ui.models.Movie

@Database(entities = [Movie::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {

        private var INSTANCE: AppDataBase? = null

        fun getDB(context: Context): AppDataBase {

            Logger.i("Getting DB instance, instance = $INSTANCE")

            return INSTANCE ?: synchronized(this) {
                Logger.i("Creating new DB instance")

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "appDataBase"
                ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}