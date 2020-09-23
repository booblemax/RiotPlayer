package by.akella.riotplayer.ui.base

import timber.log.Timber

fun info(text: String) {
    Timber.i(text)
}

fun warn(text: String) {
    Timber.w(text)
}

fun error(error: Throwable) {
    Timber.e(error)
}