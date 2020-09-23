package by.akella.riotplayer.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import by.akella.riotplayer.ui.base.BaseViewModel
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.viewmodel.container
import com.example.domain.dispatchers.DispatcherProvider

class MainViewModel @ViewModelInject constructor(dispatcherProvider: DispatcherProvider) :
    BaseViewModel(dispatcherProvider), ContainerHost<MainState, Nothing> {

    override val container: Container<MainState, Nothing> = container(MainState())


}