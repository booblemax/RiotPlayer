package by.akella.riotplayer.ui.albumdetails

import androidx.hilt.lifecycle.ViewModelInject
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.repository.albums.AlbumModel
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.base.model.SongUiModel
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.coroutines.transformSuspend
import com.babylon.orbit2.reduce
import com.babylon.orbit2.viewmodel.container

class AlbumDetailsViewModel @ViewModelInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val songsRepository: SongsRepository
) : BaseViewModel(dispatcherProvider), ContainerHost<AlbumDetailsState, Nothing> {

    override val container: Container<AlbumDetailsState, Nothing> = container(AlbumDetailsState())

    fun loadSongs(albumModel: AlbumModel) = orbit {
        transformSuspend {
            songsRepository.getSongsByAlbum(albumModel.id)
                .map { SongUiModel(it.id, it.title, it.artist, it.albumArt, it.duration) }
        }.reduce {
            state.copy(
                album = albumModel,
                songs = event,
                countSongs = event.size,
                durationSongs = countHoursSongsPlay(event)
            )
        }
    }

    private fun countHoursSongsPlay(songs: List<SongUiModel>): Long {
        return songs.map { it.duration }.fold(0L) { acc, v -> acc + v.toInt() }
    }
}
