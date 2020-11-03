package by.akella.riotplayer.media

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import by.akella.riotplayer.util.info
import by.akella.riotplayer.util.print
import by.akella.riotplayer.util.stateName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class RiotMediaController @Inject constructor(
    context: Context,
    serviceComponent: ComponentName
) {

    private val _isConnectedToMediaBrowser = MutableStateFlow(false)
    val isConnectedToMediaBrowser: StateFlow<Boolean> get() = _isConnectedToMediaBrowser

    private val _playbackState = MutableStateFlow<PlaybackStateCompat>(EMPTY_PLAYBACK_STATE)
    val playbackState: StateFlow<PlaybackStateCompat> get() = _playbackState

    private val _nowPlayingSong = MutableStateFlow<MediaMetadataCompat>(NOTHING_TO_PLAY)
    val nowPlayingSong: StateFlow<MediaMetadataCompat> get() = _nowPlayingSong

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> get() = _shuffleMode

    private val _repeatMode = MutableStateFlow(false)
    val repeatMode: StateFlow<Boolean> get() = _repeatMode

    private val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowser = MediaBrowserCompat(
        context,
        serviceComponent,
        MediaBrowserConnectionCallback(context),
        null
    ).apply { connect() }

    private lateinit var mediaController: MediaControllerCompat

    fun play(mediaId: String? = null, extra: Bundle? = null) {
        if (isConnectedToMediaBrowser.value) {
            if (mediaId != null) {
                transportControls.playFromMediaId(mediaId, extra)
            } else {
                transportControls.play()
            }
        }
    }

    fun pause() {
        if (isConnectedToMediaBrowser.value) {
            transportControls.pause()
        }
    }

    fun next() {
        if (isConnectedToMediaBrowser.value) {
            transportControls.skipToNext()
        }
    }

    fun prev() {
        if (isConnectedToMediaBrowser.value) {
            transportControls.skipToPrevious()
        }
    }

    fun seekTo(pos: Long) {
        if (isConnectedToMediaBrowser.value) {
            transportControls.seekTo(pos)
        }
    }

    fun setShuffleMode() {
        if (isConnectedToMediaBrowser.value) {
            val mode = mediaController.shuffleMode
            transportControls.setShuffleMode(
                if (mode == PlaybackStateCompat.SHUFFLE_MODE_ALL) PlaybackStateCompat.SHUFFLE_MODE_NONE
                else PlaybackStateCompat.SHUFFLE_MODE_ALL
            )
        }
    }

    fun setRepeatMode() {
        if (isConnectedToMediaBrowser.value) {
            val mode = mediaController.repeatMode
            transportControls.setRepeatMode(
                if (mode == PlaybackStateCompat.REPEAT_MODE_ALL) PlaybackStateCompat.REPEAT_MODE_NONE
                else PlaybackStateCompat.REPEAT_MODE_ALL
            )
        }
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            info("MediaBrowser connected to MediaBrowserService")
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _shuffleMode.value = mediaController.shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
            _repeatMode.value = mediaController.repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL

            _isConnectedToMediaBrowser.value = true
        }

        override fun onConnectionSuspended() {
            _isConnectedToMediaBrowser.value = false
        }

        override fun onConnectionFailed() {
            _isConnectedToMediaBrowser.value = false
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onRepeatModeChanged(repeatMode: Int) {
            info("${this::class.java.simpleName} Repeat Mode changed on ${repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL}")
            _repeatMode.value = repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            info("${this::class.java.simpleName} Shuffle Mode changed on ${shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL}")
            _shuffleMode.value = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            info("${this::class.java.simpleName} Playback State changed on ${state?.stateName}")
            _playbackState.value = state ?: EMPTY_PLAYBACK_STATE
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            info("${this::class.java.simpleName} Metadata State changed on ${metadata?.print()}")
            _nowPlayingSong.value = metadata ?: NOTHING_TO_PLAY
        }
    }

    companion object {
        const val ARG_MUSIC_TYPE = "arg_music_type"
    }
}

val EMPTY_PLAYBACK_STATE = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

val NOTHING_TO_PLAY = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()
