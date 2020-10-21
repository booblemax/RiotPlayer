package by.akella.riotplayer.ui.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import by.akella.riotplayer.databinding.ItemsFragmentBinding
import by.akella.riotplayer.repository.albums.AlbumModel
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.util.animateVisible
import by.akella.riotplayer.util.gone
import by.akella.riotplayer.util.visible
import com.babylon.orbit2.livedata.state
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumsFragment : BaseFragment() {

    val viewModel: AlbumsViewModel by viewModels()
    private lateinit var binding: ItemsFragmentBinding
    private lateinit var adapter: AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ItemsFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = AlbumAdapter(SafeClickListener { navigateToAlbumDetails(it) })
        with(binding) {
            items.layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
            items.addItemDecoration(GridItemDecoration())
            items.setHasFixedSize(true)
            items.adapter = adapter
        }

        viewModel.container.state.observe(viewLifecycleOwner) {
            if (it.loading) {
                binding.progress.visible()
                binding.items.gone()
            } else {
                binding.progress.gone()
                binding.items.animateVisible()
            }

            adapter.submitList(it.albums)
        }

        viewModel.load()
    }

    private fun navigateToAlbumDetails(albumModel: AlbumModel) {

    }

    companion object {
        private const val SPAN_COUNT = 2

        fun create() = AlbumsFragment()
    }
}
