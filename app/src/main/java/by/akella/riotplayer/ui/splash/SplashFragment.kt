package by.akella.riotplayer.ui.splash

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.akella.riotplayer.databinding.SplashComposeFragmentBinding
import by.akella.riotplayer.databinding.SplashFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.util.AppConfig
import by.akella.riotplayer.util.collectState
import by.akella.riotplayer.util.snack
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class SplashFragment : BaseFragment(), EasyPermissions.PermissionCallbacks {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = if (AppConfig.isCompose) {
        SplashComposeFragmentBinding.inflate(inflater, container, false)
            .apply {
                composeLayout.setContent {
                    MdcTheme {
                        SplashCompose(
                            navigate = ::navigateToMain
                        )
                    }
                }
            }
            .root
    } else {
        SplashFragmentBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (AppConfig.isCompose) {
            viewModel.collectState(
                viewLifecycleOwner,
                ::render
            )
            load()
        }
    }

    private fun render(state: SplashState) {
        when (state) {
            is SplashState.Decline -> showDialog()
            is SplashState.Scanned -> navigateToMain()
            else -> {}
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(
            requestCode, permissions, grantResults, this
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_INTERNAL_STORAGE) {
            viewModel.granted()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        viewModel.decline()
    }

    private fun load() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            viewModel.runDelayed(START_DELAY) { viewModel.granted() }
        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to internal storage",
                REQUEST_INTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun navigateToMain() {
        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
    }

    private fun showDialog() {
        view?.snack("Need permission to read music") { onBackPressed() }
    }

    companion object {
        const val REQUEST_INTERNAL_STORAGE = 1000
        private const val START_DELAY = 1000L
    }
}
