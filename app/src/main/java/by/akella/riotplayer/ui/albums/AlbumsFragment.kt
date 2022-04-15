package by.akella.riotplayer.ui.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import by.akella.riotplayer.databinding.ItemsFragmentBinding
import by.akella.riotplayer.repository.albums.AlbumModel
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.ui.main.MainFragmentDirections
import by.akella.riotplayer.util.collectState
import by.akella.riotplayer.util.gone
import by.akella.riotplayer.util.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumsFragment : BaseFragment() {

    private val viewModel: AlbumsViewModel by viewModels()
    private lateinit var binding: ItemsFragmentBinding
    private lateinit var adapter: AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemsFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = AlbumAdapter(SafeClickListener { it?.let { navigateToAlbumDetails(it) } })
        with(binding) {
            items.layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
            items.addItemDecoration(GridItemDecoration())
            items.setHasFixedSize(true)
            items.adapter = adapter
        }

        viewModel.container.collectState(
            viewLifecycleOwner,
            ::renderState
        )
    }

    private fun renderState(state: AlbumsState) {
        if (state.loading) {
            binding.progress.visible()
            binding.items.gone()
            binding.emptyText.gone()
        } else {
            binding.progress.gone()

            when {
                state.albums == null -> {
                    binding.items.gone()
                    binding.emptyText.gone()
                }
                state.albums.isEmpty() -> {
                    binding.items.gone()
                    binding.emptyText.visible()
                }
                else -> {
                    binding.items.visible()
                    binding.emptyText.gone()
                }
            }
        }

        adapter.submitList(state.albums)
    }

    private fun navigateToAlbumDetails(albumModel: AlbumModel) {
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToAlbumDetailsFragment((albumModel))
        )
    }

    companion object {
        private const val SPAN_COUNT = 2

        fun create() = AlbumsFragment()
    }
}
