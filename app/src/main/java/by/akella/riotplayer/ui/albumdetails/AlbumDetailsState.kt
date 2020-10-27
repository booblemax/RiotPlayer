package by.akella.riotplayer.ui.albumdetails

import by.akella.riotplayer.repository.albums.AlbumModel
import by.akella.riotplayer.ui.base.model.SongUiModel

data class AlbumDetailsState(
    val album: AlbumModel? = null,
    val songs: List<SongUiModel> = listOf(),
    val countSongs: Int = -1,
    val durationSongs: Long = -1L
)
