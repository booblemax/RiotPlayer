package by.akella.riotplayer.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.PlayerFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.main.state.MusicType
import by.akella.riotplayer.util.TimeUtils
import by.akella.riotplayer.util.info
import by.akella.riotplayer.util.loadAlbumIcon
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.container.state.observe(viewLifecycleOwner) { state ->
            info(state.toString())
            with(state) {
                song?.let {
                    if (!isSameSong) {
                        binding.songName.text = it.title
                        binding.songArtist.text = it.artist
                        binding.albumImage.loadAlbumIcon(
                            it.albumArtPath,
                            R.drawable.ic_musical_note
                        )
                        binding.allPlayTime.text =
                            TimeUtils.convertMillisToShortTime(requireContext(), it.duration)

                        val duration = it.duration / TimeUtils.MILLIS
                        binding.progressBar.valueTo = duration
                        binding.progressBar.valueFrom = 0
                    }

                    binding.progressBar.value = currentPlayPosition / TimeUtils.MILLIS
                    binding.currentPlayTime.text =
                        TimeUtils.convertMillisToShortTime(
                            requireContext(),
                            currentPlayPosition
                        )
                }
                binding.playPause.setImageResource(
                    if (isPlaying) R.drawable.ic_pause
                    else R.drawable.ic_play
                )
            }
        }

        viewModel.play(args.mediaId, MusicType.values()[args.musicType])
    }
}
