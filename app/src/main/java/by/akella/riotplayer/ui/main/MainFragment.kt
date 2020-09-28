package by.akella.riotplayer.ui.main

import android.Manifest
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.MainFragmentBinding
import by.akella.riotplayer.scanner.SingleMediaScanner
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.main.state.MainSideEffect
import by.akella.riotplayer.ui.main.state.MainState
import by.akella.riotplayer.util.*
import com.babylon.orbit2.livedata.sideEffect
import com.babylon.orbit2.livedata.state
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class MainFragment : BaseFragment(), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainFragmentBinding
    private lateinit var adapter: MainAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init recycler
        adapter = MainAdapter { view.snack("${it.title} song clicked")}
        with(binding.localSongs) {
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val currItemPosition = parent.getChildAdapterPosition(view)
                    val lastPosition = parent.adapter?.itemCount ?: 0
                    if (currItemPosition == lastPosition) {
                        outRect.bottom = resources.getDimensionPixelOffset(R.dimen.size_8)
                    }
                }
            })
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@MainFragment.adapter
        }

        viewModel.container.state.observe(viewLifecycleOwner) { render(it) }
        viewModel.container.sideEffect.observe(viewLifecycleOwner) { processSideEffect(it) }
    }

    private fun render(state: MainState) {
        with(state) {
            if (loading) {
                binding.progress.visible()
                binding.content.gone()
            } else {
                binding.progress.gone()
                binding.content.animateVisible()
            }

            adapter.submitList(songs)
        }
        error(state.toString())
    }

    private fun processSideEffect(sideEffect: MainSideEffect) {
        if (sideEffect is MainSideEffect.ScanFiles) {
            val scanner = SingleMediaScanner(requireContext())
            scanner.onScanComplete = { load() }
            scanner.scan()
        }
    }

    private fun load() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )) {
            viewModel.loadSongs()
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

    companion object {
        const val REQUEST_INTERNAL_STORAGE = 1000
    }
}