package by.akella.riotplayer.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import by.akella.riotplayer.R
import by.akella.riotplayer.media.QueueManager
import by.akella.riotplayer.media.RiotMediaController
import by.akella.riotplayer.repository.songs.SongModel
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.util.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class RiotMusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var songsRepository: SongsRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private val queueManager = QueueManager()

    private val myAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val playerListener = PlayerEventListener()

    private val player: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().apply {
            audioAttributes = myAudioAttributes
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
    }

    private val dataFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(
            this,
            USER_AGENT
        )
    }

    override fun onCreate() {
        super.onCreate()
        info("RiotMusicService onCreate")

        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        stateBuilder = PlaybackStateCompat.Builder()
        mediaSession = MediaSessionCompat(
            baseContext,
            MY_MEDIA_ID,
            ComponentName(baseContext, MediaButtonReceiver::class.java),
            null
        ).apply {
            setSessionActivity(sessionActivityPendingIntent)

            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            setCallback(MediaSessionCallback())
            isActive = true
        }

        stateBuilder.setActions(
            PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SEEK_TO or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )

        sessionToken = mediaSession.sessionToken
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }

        serviceJob.cancel()

        player.run {
            removeListener(playerListener)
            release()
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("/", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
    }

    private inner class PlayerEventListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    //show notification
                    if (playbackState == Player.STATE_READY) {
                        //store recent played song into preferences

                        if (!playWhenReady) {

//                            stopForeground(false)
                        }
                    }
                }
                else -> {  /* hide notification */
                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            var message = R.string.generic_error;
            when (error.type) {
                // If the data from MediaSource object could not be loaded the Exoplayer raises
                // a type_source error.
                // An error message is printed to UI via Toast message to inform the user.
                ExoPlaybackException.TYPE_SOURCE -> {
                    message = R.string.error_media_not_found;
                    Log.e(TAG, "TYPE_SOURCE: " + error.sourceException.message)
                }
                // If the error occurs in a render component, Exoplayer raises a type_remote error.
                ExoPlaybackException.TYPE_RENDERER -> {
                    Log.e(TAG, "TYPE_RENDERER: " + error.rendererException.message)
                }
                // If occurs an unexpected RuntimeException Exoplayer raises a type_unexpected error.
                ExoPlaybackException.TYPE_UNEXPECTED -> {
                    Log.e(TAG, "TYPE_UNEXPECTED: " + error.unexpectedException.message)
                }
                // Occurs when there is a OutOfMemory error.
                ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                    Log.e(TAG, "TYPE_OUT_OF_MEMORY: " + error.outOfMemoryError.message)
                }
                // If the error occurs in a remote component, Exoplayer raises a type_remote error.
                ExoPlaybackException.TYPE_REMOTE -> {
                    Log.e(TAG, "TYPE_REMOTE: " + error.message)
                }
            }
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()
            info("MediaSessionCallback onPlay")

            mediaSession.isActive = true
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
            mediaSession.setPlaybackState(stateBuilder.build())
            player.playWhenReady = true
            player.play()
        }

        override fun onPause() {
            super.onPause()
            info("MediaSessionCallback onPause")
            pause()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            info("MediaSessionCallback onSkipToNext")
            pause()
            queueManager.skipPositions(1)
            playSong(queueManager.getCurrentSong())
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            info("MediaSessionCallback onSkipToPrevious")
            pause()
            queueManager.skipPositions(-1)
            playSong(queueManager.getCurrentSong())
        }

        override fun onStop() {
            super.onStop()
            info("MediaSessionCallback onStop")
            stop()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            info("MediaSessionCallback onSeekTo to $pos")

            player.seekTo(pos)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            info("onPlayFromMediaId -> $mediaId")

            mediaId?.let {
                val mediaMetadata = songsRepository.getSong(mediaId).toMediaMetadata()
                prepareQueue(mediaMetadata.id ?: "")
                playSong(mediaMetadata)
            }
        }
    }

    private fun playSong(media: MediaMetadataCompat) {
        pause()

        stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.setMetadata(media)

        val mediaSource = media.toMediaSource(dataFactory)
        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
    }

    private fun pause() {
        if (player.isPlaying) {
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, 0, 1f)
            mediaSession.setPlaybackState(stateBuilder.build())
            player.pause()
        }
    }

    private fun stop() {
        if (player.isPlaying) {
            pause()
        }

        stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
        player.stop(true)
    }

    private fun prepareQueue(currMediaId: String = "") {
        if (queueManager.getQueueSize() != 0) return

        serviceScope.launch {
            val songs = songsRepository.getSongs()
            queueManager.setQueue(songs.toMediaMetadata(), currMediaId)
        }
    }

    companion object {
        const val MY_MEDIA_ID = "media_id"
        const val USER_AGENT = "riot.next"
        private const val TAG = "MusicPlayer"
    }
}