package by.akella.riotplayer.di

import android.content.Context
import androidx.room.Room
import by.akella.riotplayer.db.RiotDatabase
import by.akella.riotplayer.db.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
class DbModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): RiotDatabase {
        return Room.databaseBuilder(context, RiotDatabase::class.java, RiotDatabase.DATABASE_NAME).build()
    }

    @Provides
    fun provideSongDao(db: RiotDatabase): SongDao = db.songsDao

}