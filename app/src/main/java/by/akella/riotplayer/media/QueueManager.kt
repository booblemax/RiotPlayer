package by.akella.riotplayer.media

import android.support.v4.media.MediaMetadataCompat
import by.akella.riotplayer.util.id
import javax.inject.Inject
import kotlin.random.Random

class QueueManager @Inject constructor() {

    private var currentIndex = 0
    private val playingQueue: MutableList<MediaMetadataCompat> = mutableListOf()

    var shuffleEnabled: Boolean = false
    var repeatEnabled: Boolean = false

    fun setCurrentSong(mediaId: String) {
        val index =
            if (mediaId.isEmpty()) 0
            else playingQueue.indexOfFirst { it.id == mediaId }

        setCurrentIndex(index)
    }

    private fun setCurrentIndex(index: Int) {
        if (index >= 0 && index < playingQueue.size) {
            currentIndex = index
        } else throw IllegalArgumentException("Wrong index value $index")
    }

    fun skipPositions(count: Int): MediaMetadataCompat? {
        val index = generateNextIndex(count)
        setCurrentIndex(index)
        return getCurrentSong()
    }

    fun getCurrentSong(): MediaMetadataCompat? = playingQueue.getOrNull(currentIndex)

    fun getQueueSize() = playingQueue.size

    fun setQueue(queue: List<MediaMetadataCompat>, initialMediaId: String) {
        playingQueue.clear()
        playingQueue.addAll(queue)

        setCurrentSong(initialMediaId)
    }

    private fun generateNextIndex(count: Int): Int {
        var index =
            if (shuffleEnabled) Random.Default.nextInt(0, getQueueSize())
            else getQueueSize() + count

        if (repeatEnabled && index >= getQueueSize()) {
            index %= playingQueue.size
        }

        return index
    }
}
