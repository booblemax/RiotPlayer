package by.akella.riotplayer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import by.akella.riotplayer.R
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.media.BecomeNoisyReceiver
import by.akella.riotplayer.media.QueueManager
import by.akella.riotplayer.notification.RiotNotificationManager
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.util.error
import by.akella.riotplayer.util.id
import by.akella.riotplayer.util.info
import by.akella.riotplayer.util.toMediaMetadata
import by.akella.riotplayer.util.toMediaSource
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RiotMusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    private val serviceJob = SupervisorJob()
    private lateinit var serviceScope: CoroutineScope

    private lateinit var notificationManager: RiotNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var becomeNoisyReceiver: BecomeNoisyReceiver
    private val queueManager = QueueManager()

    private val myAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val playerListener = PlayerEventListener()

    private val player: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().apply {
            setAudioAttributes(myAudioAttributes, true)
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

    private var isForegroundService = false

    override fun onCreate() {
        super.onCreate()
        info("RiotMusicService onCreate")

        serviceScope = CoroutineScope(dispatcherProvider.main() + serviceJob)

        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        stateBuilder = PlaybackStateCompat.Builder()
        mediaSession = MediaSessionCompat(
            this,
            MY_MEDIA_ID,
            ComponentName(this, MediaButtonReceiver::class.java),
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

        becomeNoisyReceiver = BecomeNoisyReceiver(this, mediaSession.sessionToken)

        notificationManager = RiotNotificationManager(
            this,
            dispatcherProvider,
            mediaSession.sessionToken,
            PlayerNotificationListener()
        )

        notificationManager.showNotification(player)
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

    private fun playSong(media: MediaMetadataCompat? = null) {
        applyPlayState()

        media?.let {
            mediaSession.setMetadata(it)
            val mediaSource = it.toMediaSource(dataFactory)
            player.setMediaSource(mediaSource)
            player.prepare()
        }

        player.playWhenReady = true
    }

    private fun pause() {
        if (player.isPlaying) {
            applyPauseState()
            player.pause()
        }
    }

    private fun stop() {
        if (player.isPlaying) {
            pause()
        }

        applyStopState()
        player.stop(true)
    }

    private fun seekTo(pos: Long) {
        player.seekTo(pos)
    }

    private fun applyPlayState() {
        stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun applyPauseState() {
        stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, player.currentPosition, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun applyStopState() {
        stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun prepareQueue(currMediaId: String = "") {
        if (queueManager.getQueueSize() != 0) return

        serviceScope.launch {
            val songs = songsRepository.getSongs()
            queueManager.setQueue(songs.toMediaMetadata(), currMediaId)
        }
    }

    private inner class PlayerEventListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.showNotification(player)
                    if (playbackState == Player.STATE_READY) {
                        // todo store recent played song into preferences

                        if (playWhenReady) {
                            applyPlayState()
                        } else {
                            applyPauseState()
                            stopForeground(false)
                        }
                    }
                }
                else -> notificationManager.hideNotification()
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            val message = R.string.generic_error
            when (error.type) {
                // If the data from MediaSource object could not be loaded the Exoplayer raises
                // a type_source error.
                // An error message is printed to UI via Toast message to inform the user.
                ExoPlaybackException.TYPE_SOURCE -> {
                    error("TYPE_SOURCE: ${error.sourceException.message}")
                }
                // If the error occurs in a render component, Exoplayer raises a type_remote error.
                ExoPlaybackException.TYPE_RENDERER -> {
                    error("TYPE_RENDERER: ${error.rendererException.message}")
                }
                // If occurs an unexpected RuntimeException Exoplayer raises a type_unexpected error.
                ExoPlaybackException.TYPE_UNEXPECTED -> {
                    error("TYPE_UNEXPECTED: ${error.unexpectedException.message}")
                }
                // Occurs when there is a OutOfMemory error.
                ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                    error("TYPE_OUT_OF_MEMORY: ${error.outOfMemoryError.message}")
                }
                // If the error occurs in a remote component, Exoplayer raises a type_remote error.
                ExoPlaybackException.TYPE_REMOTE -> {
                    error("TYPE_REMOTE: ${error.message}")
                }
                ExoPlaybackException.TYPE_TIMEOUT -> {
                    error("TYPE_TIMEOUT: ${error.message}")
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
            info("MediaSessionCallback onPlay")
            becomeNoisyReceiver.register()
            playSong()
        }

        override fun onPause() {
            info("MediaSessionCallback onPause")
            pause()
        }

        override fun onSkipToNext() {
            info("MediaSessionCallback onSkipToNext")
            queueManager.skipPositions(1)
            playSong(queueManager.getCurrentSong())
        }

        override fun onSkipToPrevious() {
            info("MediaSessionCallback onSkipToPrevious")
            queueManager.skipPositions(-1)
            playSong(queueManager.getCurrentSong())
        }

        override fun onStop() {
            info("MediaSessionCallback onStop")
            becomeNoisyReceiver.unregister()
            stop()
        }

        override fun onSeekTo(pos: Long) {
            info("MediaSessionCallback onSeekTo to $pos")
            seekTo(pos)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            info("onPlayFromMediaId -> $mediaId")

            mediaId?.let {
                try {
                    val mediaMetadata = songsRepository.getSong(mediaId).toMediaMetadata()
                    prepareQueue(mediaMetadata.id ?: "")
                    playSong(mediaMetadata)
                } catch (e: NoSuchElementException) {
                    error(e.message.toString())
                }
            }
        }
    }

    private inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@RiotMusicService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    companion object {
        const val MY_MEDIA_ID = "media_id"
        const val USER_AGENT = "riot.next"
        private const val TAG = "MusicPlayer"
    }
}
