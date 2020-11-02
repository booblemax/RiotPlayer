package by.akella.riotplayer.media

import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class PlaybackStateHelper(
    private val mediaSession: MediaSessionCompat
) {

    private val stateBuilder: PlaybackStateCompat.Builder by lazy {
        PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
    }

    fun applyPlayState(position: Long = 0) {
        stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, position, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    fun applyPauseState(position: Long = 0) {
        stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, position, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    fun applyStopState() {
        stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    fun applySkipNextState() {
        stateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, 0, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    fun applySkipPreviousState() {
        stateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS, 0, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }
}