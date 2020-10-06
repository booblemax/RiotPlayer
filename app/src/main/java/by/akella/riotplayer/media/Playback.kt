package by.akella.riotplayer.media

import android.support.v4.media.session.MediaSessionCompat.QueueItem

interface Playback {

    fun stop()

    fun pause()

    fun next()

    fun prev()

    fun setState(state: Int)

    fun getState(): Int

    fun isPlaying(): Boolean

    fun play(item: QueueItem)

    fun seekTo(pos: Int)

    fun currentStreamPosition(): Long
}