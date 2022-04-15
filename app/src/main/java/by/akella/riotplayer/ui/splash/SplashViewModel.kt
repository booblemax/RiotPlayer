package by.akella.riotplayer.ui.splash

import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.scanner.SingleMediaScanner
import by.akella.riotplayer.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val mediaScanner: SingleMediaScanner
) : BaseViewModel(dispatcherProvider), ContainerHost<SplashState, Nothing> {

    override val container: Container<SplashState, Nothing> = container(SplashState.Initial)

    fun granted() = intent { reduce { SplashState.Granted } }

    fun decline() = intent { reduce { SplashState.Decline } }

    fun scanFiles() {
        mediaScanner.scan()
        mediaScanner.onScanComplete = { intent { reduce { SplashState.Scanned } } }
    }
}