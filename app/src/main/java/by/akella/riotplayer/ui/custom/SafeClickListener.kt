package by.akella.riotplayer.ui.custom

import android.os.SystemClock

class SafeClickListener<T : Any?>(
    private val action: (T) -> Unit
) {

    private var lastClickTime = 0L

    operator fun invoke(item: T) {
        if (SystemClock.elapsedRealtime() - lastClickTime > CLICK_DELAY) {
            action(item)
        }
        lastClickTime = SystemClock.elapsedRealtime()
    }

    companion object {
        private const val CLICK_DELAY = 1000L
    }
}
