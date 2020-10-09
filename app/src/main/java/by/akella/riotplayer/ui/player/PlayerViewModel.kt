package by.akella.riotplayer.ui.player

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import by.akella.riotplayer.media.RiotMediaController
import by.akella.riotplayer.repository.songs.SongModel
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.player.state.PlayerState
import by.akella.riotplayer.util.*
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.reduce
import com.babylon.orbit2.transform
import com.babylon.orbit2.viewmodel.container
import com.example.domain.dispatchers.DispatcherProvider

class PlayerViewModel @ViewModelInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val riotMediaController: RiotMediaController
) : BaseViewModel(dispatcherProvider), ContainerHost<PlayerState, Nothing> {

    private val nowPlayingSongObserver: Observer<MediaMetadataCompat>
    private val playbackStateObserver: Observer<PlaybackStateCompat>

    override val container: Container<PlayerState, Nothing> = container(PlayerState())

    init {
        with(riotMediaController) {
            nowPlayingSongObserver = Observer { media ->
                orbit {
                    transform {
                        SongUiModel(
                            media.id ?: "",
                            media.title ?: "",
                            media.artist ?: "")
                    }.reduce {
                        PlayerState(song = event)
                    }
                }
            }
            nowPlayingSong.observeForever(nowPlayingSongObserver)

            playbackStateObserver = Observer { state ->
                orbit {
                    transform {
                        state.isPlaying
                    }.reduce { PlayerState(isPlaying = event) }
                }
            }
            playbackState.observeForever(playbackStateObserver)
        }
    }

    fun onPlayPauseClicked() {
        riotMediaController.playbackState.value?.let {
            if (it.isPlaying) pause()
            else play(riotMediaController.rootMediaId)
        }
    }

    fun play(songModelId: String) {
        val nowPlaying = riotMediaController.nowPlayingSong.value

        val isPrepared = riotMediaController.playbackState.value?.isPrepared ?: false
        if (isPrepared && songModelId != nowPlaying?.id) {
            riotMediaController.playbackState.value?.let { state ->
                if (state.isPlayEnabled) {
                    riotMediaController.play()
                }
            }
        } else {
            riotMediaController.play(songModelId)
        }
    }

    fun pause() {
        riotMediaController.pause()
    }

    override fun onCleared() {
        super.onCleared()
        with(riotMediaController) {
            nowPlayingSong.removeObserver(nowPlayingSongObserver)
            playbackState.removeObserver(playbackStateObserver)
        }
    }
}