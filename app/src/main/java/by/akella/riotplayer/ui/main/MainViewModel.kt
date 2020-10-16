package by.akella.riotplayer.ui.main

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.main.state.MainSideEffect
import by.akella.riotplayer.ui.main.state.MainState
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.coroutines.transformSuspend
import com.babylon.orbit2.reduce
import com.babylon.orbit2.sideEffect
import com.babylon.orbit2.viewmodel.container
import com.example.domain.dispatchers.DispatcherProvider

class MainViewModel @ViewModelInject constructor(
    dispatchersProvider: DispatcherProvider,
    private val songsRepository: SongsRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : BaseViewModel(dispatchersProvider), ContainerHost<MainState, MainSideEffect> {

    override val container: Container<MainState, MainSideEffect> = container(MainState()) {
        orbit {
            sideEffect {
                post(MainSideEffect.ScanFiles)
            }
        }
    }

    fun loadSongs() = orbit {
        transformSuspend {
            songsRepository.getSongs().map { SongUiModel(it.id, it.title, it.artist, it.albumArt) }
        }.reduce {
            state.copy(loading = false, songs = event)
        }
    }
}
