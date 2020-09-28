package by.akella.riotplayer.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.dispatchers.DispatcherProvider
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import by.akella.riotplayer.util.error

abstract class BaseViewModel(protected val dispatcherProvider: DispatcherProvider) : ViewModel() {

    private val job = Job()
    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable -> handleException(throwable) }

    private val baseScope = CoroutineScope(dispatcherProvider.main() + exceptionHandler + job)

    protected fun handleException(throwable: Throwable) {
        error(throwable)
        viewModelScope
    }

    override fun onCleared() {
        baseScope.cancel()
    }
}