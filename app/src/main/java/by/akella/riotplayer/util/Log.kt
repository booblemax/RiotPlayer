package by.akella.riotplayer.util

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

fun error(text: String) {
    Timber.e(text)
}
