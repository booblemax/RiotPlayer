package by.akella.riotplayer.ui.splash

sealed class SplashState {
    object Initial : SplashState()
    object Decline : SplashState()
    object Scanned : SplashState()
}
