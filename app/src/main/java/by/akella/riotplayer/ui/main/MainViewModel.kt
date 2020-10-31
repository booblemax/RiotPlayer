package by.akella.riotplayer.ui.main

import android.media.MediaMetadata
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.media.RiotMediaController
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.main.state.MainSideEffect
import by.akella.riotplayer.ui.main.state.MainState
import by.akella.riotplayer.util.id
import by.akella.riotplayer.util.title
import by.akella.riotplayer.util.toSongUiModel
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.reduce
import com.babylon.orbit2.transform
import com.babylon.orbit2.viewmodel.container

class MainViewModel @ViewModelInject constructor(
    dispatchersProvider: DispatcherProvider,
    private val riotMediaController: RiotMediaController
) : BaseViewModel(dispatchersProvider), ContainerHost<MainState, MainSideEffect> {

    override val container: Container<MainState, MainSideEffect> = container(MainState())

    private val isConnectedObserver: Observer<Boolean> = Observer {
        orbit {
            transform { it }.reduce { state.copy(playerConnected = event) }
        }
    }

    private val playbackObserver: Observer<PlaybackStateCompat> = Observer {
        val stateForDisplay = when (it.state) {
            PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.STATE_ERROR -> false
            else -> true
        }
        orbit {
            transform { stateForDisplay }.reduce { state.copy(playerDisplay = event) }
        }
    }

    private val nowPlayingObserver: Observer<MediaMetadataCompat> = Observer {
        orbit {
            transform { it.toSongUiModel() }.reduce { state.copy(nowPlayingSong = event) }
        }
    }

    init {
        riotMediaController.isConnected.observeForever(isConnectedObserver)
        riotMediaController.playbackState.observeForever(playbackObserver)
        riotMediaController.nowPlayingSong.observeForever(nowPlayingObserver)
    }

    override fun onCleared() {
        super.onCleared()
        riotMediaController.isConnected.removeObserver(isConnectedObserver)
        riotMediaController.playbackState.removeObserver(playbackObserver)
        riotMediaController.nowPlayingSong.removeObserver(nowPlayingObserver)
    }
}
