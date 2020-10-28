package by.akella.riotplayer.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.ui.base.BaseViewModel
import by.akella.riotplayer.ui.main.state.MainSideEffect
import by.akella.riotplayer.ui.main.state.MainState
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.viewmodel.container

class MainViewModel @ViewModelInject constructor(
    dispatchersProvider: DispatcherProvider
) : BaseViewModel(dispatchersProvider), ContainerHost<MainState, MainSideEffect> {

    override val container: Container<MainState, MainSideEffect> = container(MainState())
}
