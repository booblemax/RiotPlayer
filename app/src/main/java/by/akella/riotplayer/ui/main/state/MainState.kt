package by.akella.riotplayer.ui.main.state

import by.akella.riotplayer.R
import by.akella.riotplayer.ui.base.model.SongUiModel

data class MainState(
    val nowPlayingSong: SongUiModel? = null,
    val playerConnected: Boolean = false,
    val playerDisplay: Boolean = false
) {

    override fun toString(): String {
        return "MainState(nowPlayingSong = ${nowPlayingSong}, " +
                "playerConnected = $playerConnected, playerDisplay = $playerDisplay)"
    }
}

enum class MusicType(val tabName: Int) {
    ALBUMS(R.string.tab_albums),
    ALL_SONGS(R.string.tab_all_songs),
    RECENTS(R.string.tab_recents)
}
