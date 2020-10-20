package by.akella.riotplayer.util

import android.provider.BaseColumns
import android.provider.MediaStore

val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
} else {
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
}

val baseMusicProjection = arrayOf(
    BaseColumns._ID, // 0
    MediaStore.Audio.AudioColumns.TITLE, // 1
    MediaStore.Audio.AudioColumns.ALBUM_ID, // 2
    MediaStore.Audio.AlbumColumns.ALBUM, // 3
    MediaStore.Audio.AudioColumns.ARTIST_ID, // 4
    MediaStore.Audio.ArtistColumns.ARTIST, // 5
    MediaStore.Audio.Media.DURATION // 6
)
