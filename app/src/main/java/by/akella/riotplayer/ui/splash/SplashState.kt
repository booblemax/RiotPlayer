package by.akella.riotplayer.ui.splash

sealed class SplashState {
    object Initial : SplashState()
    object Granted : SplashState()
    object Decline : SplashState()
}