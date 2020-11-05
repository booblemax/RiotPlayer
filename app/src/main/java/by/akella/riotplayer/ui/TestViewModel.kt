package by.akella.riotplayer.ui

import androidx.hilt.lifecycle.ViewModelInject
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.ui.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow

class TestViewModel @ViewModelInject constructor(dispatcherProvider: DispatcherProvider) :
    BaseViewModel(dispatcherProvider) {

    val counter = flow<Long> {
        var counter = 0L
        do {
            emit(counter)
            counter++
            delay(500L)
        } while (counter <= 100)
    }

}