package by.akella.riotplayer.media

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import by.akella.riotplayer.service.RiotMusicService
import by.akella.riotplayer.util.id
import by.akella.riotplayer.util.toMediaSource
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class PlayerController(
    private val context: Context,
    private val playbackStateHelper: PlaybackStateHelper,
    private val playerListener: Player.Listener
) {

    private val myAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    val player: ExoPlayer by lazy {
        ExoPlayer
            .Builder(context)
            .build()
            .apply {
                setAudioAttributes(myAudioAttributes, true)
                setHandleAudioBecomingNoisy(true)
                addListener(playerListener)
            }
    }

    private val dataFactory: DefaultDataSource.Factory by lazy {
        DefaultDataSource.Factory(context)
    }

    fun playSong(media: MediaMetadataCompat? = null) {
        var playingPosition = player.currentPosition

        media?.let {
            playingPosition = 0L
            val mediaSource = it.toMediaSource(dataFactory)
            player.setMediaSource(mediaSource)
            player.prepare()
        }

        playbackStateHelper.applyPlayState(playingPosition)
        player.playWhenReady = true
    }

    fun pause() {
        if (player.isPlaying) {
            playbackStateHelper.applyPauseState(player.currentPosition)
            player.pause()
        }
    }

    fun stop() {
        if (player.isPlaying) {
            pause()
        }

        playbackStateHelper.applyStopState()
        player.clearMediaItems()
        player.stop()
    }

    fun seekTo(pos: Long) {
        player.seekTo(pos)
        playbackStateHelper.applyPlayState(player.currentPosition)
    }

    fun freePlayer() {
        player.run {
            removeListener(playerListener)
            release()
        }
    }
}
