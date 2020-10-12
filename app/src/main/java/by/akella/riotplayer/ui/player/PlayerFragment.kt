package by.akella.riotplayer.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.PlayerFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.util.warn
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
        viewModel.currentMediaId = args.mediaId

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.container.state.observe(viewLifecycleOwner) { state ->
            warn("$state")
            with(state) {
                song?.let {
                    binding.songName.text = it.title
                    binding.songArtist.text = it.artist
                }
                binding.playPause.setImageResource(
                    if (isPlaying) R.drawable.ic_pause
                    else R.drawable.ic_play
                )
            }
        }

        viewModel.play()
    }
}