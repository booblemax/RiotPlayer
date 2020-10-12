package by.akella.riotplayer.ui.player.state

import by.akella.riotplayer.ui.base.model.SongUiModel

data class PlayerState(
    val song: SongUiModel? = null,
    val isPlaying: Boolean = false
) {
    override fun toString(): String {
        return "PlayerState(isPlaying=$isPlaying, song=$song)"
    }
}