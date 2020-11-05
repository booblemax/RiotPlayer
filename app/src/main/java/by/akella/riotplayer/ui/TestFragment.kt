package by.akella.riotplayer.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.akella.riotplayer.databinding.TestFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.util.info
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class TestFragment : BaseFragment() {

    private val viewModel: TestViewModel by viewModels()
    private lateinit var binding: TestFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TestFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.counter.onEach {
//            info(it.toString())
            binding.bar.value = it
        }.launchIn(lifecycleScope)
    }
}