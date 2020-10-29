package by.akella.riotplayer.ui.player

import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.os.bundleOf
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Observer
import by.akella.riotplayer.media.EMPTY_PLAYBACK_STATE
import by.akella.riotplayer.media.RiotMediaController
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.player.state.PlayerState
import by.akella.riotplayer.util.id
import by.akella.riotplayer.util.title
import by.akella.riotplayer.util.artist
import by.akella.riotplayer.util.albumArtUri
import by.akella.riotplayer.util.duration
import by.akella.riotplayer.util.isPlaying
import by.akella.riotplayer.util.isPrepared
import by.akella.riotplayer.util.isPlayEnabled
import by.akella.riotplayer.util.currentPlayBackPosition
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.reduce
import com.babylon.orbit2.transform
import com.babylon.orbit2.viewmodel.container
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.ui.main.state.MusicTabs
import by.akella.riotplayer.util.info
import by.akella.riotplayer.util.stateName

class PlayerViewModel @ViewModelInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val riotMediaController: RiotMediaController
) : BaseViewModel(dispatcherProvider), ContainerHost<PlayerState, Nothing> {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var needUpdateDuration = true

    private val nowPlayingSongObserver: Observer<MediaMetadataCompat>
    private val playbackStateObserver: Observer<PlaybackStateCompat>
    private val connectionObserver: Observer<Boolean>
    private var playbackState = EMPTY_PLAYBACK_STATE

    override val container: Container<PlayerState, Nothing> = container(PlayerState())

    init {
        with(riotMediaController) {
            nowPlayingSongObserver = Observer { media ->
                orbit {
                    transform {
                        media.id?.let {
                            SongUiModel(
                                it,
                                media.title ?: "",
                                media.artist ?: "",
                                media.albumArtUri.toString(),
                                media.duration
                            )
                        }
                    }.reduce {
                        val isSameSong = state.song?.id == event?.id
                        state.copy(song = event, isSameSong = isSameSong, currentPlayPosition = 0)
                    }
                }
            }
            nowPlayingSong.observeForever(nowPlayingSongObserver)

            playbackStateObserver = Observer { playbackState ->
                orbit {
                    transform {
                        this@PlayerViewModel.playbackState = playbackState
                        info(playbackState.stateName)
                        playbackState.isPlaying
                    }.reduce {
                        state.copy(isPlaying = event)
                    }
                }
            }
            playbackState.observeForever(playbackStateObserver)

            connectionObserver = Observer {
                needUpdateDuration = it
                if (it) {
                    checkDuration()
                }
            }
            isConnected.observeForever(connectionObserver)
        }
    }

    fun onPlayPauseClicked() {
        riotMediaController.playbackState.value?.let {
            if (it.isPlaying) pause()
            else play(container.currentState.song?.id ?: "")
        }
    }

    fun play(mediaId: String, musicType: MusicTabs? = null) {
        riotMediaController.play(
            mediaId,
            bundleOf(RiotMediaController.ARG_MUSIC_TYPE to musicType)
        )
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

    fun seekTo(pos: Int) {
        riotMediaController.seekTo(pos)
    }

    private fun checkDuration(): Boolean = handler.postDelayed(
        {
            val playPosition = playbackState.currentPlayBackPosition
            if (playPosition != container.currentState.currentPlayPosition &&
                playbackState.isPlaying
            ) {
                orbit {
                    transform { playPosition }.reduce {
                        val duration = state.song?.duration ?: 0
                        state.copy(
                            currentPlayPosition = if (playPosition > duration) duration else playPosition
                        )
                    }
                }
            }
            if (needUpdateDuration) checkDuration()
        },
        POSITION_UPDATE_INTERVAL_MILLIS
    )

    override fun onCleared() {
        super.onCleared()
        with(riotMediaController) {
            nowPlayingSong.removeObserver(nowPlayingSongObserver)
            playbackState.removeObserver(playbackStateObserver)
            isConnected.removeObserver(connectionObserver)
        }
        needUpdateDuration = false
    }

    companion object {
        private const val POSITION_UPDATE_INTERVAL_MILLIS = 500L
    }
}
