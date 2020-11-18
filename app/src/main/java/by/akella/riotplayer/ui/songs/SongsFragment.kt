package by.akella.riotplayer.ui.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.akella.riotplayer.databinding.ItemsFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.ui.main.MainFragmentDirections
import by.akella.riotplayer.ui.main.state.MusicType
import by.akella.riotplayer.util.error
import by.akella.riotplayer.util.gone
import by.akella.riotplayer.util.info
import by.akella.riotplayer.util.visible
import com.babylon.orbit2.livedata.state
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SongsFragment : BaseFragment() {

    private val viewModel: SongsViewModel by viewModels()
    private lateinit var binding: ItemsFragmentBinding
    private lateinit var adapter: SongsAdapter

    override fun onResume() {
        super.onResume()
        val songType = viewModel.container.currentState.songType
        if (songType == MusicType.RECENTS) {
            viewModel.loadSongs(songType)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemsFragmentBinding.inflate(inflater, container, false)
        binding.clearHistory.setOnClickListener { viewModel.clearHistory() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SongsAdapter(SafeClickListener { it?.let { navigateToPlayer(it) } })
        with(binding.items) {
            addItemDecoration(BottomOffsetItemDecoration())
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@SongsFragment.adapter
        }

        viewModel.container.state.observe(viewLifecycleOwner) { render(it) }

        load()
    }

    private fun render(state: SongsState) {
        info(state.toString())
        with(state) {
            if (loading) {
                binding.progress.visible()
                binding.items.gone()
                binding.clearHistory.gone()
                binding.emptyText.gone()
            } else {
                binding.progress.gone()

                if (viewModel.container.currentState.songType == MusicType.RECENTS &&
                    !songs.isNullOrEmpty()
                ) {
                    binding.clearHistory.visible()
                }

                when {
                    songs == null -> {
                        binding.items.gone()
                        binding.emptyText.gone()
                    }
                    songs.isEmpty() -> {
                        binding.items.gone()
                        binding.emptyText.visible()
                    }
                    else -> {
                        binding.emptyText.gone()
                        binding.items.visible()
                    }
                }
            }

            adapter.submitList(songs)
        }
        error(state.toString())
    }

    private fun load() {
        val songTypePosition = arguments?.getInt(ARG_TAB_TYPE)
        viewModel.loadSongs(songTypePosition?.let { MusicType.values()[it] }
            ?: MusicType.ALL_SONGS)
    }

    private fun navigateToPlayer(songUiModel: SongUiModel) {
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToPlayerFragment(
                songUiModel.id,
                viewModel.container.currentState.songType?.ordinal ?: 0
            )
        )
    }

    companion object {
        const val ARG_TAB_TYPE = "arg_tab_type"

        fun create(tabType: MusicType): Fragment = SongsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_TAB_TYPE, tabType.ordinal)
            }
        }
    }
}
