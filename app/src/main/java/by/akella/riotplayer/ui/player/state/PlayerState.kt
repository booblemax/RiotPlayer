package by.akella.riotplayer.ui.player.state

import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.main.state.MusicType

data class PlayerState(
    val song: SongUiModel? = null,
    val currentPlayPosition: Long = 0L,
    val isSameSong: Boolean = false,
    val isPlaying: Boolean = false,
    val musicType: MusicType? = null,
    val isRepeatEnabled: Boolean = false,
    val isShuffleEnabled: Boolean = false
) {
    override fun toString(): String {
        return "PlayerState(isPlaying=$isPlaying, song=$song, isSameSong=$isSameSong, isPlaying=$isPlaying," +
                " currentPlayPosition=$currentPlayPosition), isRepeatEnabled=$isRepeatEnabled, isShuffleEnabled=$isShuffleEnabled"
    }
}
