package by.akella.riotplayer.ui.songs

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.akella.riotplayer.databinding.ItemsFragmentBinding
import by.akella.riotplayer.scanner.SingleMediaScanner
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.ui.main.MainFragmentDirections
import by.akella.riotplayer.ui.main.state.MusicTabs
import by.akella.riotplayer.util.animateVisible
import by.akella.riotplayer.util.error
import by.akella.riotplayer.util.gone
import by.akella.riotplayer.util.info
import by.akella.riotplayer.util.snack
import by.akella.riotplayer.util.visible
import com.babylon.orbit2.livedata.sideEffect
import com.babylon.orbit2.livedata.state
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class SongsFragment : BaseFragment(), EasyPermissions.PermissionCallbacks {

    private val viewModel: SongsViewModel by viewModels()
    private lateinit var binding: ItemsFragmentBinding
    private lateinit var adapter: SongsAdapter

    override fun onResume() {
        super.onResume()
        val songType = viewModel.container.currentState.songType
        if (songType == MusicTabs.RECENTS) {
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

        adapter = SongsAdapter(SafeClickListener { navigateToPlayer(it) })
        with(binding.items) {
            addItemDecoration(BottomOffsetItemDecoration())
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@SongsFragment.adapter
        }

        viewModel.container.state.observe(viewLifecycleOwner) { render(it) }
        viewModel.container.sideEffect.observe(viewLifecycleOwner) { processSideEffect(it) }
    }

    private fun render(state: SongsState) {
        with(state) {
            if (loading) {
                binding.progress.visible()
                binding.items.gone()
                binding.clearHistory.gone()
                binding.emptyText.gone()
            } else {
                binding.progress.gone()

                if (viewModel.container.currentState.songType == MusicTabs.RECENTS &&
                    songs.isNotEmpty()) {
                    binding.clearHistory.visible()
                }

                if (songs.isEmpty()) {
                    binding.items.gone()
                    binding.emptyText.visible()
                } else {
                    binding.emptyText.gone()
                    binding.items.visible()
                }
            }

            adapter.submitList(songs)
        }
        error(state.toString())
    }

    private fun processSideEffect(sideEffect: SongsSideEffect) {
        if (sideEffect is SongsSideEffect.ScanFiles) {
            val scanner = SingleMediaScanner(requireContext())
            scanner.onScanComplete = { load() }
            scanner.scan()
        }
    }

    private fun load() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val songTypePosition = arguments?.getInt(ARG_TAB_TYPE)
            viewModel.loadSongs(songTypePosition?.let { MusicTabs.values()[it] }
                ?: MusicTabs.ALL_SONGS)
        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to internal storage",
                REQUEST_INTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_INTERNAL_STORAGE) {
            viewModel.loadSongs()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        view?.snack("Need permission to read music") { onBackPressed() }
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
        const val REQUEST_INTERNAL_STORAGE = 1000
        const val ARG_TAB_TYPE = "arg_tab_type"

        fun create(tabType: MusicTabs): Fragment = SongsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_TAB_TYPE, tabType.ordinal)
            }
        }
    }
}
