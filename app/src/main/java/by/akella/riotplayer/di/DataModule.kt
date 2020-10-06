package by.akella.riotplayer.di

import by.akella.riotplayer.dispatchers.DefaultDispatcherProvider
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.repository.songs.SongsRepositoryImpl
import com.example.domain.dispatchers.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ApplicationComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class DataModule {

    @Binds
    abstract fun provideSongsRepository(songsRepositoryImpl: SongsRepositoryImpl): SongsRepository

    @Binds
    abstract fun provideDispatcherProvider(provider: DefaultDispatcherProvider): DispatcherProvider
}