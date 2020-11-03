package by.akella.riotplayer.media

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    /**
     * LiveData indicates connection with [MediaBrowserCompat]
     */
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> get() = _isConnected

    private val _playbackState = MutableLiveData<PlaybackStateCompat>().apply {
        postValue(EMPTY_PLAYBACK_STATE)
    }
    val playbackState: LiveData<PlaybackStateCompat> get() = _playbackState

    private val _nowPlayingSong = MutableLiveData<MediaMetadataCompat>().apply {
        postValue(NOTHING_TO_PLAY)
    }
    val nowPlayingSong: LiveData<MediaMetadataCompat> get() = _nowPlayingSong

    private val _shuffleMode = MutableLiveData<Boolean>().apply {
        postValue(false)
    }
    val shuffleMode: LiveData<Boolean> get() = _shuffleMode

    private val _repeatMode = MutableLiveData<Boolean>().apply {
        postValue(false)
    }
    val repeatMode: LiveData<Boolean> get() = _repeatMode

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
        if (isConnected.value == true) {
            if (mediaId != null) {
                transportControls.playFromMediaId(mediaId, extra)
            } else {
                transportControls.play()
            }
        }
    }

    fun pause() {
        if (isConnected.value == true) {
            transportControls.pause()
        }
    }

    fun next() {
        if (isConnected.value == true) {
            transportControls.skipToNext()
        }
    }

    fun prev() {
        if (isConnected.value == true) {
            transportControls.skipToPrevious()
        }
    }

    fun seekTo(pos: Long) {
        if (isConnected.value == true) {
            transportControls.seekTo(pos)
        }
    }

    fun setShuffleMode() {
        if (isConnected.value == true) {
            val mode = mediaController.shuffleMode
            transportControls.setShuffleMode(
                if (mode == PlaybackStateCompat.SHUFFLE_MODE_ALL) PlaybackStateCompat.SHUFFLE_MODE_NONE
                else PlaybackStateCompat.SHUFFLE_MODE_ALL
            )
        }
    }

    fun setRepeatMode() {
        if (isConnected.value == true) {
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
            _shuffleMode.postValue(mediaController.shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL)
            _repeatMode.postValue(mediaController.repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL)

            _isConnected.value = true
        }

        override fun onConnectionSuspended() {
            _isConnected.value = false
        }

        override fun onConnectionFailed() {
            _isConnected.value = false
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onRepeatModeChanged(repeatMode: Int) {
            info("${this::class.java.simpleName} Repeat Mode changed on ${repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL}")
            _repeatMode.postValue(repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL)
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            info("${this::class.java.simpleName} Shuffle Mode changed on ${shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL}")
            _shuffleMode.postValue(shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            info("${this::class.java.simpleName} Playback State changed on ${state?.stateName}")
            _playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            info("${this::class.java.simpleName} Metadata State changed on ${metadata?.print()}")
            _nowPlayingSong.postValue(metadata ?: NOTHING_TO_PLAY)
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
