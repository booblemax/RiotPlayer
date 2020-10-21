package by.akella.riotplayer.ui.albums

import androidx.hilt.lifecycle.ViewModelInject
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.repository.albums.AlbumRepository
import by.akella.riotplayer.ui.base.BaseViewModel
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.viewmodel.container

class AlbumsViewModel @ViewModelInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val albumsRepository: AlbumRepository
) : BaseViewModel(dispatcherProvider), ContainerHost<AlbumsState, Nothing> {

    override val container: Container<AlbumsState, Nothing> = container(AlbumsState())

}