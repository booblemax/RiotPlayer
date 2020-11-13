package by.akella.riotplayer.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.PlayerMiniFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.ui.main.MainFragmentDirections
import by.akella.riotplayer.util.TimeUtils
import by.akella.riotplayer.util.info
import by.akella.riotplayer.util.loadAlbumIcon
import by.akella.riotplayer.util.onSafeClick
import com.babylon.orbit2.livedata.state
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderScriptBlur

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
            .setBlurRadius(20f)
            .setHasFixedTransformationMatrix(true)

        binding.root.onSafeClick(SafeClickListener<Nothing> { navigateToPlayer() })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.container.state.observe(viewLifecycleOwner) { state ->
            with(state) {
                song?.let {
                    binding.title.text = it.title
                    binding.artist.text = it.artist
                    binding.albumArt.loadAlbumIcon(
                        it.albumArtPath,
                        R.drawable.ic_musical_note
                    )
                    val duration = it.duration / TimeUtils.MILLIS
                    if (binding.progress.valueTo != duration) {
                        binding.progress.valueTo = duration
                        binding.progress.valueFrom = 0
                    }
                }

                binding.progress.value = currentPlayPosition / TimeUtils.MILLIS
                renderPlayPause(isPlaying)
            }
        }
    }

    private fun renderPlayPause(isPlaying: Boolean) {
        binding.playPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause
            else R.drawable.ic_play
        )
    }

    private fun navigateToPlayer() {
        viewModel.container.currentState.run {
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
    }
}
