package by.akella.riotplayer.ui.albums

import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.repository.albums.AlbumRepository
import by.akella.riotplayer.scanner.SingleMediaScanner
import by.akella.riotplayer.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val albumsRepository: AlbumRepository
) : BaseViewModel(dispatcherProvider), ContainerHost<AlbumsState, Nothing> {

    val stateFlow = MutableStateFlow(AlbumsState(loading = true))

    override val container: Container<AlbumsState, Nothing> = container(AlbumsState())

    fun load() = baseScope.launch {
        val albums = withContext(Dispatchers.IO) {
            albumsRepository.getAlbums()
        }
        stateFlow.value = AlbumsState(false, albums)
    }
}
