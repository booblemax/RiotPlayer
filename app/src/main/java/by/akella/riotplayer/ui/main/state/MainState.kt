package by.akella.riotplayer.ui.main.state

import by.akella.riotplayer.ui.base.model.SongUiModel

data class MainState (
    val loading: Boolean = true,
    val songs: List<SongUiModel> = listOf()
) {

    override fun toString(): String {
        return "MainState(loading=$loading, songs=${songs::class.simpleName})"
    }
}