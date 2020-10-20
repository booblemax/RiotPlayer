package by.akella.riotplayer.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat
import by.akella.riotplayer.util.Versions
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ExoPlayerPlayback @Inject constructor(
    @ApplicationContext private val context: Context
) : Playback {

    private val exoPlayer: SimpleExoPlayer? = null
    private val mediaProvider: MediaProvider? = null
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var currentAudioFocusState: Int = AUDIO_NO_FOCUS_NO_DUCK
    private var playbackDelayed: Boolean = false
    private var playOnFocusGain: Boolean = false

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun next() {
        TODO("Not yet implemented")
    }

    override fun prev() {
        TODO("Not yet implemented")
    }

    override fun setState(state: Int) {
        TODO("Not yet implemented")
    }

    override fun getState(): Int {
        TODO("Not yet implemented")
    }

    override fun isPlaying(): Boolean {
        TODO("Not yet implemented")
    }

    override fun play(item: MediaSessionCompat.QueueItem) {

        tryGetAudioFocus()

        // get media source
        // ass media source to exo player
    }

    override fun seekTo(pos: Int) {
        TODO("Not yet implemented")
    }

    override fun currentStreamPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    private fun tryGetAudioFocus() {
        val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
            when (it) {
                AudioManager.AUDIOFOCUS_GAIN -> currentAudioFocusState = AUDIO_FOCUSED
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                    // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
                    currentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Lost audio focus, but will gain it back (shortly), so note whether
                    // playback should resume
                    currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
                    playOnFocusGain = exoPlayer != null && exoPlayer.playWhenReady
                }
                AudioManager.AUDIOFOCUS_LOSS -> currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
            }

            exoPlayer?.let { updatePlayerState() }
        }

        val result = if (Versions.isOreoOrUp()) {
            val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(audioFocusChangeListener)
            }.build()

            audioManager.requestAudioFocus(audioFocusRequest)
        } else {
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        when (result) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED ->
                currentAudioFocusState = AUDIO_FOCUSED
            AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                currentAudioFocusState = AUDIO_FOCUSED
                playbackDelayed = true
            }
            else -> currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    private fun updatePlayerState() {
        if (currentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
            pause()
        } else {

            exoPlayer?.volume = if (currentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
                VOLUME_DUCK
            } else {
                VOLUME_NORMAL
            }

            if (playOnFocusGain) {
                exoPlayer?.playWhenReady = true
                playOnFocusGain = false
            }
        }
    }

    companion object {
        const val VOLUME_DUCK = 0.2f
        const val VOLUME_NORMAL = 1.0f

        private const val AUDIO_NO_FOCUS_NO_DUCK = 0
        private const val AUDIO_NO_FOCUS_CAN_DUCK = 1
        private const val AUDIO_FOCUSED = 2
    }
}
