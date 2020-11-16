package by.akella.riotplayer.ui.albums

import by.akella.riotplayer.repository.albums.AlbumModel

data class AlbumsState(
    val loading: Boolean = false,
    val albums: List<AlbumModel>? = null
)
