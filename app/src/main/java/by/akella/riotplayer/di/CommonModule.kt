package by.akella.riotplayer.di

import android.content.ComponentName
import android.content.Context
import by.akella.riotplayer.dispatchers.DefaultDispatcherProvider
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.media.QueueManager
import by.akella.riotplayer.media.RiotMediaController
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.repository.songs.SongsRepositoryImpl
import by.akella.riotplayer.service.RiotMusicService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class CommonModule {

    @Singleton
    @Provides
    fun provideRiotMediaController(@ApplicationContext context: Context): RiotMediaController {
        return RiotMediaController(context, ComponentName(context, RiotMusicService::class.java))
    }

    @Singleton
    @Provides
    fun provideSongsRepository(@ApplicationContext context: Context): SongsRepository {
        return SongsRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideQueueManager() = QueueManager()

    @Provides
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
