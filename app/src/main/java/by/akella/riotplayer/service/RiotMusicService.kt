package by.akella.riotplayer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import by.akella.riotplayer.R
import by.akella.riotplayer.dispatchers.DispatcherProvider
import by.akella.riotplayer.media.BecomeNoisyReceiver
import by.akella.riotplayer.media.PlaybackStateHelper
import by.akella.riotplayer.media.PlayerController
import by.akella.riotplayer.media.QueueManager
import by.akella.riotplayer.media.RiotMediaController
import by.akella.riotplayer.notification.RiotNotificationManager
import by.akella.riotplayer.repository.songs.SongModel
import by.akella.riotplayer.repository.songs.SongsRepository
import by.akella.riotplayer.ui.main.state.MusicType
import by.akella.riotplayer.util.album
import by.akella.riotplayer.util.error
import by.akella.riotplayer.util.id
import by.akella.riotplayer.util.info
import by.akella.riotplayer.util.toMediaMetadata
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
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

    private lateinit var playbackStateHelper: PlaybackStateHelper
    private lateinit var playerController: PlayerController
    private lateinit var notificationManager: RiotNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var becomeNoisyReceiver: BecomeNoisyReceiver

    private val queueManager = QueueManager()
    private var musicType: MusicType? = null

    private val playerListener = PlayerEventListener()

    private var isForegroundService = false

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(dispatcherProvider.main() + serviceJob)

        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

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
        sessionToken = mediaSession.sessionToken

        playbackStateHelper = PlaybackStateHelper(mediaSession)
        playerController = PlayerController(this, playbackStateHelper, playerListener)

        becomeNoisyReceiver = BecomeNoisyReceiver(this, mediaSession.sessionToken)
        notificationManager = RiotNotificationManager(
            this,
            dispatcherProvider,
            mediaSession.sessionToken,
            PlayerNotificationListener()
        )
        notificationManager.showNotification(playerController.player)
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }

        serviceJob.cancel()
        playerController.freePlayer()
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

    private fun prepareQueue(songModel: SongModel) {
        serviceScope.launch(dispatcherProvider.io()) {
            val songs = when (musicType) {
                MusicType.ALBUMS -> songsRepository.getSongsByAlbum(songModel.albumId.toString())
                MusicType.RECENTS -> songsRepository.getRecentSongs()
                MusicType.ALL_SONGS -> songsRepository.getAllSongs()
                else -> emptyList()
            }
            queueManager.setQueue(songs.toMediaMetadata(), songModel.id)
        }
    }

    private fun saveSongIntoHistory(songId: String?) {
        songId?.let {
            serviceScope.launch(dispatcherProvider.io()) {
                songsRepository.insertSongToRecent(songId)
            }
        }
    }

    private inner class PlayerEventListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.showNotification(playerController.player)
                    if (playbackState == Player.STATE_READY) {
                        if (playWhenReady) {
                            stopForeground(false)
                        }
                    }
                }
                Player.STATE_ENDED ->
                    try {
                        queueManager.skipPositions(1)?.let { media ->
                            mediaSession.setMetadata(media)
                            saveSongIntoHistory(media.id)
                            playerController.playSong(media)
                        }
                    } catch (e: IllegalArgumentException) {
                        error(e.message ?: RiotMusicService::class.java.simpleName)
                        playerController.stop()
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

        override fun onSetShuffleMode(shuffleMode: Int) {
            queueManager.shuffleEnabled = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
            mediaSession.setShuffleMode(shuffleMode)
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            queueManager.repeatEnabled = repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL
            mediaSession.setRepeatMode(repeatMode)
        }

        override fun onPlay() {
            info("MediaSessionCallback onPlay")
            becomeNoisyReceiver.register()
            playerController.playSong()
        }

        override fun onPause() {
            info("MediaSessionCallback onPause")
            playerController.pause()
        }

        override fun onSkipToNext() {
            info("MediaSessionCallback onSkipToNext")
            playbackStateHelper.applyStopState()
            skipAndPlay(1)
        }

        override fun onSkipToPrevious() {
            info("MediaSessionCallback onSkipToPrevious")
            skipAndPlay(-1)
        }

        private fun skipAndPlay(count: Int) {
            try {
                queueManager.skipPositions(count)?.let { media ->
                    mediaSession.setMetadata(media)
                    saveSongIntoHistory(media.id)
                    playerController.playSong(media)
                }
            } catch (e: IllegalArgumentException) {
                error(e.message ?: RiotMusicService::class.java.simpleName)
                playerController.stop()
            }
        }

        override fun onStop() {
            info("MediaSessionCallback onStop")
            becomeNoisyReceiver.unregister()
            playerController.stop()
        }

        override fun onSeekTo(pos: Long) {
            info("MediaSessionCallback onSeekTo to $pos")
            playerController.seekTo(pos)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            info("onPlayFromMediaId -> $mediaId")

            val musicType =
                extras?.getSerializable(RiotMediaController.ARG_MUSIC_TYPE) as? MusicType

            if (musicType == this@RiotMusicService.musicType &&
                queueManager.getCurrentSong()?.id == mediaId
            ) {
                playerController.playSong()
            } else {
                mediaId?.let {
                    try {
                        val songModel = songsRepository.getSong(mediaId)
                        val currentSong = queueManager.getCurrentSong()

                        if (musicType != this@RiotMusicService.musicType ||
                            currentSong?.album != songModel.album
                        ) {
                            this@RiotMusicService.musicType = musicType
                            prepareQueue(songModel)
                        } else {
                            queueManager.setCurrentSong(it)
                        }

                        val mediaMetadata = songModel.toMediaMetadata()
                        mediaSession.setMetadata(mediaMetadata)
                        playerController.playSong(mediaMetadata)
                    } catch (e: NoSuchElementException) {
                        error(e.message.toString())
                    }
                }
            }
        }
    }

    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {

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
