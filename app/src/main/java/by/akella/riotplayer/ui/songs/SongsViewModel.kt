package by.akella.riotplayer.ui.songs

import androidx.hilt.lifecycle.ViewModelInject
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.main.state.MusicType
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.coroutines.transformSuspend
import com.babylon.orbit2.reduce
import com.babylon.orbit2.sideEffect
import com.babylon.orbit2.viewmodel.container

class SongsViewModel @ViewModelInject constructor(
    dispatchersProvider: DispatcherProvider,
    private val songsRepository: SongsRepository
) : BaseViewModel(dispatchersProvider), ContainerHost<SongsState, SongsSideEffect> {

    override val container: Container<SongsState, SongsSideEffect> = container(SongsState()) {
        orbit {
            sideEffect {
                post(SongsSideEffect.ScanFiles)
            }
        }
    }

    fun loadSongs(songType: MusicType? = null) = orbit {
        transformSuspend {
            loadSongByType(songType).map { SongUiModel(it.id, it.title, it.artist, it.albumArt) }
        }.reduce {
            state.copy(
                songType = songType,
                loading = false,
                songs = event)
        }
    }

    fun clearHistory() = orbit {
        transformSuspend {
            songsRepository.clearRecent()
        }.reduce {
            state.copy(songs = emptyList())
        }
    }

    private suspend fun loadSongByType(songType: MusicType? = null) = when (songType) {
        MusicType.ALL_SONGS -> songsRepository.getAllSongs()
        MusicType.RECENTS -> songsRepository.getRecentSongs()
        else -> listOf()
    }
}
