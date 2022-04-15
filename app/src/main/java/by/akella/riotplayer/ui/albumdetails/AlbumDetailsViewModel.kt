package by.akella.riotplayer.ui.albumdetails

import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.repository.albums.AlbumModel
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.base.model.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val songsRepository: SongsRepository
) : BaseViewModel(dispatcherProvider), ContainerHost<AlbumDetailsState, Nothing> {

    override val container: Container<AlbumDetailsState, Nothing> = container(AlbumDetailsState())

    fun loadSongs(albumModel: AlbumModel) = intent {

        val songs = songsRepository.getSongsByAlbum(albumModel.id)
                .map { SongUiModel(it.id, it.title, it.artist, it.albumArt, it.duration) }
        reduce {
            state.copy(
                album = albumModel,
                songs = songs,
                countSongs = songs.size,
                durationSongs = countHoursSongsPlay(songs)
            )
        }
    }

    private fun countHoursSongsPlay(songs: List<SongUiModel>): Long =
        songs.map { it.duration }.fold(0L) { acc, v -> acc + v.toInt() }

}
