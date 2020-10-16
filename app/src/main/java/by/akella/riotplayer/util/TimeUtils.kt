package by.akella.riotplayer.util

import android.content.Context
import by.akella.riotplayer.R
import kotlin.math.floor

object TimeUtils {

    const val MILLIS = 1000
    private const val SECONDS = 60

    fun convertMillisToTime(context: Context, time: Long): String {
        val totalSeconds = floor(time / MILLIS.toDouble()).toInt()
        val minutes = totalSeconds / SECONDS
        val remainingSeconds = totalSeconds - (minutes * SECONDS)
        return if (time < 0) context.getString(R.string.duration_unknown)
        else context.getString(R.string.duration_format).format(minutes, remainingSeconds)
    }
}
