package by.akella.riotplayer.media

import android.support.v4.media.session.MediaSessionCompat

class QueueManager {

    private var currentIndex = 0
    private val playingQueue: MutableList<MediaSessionCompat.QueueItem> = mutableListOf()

    private fun setCurrentIndex(index: Int) {
        if (index > 0 && index < playingQueue.size) {
            currentIndex = index
        }
    }

    fun setCurrentQueueItem(queueId: Long) {
        val index = playingQueue.indexOfFirst { it.queueId == queueId }
        setCurrentIndex(index)
    }

    fun setCurrentQueueItem(mediaId: String) {
        val index = playingQueue.indexOfFirst { it.description.mediaId == mediaId }
        setCurrentIndex(index)
    }

    fun skipPositions(count: Int) {
        var index = currentIndex + count
        if (index < 0) {
            index = 0
        } else {
            index %= playingQueue.size
        }

        setCurrentIndex(index)
    }

    fun getCurrentSong(): MediaSessionCompat.QueueItem = playingQueue[currentIndex]

    fun getQueueSize() = playingQueue.size

    fun setQueue(queue: List<MediaSessionCompat.QueueItem>, initialMediaId: String = "") {
        playingQueue.clear()
        playingQueue.addAll(queue)

        val index =
            if (initialMediaId.isEmpty()) 0
            else playingQueue.indexOfFirst { it.description.mediaId == initialMediaId }

        setCurrentIndex(index)
    }
}