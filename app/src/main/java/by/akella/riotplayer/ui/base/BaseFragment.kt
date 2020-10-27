package by.akella.riotplayer.ui.base

import android.os.Bundle
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

abstract class BaseFragment : Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) { onBackPressed() }
    }

    open fun onBackPressed(@IdRes popTo: Int? = null) {
        if (!(popTo?.let { findNavController().popBackStack(it, false) }
                ?: findNavController().popBackStack())) {
            requireActivity().finish()
        }
    }
}
