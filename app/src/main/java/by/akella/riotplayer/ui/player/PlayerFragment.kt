package by.akella.riotplayer.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.PlayerFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.ui.main.state.MusicType
import by.akella.riotplayer.util.TimeUtils
import by.akella.riotplayer.util.animateGone
import by.akella.riotplayer.util.animateVisible
import by.akella.riotplayer.util.loadAlbumIcon
import by.akella.riotplayer.util.onSafeClick
import com.babylon.orbit2.livedata.state
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerFragment : BaseFragment() {

    private val viewModel by viewModels<PlayerViewModel>()
    private lateinit var binding: PlayerFragmentBinding
    private val args by lazy { PlayerFragmentArgs.fromBundle(requireArguments()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PlayerFragmentBinding.inflate(inflater, container, false)
        binding.playPause.setOnClickListener { viewModel.onPlayPauseClicked() }
        binding.next.setOnClickListener { viewModel.next() }
        binding.prev.setOnClickListener { viewModel.prev() }
        binding.progressBar.onTouchEnds = { viewModel.seekTo(it * TimeUtils.MILLIS) }
        binding.shuffle.onSafeClick(null, SafeClickListener { viewModel.shuffle() })
        binding.repeat.onSafeClick(null, SafeClickListener { viewModel.repeat() })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.container.state.observe(viewLifecycleOwner) { state ->
            with(state) {
                song?.let {
                    if (!isSameSong) {
                        binding.songName.text = it.title
                        binding.songArtist.text = it.artist
                        binding.albumImage.loadAlbumIcon(
                            it.albumArtPath,
                            R.drawable.ic_musical_note
                        ) { postponeEnterTransition() }
                        binding.allPlayTime.text =
                            TimeUtils.convertMillisToShortTime(requireContext(), it.duration)

                        val duration = it.duration / TimeUtils.MILLIS
                        binding.progressBar.valueTo = duration.toFloat()
                        binding.progressBar.valueFrom = 0f
                    }

                    binding.progressBar.value = currentPlayPosition / TimeUtils.MILLIS.toFloat()
                    binding.currentPlayTime.text =
                        TimeUtils.convertMillisToShortTime(
                            requireContext(),
                            currentPlayPosition
                        )
                }
                renderPlayPause(isPlaying)
                renderShuffle(isShuffleEnabled)
                renderRepeat(isRepeatEnabled)
            }
        }
        viewModel.play(args.mediaId, MusicType.values()[args.musicType])
        startPostponedEnterTransition()
    }

    private fun renderPlayPause(isPlaying: Boolean) {
        binding.playPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause
            else R.drawable.ic_play
        )
    }

    private fun renderShuffle(isShuffleEnabled: Boolean) {
        binding.shuffle.setImageResource(
            if (isShuffleEnabled) {
                binding.shuffleBackground.animateVisible()
                R.drawable.ic_shuffle_active
            } else {
                binding.shuffleBackground.animateGone()
                R.drawable.ic_shuffle_inactive
            }
        )
    }

    private fun renderRepeat(isRepeatEnabled: Boolean) {
        binding.repeat.setImageResource(
            if (isRepeatEnabled) {
                binding.repeatBackground.animateVisible()
                R.drawable.ic_repeat_active
            }
            else {
                binding.repeatBackground.animateGone()
                R.drawable.ic_repeat_inactive
            }
        )
    }
}
