package by.akella.riotplayer.ui.splash

import androidx.hilt.lifecycle.ViewModelInject
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.ui.base.BaseViewModel
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.reduce
import com.babylon.orbit2.transform
import com.babylon.orbit2.viewmodel.container

class SplashViewModel @ViewModelInject constructor(
    dispatcherProvider: DispatcherProvider
) : BaseViewModel(dispatcherProvider), ContainerHost<SplashState, Nothing> {

    override val container: Container<SplashState, Nothing> = container(SplashState.Initial)

    fun granted() = orbit {
        transform { SplashState.Granted }.reduce { event }
    }

    fun decline() = orbit {
        transform { SplashState.Decline }.reduce { event }
    }
}