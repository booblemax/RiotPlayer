package by.akella.riotplayer.ui.splash.compose

import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.scanner.SingleMediaScanner
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.splash.SplashState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SplashComposeViewModel  @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val mediaScanner: SingleMediaScanner
) : BaseViewModel(dispatcherProvider), ContainerHost<SplashState, Nothing> {

    override val container: Container<SplashState, Nothing> = container(SplashState.Initial)

    fun granted() = scanFiles()

    fun declined() = intent { reduce { SplashState.Decline } }

    private fun scanFiles() {
        mediaScanner.onScanComplete = { intent { reduce { SplashState.Scanned } } }
        mediaScanner.scan()
    }
}
