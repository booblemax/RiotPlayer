package by.akella.riotplayer.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.util.error
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseViewModel(dispatcherProvider: DispatcherProvider) : ViewModel() {

    private val job = Job()
    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable -> handleException(throwable) }

    protected val baseScope = CoroutineScope(dispatcherProvider.main() + exceptionHandler + job)

    protected fun handleException(throwable: Throwable) {
        error(throwable)
        viewModelScope
    }

    fun runDelayed(timeDelay: Long = TIME_DELAYED_MILLIS, action: () -> Unit) {
        baseScope.launch {
            delay(timeDelay)
            action()
        }
    }

    override fun onCleared() {
        baseScope.cancel()
    }

    companion object {
        private const val TIME_DELAYED_MILLIS = 500L
    }
}
