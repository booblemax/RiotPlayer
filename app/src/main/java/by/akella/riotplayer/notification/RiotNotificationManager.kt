package by.akella.riotplayer.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import by.akella.riotplayer.R
import by.akella.riotplayer.util.albumArtUri
import by.akella.riotplayer.util.artist
import by.akella.riotplayer.util.title
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import by.akella.riotplayer.dispatchers.DispatcherProvider
import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    private val scope = CoroutineScope(job)
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_channel_name,
            R.string.notification_channel_description,
            NOW_PLAYING_NOTIFICATION_ID,
            DescriptionAdapter(mediaController),
            notificationListener
        ).apply {
            setMediaSessionToken(sessionToken)
            setSmallIcon(R.drawable.ic_musical_note)
            setControlDispatcher(DefaultControlDispatcher(0, 0))
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
                    currentBitmap = retrieveBitmapFromUri(iconUri).apply {
                        callback.onBitmap(this)
                    }
                }
                null
            } else {
                currentBitmap
            }
        }

        private suspend fun retrieveBitmapFromUri(uri: Uri): Bitmap {
            return withContext(dispatchersProvider.io()) {
                Glide.with(context).applyDefaultRequestOptions(glideOptions)
                    .asBitmap()
                    .load(uri)
                    .submit(NOTIFICATION_ICON_SIZE, NOTIFICATION_ICON_SIZE)
                    .get()
            }
        }

        private val glideOptions = RequestOptions()
            .fallback(R.drawable.default_art)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    }
}
