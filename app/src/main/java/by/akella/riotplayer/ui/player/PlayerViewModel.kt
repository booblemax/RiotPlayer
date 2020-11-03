package by.akella.riotplayer.ui.player

import android.os.Handler
import android.os.Looper
import androidx.core.os.bundleOf
import androidx.hilt.lifecycle.ViewModelInject
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.media.EMPTY_PLAYBACK_STATE
import by.akella.riotplayer.media.RiotMediaController
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.main.state.MusicType
import by.akella.riotplayer.ui.player.state.PlayerState
import by.akella.riotplayer.util.currentPlayBackPosition
import by.akella.riotplayer.util.isPlaying
import by.akella.riotplayer.util.isSkipState
import by.akella.riotplayer.util.isStop
import by.akella.riotplayer.util.toSongUiModel
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.reduce
import com.babylon.orbit2.transform
import com.babylon.orbit2.viewmodel.container
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PlayerViewModel @ViewModelInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val riotMediaController: RiotMediaController
) : BaseViewModel(dispatcherProvider), ContainerHost<PlayerState, Nothing> {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var needUpdateDuration = true

    private var playbackState = EMPTY_PLAYBACK_STATE
    private var seekToValue = DEFAULT_SEEK_TO_VALUE

    override val container: Container<PlayerState, Nothing> = container(PlayerState())

    init {
        initNowPlayingSongListener()
        initPlaybackStateListener()
        initShuffleModeListener()
        initRepeatModeListener()
        initConnectionListener()
    }

    private fun initConnectionListener() {
        riotMediaController.isConnectedToMediaBrowser.onEach {
            needUpdateDuration = it
            if (it) {
                checkDuration()
            }
        }.launchIn(baseScope)
    }

    private fun initRepeatModeListener() {
        riotMediaController.repeatMode.onEach {
            orbit {
                transform { it }.reduce { state.copy(isRepeatEnabled = event) }
            }
        }.launchIn(baseScope)
    }

    private fun initShuffleModeListener() {
        riotMediaController.shuffleMode.onEach {
            orbit {
                transform { it }.reduce { state.copy(isShuffleEnabled = event) }
            }
        }.launchIn(baseScope)
    }

    private fun initPlaybackStateListener() {
        riotMediaController.playbackState.onEach { playbackState ->
            orbit {
                transform {
                    this@PlayerViewModel.playbackState = playbackState
                    playbackState
                }.reduce {
                    state.copy(
                        isPlaying = event.isPlaying,
                        currentPlayPosition =
                        if (event.isStop) state.song?.duration ?: 0
                        else state.currentPlayPosition
                    )
                }
            }
        }.launchIn(baseScope)
    }

    private fun initNowPlayingSongListener() {
        riotMediaController.nowPlayingSong.onEach { media ->
            orbit {
                transform {
                    media.toSongUiModel()
                }.reduce {
                    val isSameSong = state.song?.id == event.id
                    state.copy(song = event, isSameSong = isSameSong, currentPlayPosition = 0)
                }
            }
        }.launchIn(baseScope)
    }

    fun onPlayPauseClicked() {
        if (riotMediaController.playbackState.value.isPlaying) {
            pause()
        } else {
            play(
                container.currentState.song?.id ?: "",
                container.currentState.musicType
            )
        }
    }

    fun play(mediaId: String, musicType: MusicType? = null) = orbit {
        transform {
            riotMediaController.play(
                mediaId,
                bundleOf(RiotMediaController.ARG_MUSIC_TYPE to musicType)
            )
        }.reduce { state.copy(musicType = musicType) }
    }


    fun pause() {
        riotMediaController.pause()
    }

    fun next() {
        riotMediaController.next()
    }

    fun prev() {
        riotMediaController.prev()
    }

    fun seekTo(pos: Long) {
        seekToValue = pos
        riotMediaController.seekTo(pos)
    }

    fun shuffle() {
        riotMediaController.setShuffleMode()
    }

    fun repeat() {
        riotMediaController.setRepeatMode()
    }

    private fun checkDuration(): Boolean = handler.postDelayed(
        {
            val playPosition = playbackState.currentPlayBackPosition

            resetSeekToPositionIfCan(playPosition)
            if (canApplyNewPosition(playPosition) && isValidState()) {
                orbit {
                    transform {
                        getValidPosition(state, playPosition)
                    }.reduce {
                        state.copy(currentPlayPosition = playPosition)
                    }
                }
            }
            if (needUpdateDuration) checkDuration()
        },
        POSITION_UPDATE_INTERVAL_MILLIS
    )

    private fun canApplyNewPosition(playPosition: Long) =
        playPosition != container.currentState.currentPlayPosition &&
                playPosition > seekToValue

    private fun isValidState() =
        playbackState.isPlaying && !playbackState.isSkipState

    private fun getValidPosition(state: PlayerState, playPosition: Long): Long {
        val duration = state.song?.duration ?: 0
        return when {
            playPosition > duration -> duration
            playPosition < seekToValue -> seekToValue
            else -> playPosition
        }
    }

    private fun resetSeekToPositionIfCan(playPosition: Long) {
        if (playPosition >= seekToValue) {
            seekToValue = DEFAULT_SEEK_TO_VALUE
        }
    }

    override fun onCleared() {
        super.onCleared()
        needUpdateDuration = false
    }

    companion object {
        private const val POSITION_UPDATE_INTERVAL_MILLIS = 300L
        private const val DEFAULT_SEEK_TO_VALUE = -1L
    }
}
