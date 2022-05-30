package by.akella.riotplayer.ui.main

import android.support.v4.media.session.PlaybackStateCompat
import androidx.hilt.lifecycle.ViewModelInject
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.media.RiotMediaController
import by.akella.riotplayer.scanner.SingleMediaScanner
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.main.state.MainSideEffect
import by.akella.riotplayer.ui.main.state.MainState
import by.akella.riotplayer.util.toSongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    dispatchersProvider: DispatcherProvider,
    private val riotMediaController: RiotMediaController,
    private val scanner: SingleMediaScanner
) : BaseViewModel(dispatchersProvider), ContainerHost<MainState, MainSideEffect> {

    val stateFlow = MutableStateFlow(MainState())

    override val container: Container<MainState, MainSideEffect> = container(MainState())

    init {
        initConnectionListener()
        initPlaybackStateListener()
        initNowPlayingSongListener()
    }

    fun rescan() {
        scanner.scan()
    }

    private fun initNowPlayingSongListener() {
        riotMediaController.nowPlayingSong.onEach {
            val event = it.toSongUiModel()
            stateFlow.value = stateFlow.value.copy(nowPlayingSong = event)
        }.launchIn(baseScope)
    }

    private fun initPlaybackStateListener() {
        riotMediaController.playbackState.onEach {
            val stateForDisplay = when (it.state) {
                PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.STATE_ERROR -> false
                else -> true
            }
            stateFlow.value = stateFlow.value.copy(playerDisplay = stateForDisplay)
        }.launchIn(baseScope)
    }

    private fun initConnectionListener() {
        riotMediaController.isConnectedToMediaBrowser.onEach {
            stateFlow.value = stateFlow.value.copy(playerConnected = it)
        }.launchIn(baseScope)
    }
}
