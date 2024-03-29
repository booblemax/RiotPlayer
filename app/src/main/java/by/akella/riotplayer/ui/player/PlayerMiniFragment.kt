package by.akella.riotplayer.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.PlayerMiniFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.ui.main.MainFragmentDirections
import by.akella.riotplayer.ui.player.state.PlayerState
import by.akella.riotplayer.util.TimeUtils
import by.akella.riotplayer.util.collectState
import by.akella.riotplayer.util.loadAlbumIcon
import by.akella.riotplayer.util.onSafeClick
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerMiniFragment : BaseFragment() {

    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var binding: PlayerMiniFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PlayerMiniFragmentBinding.inflate(inflater, container, false)
        binding.playPause.setOnClickListener { viewModel.onPlayPauseClicked() }
        binding.next.setOnClickListener { viewModel.next() }
        binding.prev.setOnClickListener { viewModel.prev() }
        binding.progress.disableTouch()

        val decorView = requireActivity().window.decorView
        val parentView = decorView.findViewById<ViewGroup>(android.R.id.content)

        binding.blur.setupWith(parentView)
            .setBlurAlgorithm(RenderScriptBlur(requireContext()))
            .setBlurRadius(BLUR_RADIUS)

        binding.root.onSafeClick(SafeClickListener<Nothing> { navigateToPlayer() })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.container.collectState(
            viewLifecycleOwner,
            ::renderState
        )
    }

    private fun renderState(state: PlayerState) {
        with(state) {
            song?.let { renderSong(it) }
            renderPositionChanging(currentPlayPosition)
            renderPlayPause(isPlaying)
        }
    }

    private fun renderSong(song: SongUiModel) {
        binding.title.text = song.title
        binding.artist.text = song.artist
        binding.albumArt.loadAlbumIcon(
            song.albumArtPath,
            R.drawable.ic_musical_note
        )
        val duration = song.duration / TimeUtils.MILLIS
        if (binding.progress.valueTo != duration) {
            binding.progress.valueTo = duration
            binding.progress.valueFrom = 0
        }
    }

    private fun renderPositionChanging(nextPosition: Long) {
        binding.progress.value = nextPosition / TimeUtils.MILLIS
    }

    private fun renderPlayPause(isPlaying: Boolean) {
        binding.playPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause
            else R.drawable.ic_play
        )
    }

    private fun navigateToPlayer() {
        viewModel.container.stateFlow.value.run {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToPlayerFragment(
                    song?.id ?: "",
                    musicType?.ordinal ?: 0
                )
            )
        }
    }

    companion object {
        const val TAG = "PlayerMiniFragment"

        private const val BLUR_RADIUS = 20f
    }
}
