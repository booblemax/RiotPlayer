package by.akella.riotplayer.ui.songs

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.main.state.MusicTabs
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.coroutines.transformSuspend
import com.babylon.orbit2.reduce
import com.babylon.orbit2.sideEffect
import com.babylon.orbit2.viewmodel.container

class SongsViewModel @ViewModelInject constructor(
    dispatchersProvider: DispatcherProvider,
    private val songsRepository: SongsRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel(dispatchersProvider), ContainerHost<SongsState, SongsSideEffect> {

    var songType: MusicTabs? = null

    override val container: Container<SongsState, SongsSideEffect> = container(SongsState()) {
        orbit {
            sideEffect {
                post(SongsSideEffect.ScanFiles)
            }
        }
    }

    fun loadSongs() = orbit {
        transformSuspend {
            loadSongByType().map { SongUiModel(it.id, it.title, it.artist, it.albumArt) }
        }.reduce {
            state.copy(loading = false, songs = event)
        }
    }

    private suspend fun loadSongByType() = when (songType) {
        MusicTabs.ALL_SONGS -> songsRepository.getAllSongs()
        MusicTabs.RECENTS -> songsRepository.getRecentSongs()
        else -> listOf()
    }
}
