package by.akella.riotplayer.ui.main

import android.support.v4.media.session.PlaybackStateCompat
import androidx.hilt.lifecycle.ViewModelInject
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.media.RiotMediaController
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.main.state.MainSideEffect
import by.akella.riotplayer.ui.main.state.MainState
import by.akella.riotplayer.util.toSongUiModel
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.reduce
import com.babylon.orbit2.transform
import com.babylon.orbit2.viewmodel.container
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel @ViewModelInject constructor(
    dispatchersProvider: DispatcherProvider,
    private val riotMediaController: RiotMediaController
) : BaseViewModel(dispatchersProvider), ContainerHost<MainState, MainSideEffect> {

    override val container: Container<MainState, MainSideEffect> = container(MainState())

    init {
        initConnectionListener()
        initPlaybackStateListener()
        initNowPlayingSongListener()
    }

    private fun initNowPlayingSongListener() {
        riotMediaController.nowPlayingSong.onEach {
            orbit {
                transform { it.toSongUiModel() }.reduce { state.copy(nowPlayingSong = event) }
            }
        }.launchIn(baseScope)
    }

    private fun initPlaybackStateListener() {
        riotMediaController.playbackState.onEach {
            val stateForDisplay = when (it.state) {
                PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.STATE_ERROR -> false
                else -> true
            }
            orbit {
                transform { stateForDisplay }.reduce { state.copy(playerDisplay = event) }
            }
        }.launchIn(baseScope)
    }

    private fun initConnectionListener() {
        riotMediaController.isConnectedToMediaBrowser.onEach {
            orbit {
                transform { it }.reduce { state.copy(playerConnected = event) }
            }
        }.launchIn(baseScope)
    }
}
