package by.akella.riotplayer.util

import android.content.Context
import by.akella.riotplayer.R
import kotlin.math.floor

object TimeUtils {

    const val MILLIS = 1000
    private const val SECONDS = 60

    fun convertMillisToShortTime(context: Context, time: Long): String {
        val totalSeconds = floor(time / MILLIS.toDouble()).toInt()
        val minutes = totalSeconds / SECONDS
        val remainingSeconds = totalSeconds - (minutes * SECONDS)
        return if (time < 0) context.getString(R.string.duration_unknown)
        else context.getString(R.string.duration_format).format(minutes, remainingSeconds)
    }

    fun convertMillisToMinutes(context: Context, time: Long): String {
        val seconds = floor(time / MILLIS.toDouble()).toInt()
        val minutes = seconds / SECONDS
        return context.resources.getQuantityString(
            R.plurals.duration_songs_minutes,
            minutes,
            minutes
        )
    }
}
