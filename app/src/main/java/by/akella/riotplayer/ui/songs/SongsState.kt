package by.akella.riotplayer.ui.songs

import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.main.state.MusicType

data class SongsState(
    val songType: MusicType? = null,
    val loading: Boolean = true,
    val songs: List<SongUiModel> = listOf()
) {

    override fun toString(): String {
        return "SongsState(loading=$loading, songs=${songs::class.simpleName})"
    }
}
