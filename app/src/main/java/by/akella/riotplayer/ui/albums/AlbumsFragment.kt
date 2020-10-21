package by.akella.riotplayer.ui.albums

import androidx.fragment.app.viewModels
import by.akella.riotplayer.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumsFragment : BaseFragment() {

    val viewModel: AlbumsViewModel by viewModels()

    companion object {
        fun create() = AlbumsFragment()
    }
}
