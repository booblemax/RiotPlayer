package by.akella.riotplayer.ui.songs

import by.akella.riotplayer.ui.base.model.SongUiModel

data class SongsState(
    val loading: Boolean = true,
    val songs: List<SongUiModel> = listOf()
) {

    override fun toString(): String {
        return "SongsState(loading=$loading, songs=${songs::class.simpleName})"
    }
}
