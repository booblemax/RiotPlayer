package by.akella.riotplayer.media

import android.content.ComponentName
import android.content.Context
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import by.akella.riotplayer.util.info
import javax.inject.Inject

class RiotMediaController @Inject constructor(
    context: Context,
    serviceComponent: ComponentName
) {

    /**
     * LiveData indicates connection with [MediaBrowserCompat]
     */
    val isConnected = MutableLiveData<Boolean>().apply { postValue(false) }

    val rootMediaId: String get() = mediaBrowser.root

    val playbackState = MutableLiveData<PlaybackStateCompat>().apply {
        postValue(EMPTY_PLAYBACK_STATE)
    }
    val nowPlayingSong = MutableLiveData<MediaMetadataCompat>().apply {
        postValue(NOTHING_TO_PLAY)
    }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowser = MediaBrowserCompat(
        context,
        serviceComponent,
        MediaBrowserConnectionCallback(context),
        null
    ).apply { connect() }

    private lateinit var mediaController: MediaControllerCompat

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    fun play(mediaId: String? = null) {
        if (isConnected.value == true) {
            mediaController.sendCommand("PLAY", null ,null)
            if (mediaId != null) {
                transportControls.playFromMediaId(mediaId, null)
            } else {
                transportControls.play()
            }
        }
        info("Playback state ${playbackState.value}")
    }

    fun pause() {
        if (isConnected.value == true) {
            transportControls.pause()
        }
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            info("MediaBrowser connected to MediaBrowserService")
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                info("Register MediaControllerCallback")
                registerCallback(MediaControllerCallback())
            }

            isConnected.postValue(true)
        }

        override fun onConnectionSuspended() {
            isConnected.postValue(false)
        }

        override fun onConnectionFailed() {
            isConnected.postValue(false)
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            info("Playback State changed on $state")
            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            info("Metadata State changed on $metadata")
            nowPlayingSong.postValue(metadata ?: NOTHING_TO_PLAY)
        }
    }

}

val EMPTY_PLAYBACK_STATE = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

val NOTHING_TO_PLAY = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()