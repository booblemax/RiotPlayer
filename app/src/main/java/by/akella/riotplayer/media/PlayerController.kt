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
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class PlayerController(
    private val context: Context,
    private val playbackStateHelper: PlaybackStateHelper,
    private val playerListener: Player.EventListener
) {

    private val myAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    val player: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(context).build().apply {
            setAudioAttributes(myAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
    }

    private val dataFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(
            context,
            RiotMusicService.USER_AGENT
        )
    }

    fun playSong(media: MediaMetadataCompat? = null) {
        playbackStateHelper.applyPlayState()

        media?.let {
            val mediaSource = it.toMediaSource(dataFactory)
            player.setMediaSource(mediaSource)
            player.prepare()
        }

        player.playWhenReady = true
    }

    fun pause() {
        if (player.isPlaying) {
            playbackStateHelper.applyPauseState()
            player.pause()
        }
    }

    fun stop() {
        if (player.isPlaying) {
            pause()
        }

        playbackStateHelper.applyStopState()
        player.stop(true)
    }

    fun seekTo(pos: Long) {
        player.seekTo(pos)
    }

    fun freePlayer() {
        player.run {
            removeListener(playerListener)
            release()
        }
    }
}