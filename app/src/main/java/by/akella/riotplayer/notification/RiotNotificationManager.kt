package by.akella.riotplayer.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import by.akella.riotplayer.R
import by.akella.riotplayer.util.albumArtUri
import by.akella.riotplayer.util.artist
import by.akella.riotplayer.util.title
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import by.akella.riotplayer.dispatchers.DispatcherProvider
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

const val NOTIFICATION_CHANNEL_ID = "by.akella.riotplayer.media.now_playing"
const val NOW_PLAYING_NOTIFICATION_ID = 0xa447
const val NOTIFICATION_ICON_SIZE = 144

class RiotNotificationManager(
    private val context: Context,
    private val dispatchersProvider: DispatcherProvider,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener
) {

    private val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        error(throwable)
    }
    private val scope = CoroutineScope(job) + exceptionHandler
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager
            .Builder(context, NOW_PLAYING_NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener)
            .build()
            .apply {
                setMediaSessionToken(sessionToken)
                setSmallIcon(R.drawable.ic_musical_note)
            }
    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null

        override fun getCurrentContentTitle(player: Player): CharSequence =
            mediaController.metadata.title.toString()

        override fun getCurrentContentText(player: Player): CharSequence? =
            mediaController.metadata.artist

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            mediaController.sessionActivity

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val iconUri = mediaController.metadata.albumArtUri
            return if (currentIconUri != iconUri || currentBitmap == null) {
                currentIconUri = iconUri
                scope.launch {
                    currentBitmap = retrieveBitmapFromUri(iconUri)
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else {
                currentBitmap
            }
        }

        private suspend fun retrieveBitmapFromUri(uri: Uri): Bitmap? =
            withContext(dispatchersProvider.io()) {
                try {
                    Glide.with(context).applyDefaultRequestOptions(glideOptions)
                        .asBitmap()
                        .load(uri)
                        .submit(NOTIFICATION_ICON_SIZE, NOTIFICATION_ICON_SIZE)
                        .get()
                } catch (ex: Exception) {
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_album,
                        context.theme
                    )?.toBitmap(
                        NOTIFICATION_ICON_SIZE, NOTIFICATION_ICON_SIZE
                    )
                }
            }

        private val glideOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    }
}
