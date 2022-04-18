package by.akella.riotplayer.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.MainFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.ui.main.state.MainState
import by.akella.riotplayer.ui.main.state.MusicType
import by.akella.riotplayer.ui.player.PlayerMiniFragment
import by.akella.riotplayer.util.collectState
import by.akella.riotplayer.util.onSafeClick
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseFragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            pager.adapter = PagerAdapter(this@MainFragment)
            TabLayoutMediator(tabs, pager) { tab, position ->
                tab.text = getString(MusicType.values()[position].tabName)
            }.attach()
            reloadScan.onSafeClick(SafeClickListener<Nothing> { viewModel.rescan() })
        }

        viewModel.container.collectState(
            viewLifecycleOwner,
            ::renderState
        )
    }

    private fun renderState(state: MainState) {
        if (state.playerConnected && state.playerDisplay) {
            childFragmentManager.findFragmentByTag(PlayerMiniFragment.TAG)?.let { return }
            childFragmentManager.beginTransaction()
                .add(
                    R.id.player_container,
                    PlayerMiniFragment::class.java,
                    null,
                    PlayerMiniFragment.TAG
                )
                .commit()
        } else {
            childFragmentManager.findFragmentByTag(PlayerMiniFragment.TAG)?.let { frag ->
                childFragmentManager.beginTransaction()
                    .remove(frag)
                    .commit()
            }
        }
    }
}
