package by.akella.riotplayer.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.PlayerFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.ui.main.state.MusicType
import by.akella.riotplayer.ui.player.state.PlayerState
import by.akella.riotplayer.util.TimeUtils
import by.akella.riotplayer.util.animateGone
import by.akella.riotplayer.util.animateVisible
import by.akella.riotplayer.util.collectState
import by.akella.riotplayer.util.loadAlbumIconCircle
import by.akella.riotplayer.util.onSafeClick
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerFragment : BaseFragment() {

    private val viewModel by viewModels<PlayerViewModel>()
    private lateinit var binding: PlayerFragmentBinding
    private val args by lazy { PlayerFragmentArgs.fromBundle(requireArguments()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PlayerFragmentBinding.inflate(inflater, container, false)
        binding.playPause.setOnClickListener { viewModel.onPlayPauseClicked() }
        binding.next.onSafeClick(SafeClickListener<Nothing> { viewModel.next() })
        binding.prev.onSafeClick(SafeClickListener<Nothing> { viewModel.prev() })
        binding.shuffle.onSafeClick(SafeClickListener<Nothing> { viewModel.shuffle() })
        binding.repeat.onSafeClick(SafeClickListener<Nothing> { viewModel.repeat() })
        binding.progressBar.onTouchEnds = { viewModel.seekTo(it * TimeUtils.MILLIS) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.container.collectState(
            viewLifecycleOwner,
            ::renderState
        )
        startPostponedEnterTransition()
        play()
    }

    private fun renderState(state: PlayerState) {
        with(state) {
            song?.let { renderSong(it) }
            renderPositionChanging(currentPlayPosition)
            renderPlayPause(isPlaying)
            renderShuffle(isShuffleEnabled)
            renderRepeat(isRepeatEnabled)
        }
    }

    private fun renderSong(it: SongUiModel) {
        binding.songName.text = it.title
        binding.songArtist.text = it.artist
        binding.albumImage.loadAlbumIconCircle(
            it.albumArtPath,
            R.drawable.ic_album
        ) { postponeEnterTransition() }
        binding.allPlayTime.text =
            TimeUtils.convertMillisToShortTime(requireContext(), it.duration)

        val duration = it.duration / TimeUtils.MILLIS
        binding.progressBar.valueTo = duration.toFloat()
        binding.progressBar.valueFrom = 0f
    }

    private fun renderPositionChanging(nextPosition: Long) {
        binding.progressBar.value = nextPosition / TimeUtils.MILLIS.toFloat()
        binding.currentPlayTime.text =
            TimeUtils.convertMillisToShortTime(
                requireContext(),
                nextPosition
            )
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
            } else {
                binding.repeatBackground.animateGone()
                R.drawable.ic_repeat_inactive
            }
        )
    }

    private fun play() {
        viewModel.play(args.mediaId, MusicType.values()[args.musicType])
    }
}
