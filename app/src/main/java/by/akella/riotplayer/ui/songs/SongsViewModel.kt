package by.akella.riotplayer.ui.songs

import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.main.state.MusicType
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    dispatchersProvider: DispatcherProvider,
    private val songsRepository: SongsRepository
) : BaseViewModel(dispatchersProvider), ContainerHost<SongsState, Nothing> {

    override val container: Container<SongsState, Nothing> = container(SongsState())

    fun loadSongs(songType: MusicType? = null) = intent {
        val songs = loadSongByType(songType).map { SongUiModel(it.id, it.title, it.artist, it.albumArt) }
        reduce {
            state.copy(
                songType = songType,
                loading = false,
                songs = songs)
        }
    }

    fun clearHistory() = intent {
        songsRepository.clearRecent()
        reduce {
            state.copy(songs = emptyList())
        }
    }

    private suspend fun loadSongByType(songType: MusicType? = null) = when (songType) {
        MusicType.ALL_SONGS -> songsRepository.getAllSongs()
        MusicType.RECENTS -> songsRepository.getRecentSongs()
        else -> listOf()
    }
}
