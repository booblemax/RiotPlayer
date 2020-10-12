package by.akella.riotplayer.media

import android.support.v4.media.MediaMetadataCompat
import by.akella.riotplayer.util.id
import javax.inject.Inject

class QueueManager @Inject constructor() {

    private var currentIndex = 0
    private val playingQueue: MutableList<MediaMetadataCompat> = mutableListOf()

    private fun setCurrentIndex(index: Int) {
        if (index > 0 && index < playingQueue.size) {
            currentIndex = index
        }
    }

    fun setCurrentQueueItem(mediaId: String) {
        val index = playingQueue.indexOfFirst { it.id == mediaId }
        setCurrentIndex(index)
    }

    fun skipPositions(count: Int) {
        var index = currentIndex + count
        if (index < 0 || playingQueue.isEmpty()) {
            index = 0
        }
        if (index > playingQueue.size) {
            index %= playingQueue.size
        }

        setCurrentIndex(index)
    }

    fun getCurrentSong(): MediaMetadataCompat = playingQueue[currentIndex]

    fun getQueueSize() = playingQueue.size

    fun setQueue(queue: List<MediaMetadataCompat>, initialMediaId: String = "") {
        playingQueue.clear()
        playingQueue.addAll(queue)

        val index =
            if (initialMediaId.isEmpty()) 0
            else playingQueue.indexOfFirst { it.id == initialMediaId }

        setCurrentIndex(index)
    }
}