package by.akella.riotplayer.util

import android.provider.BaseColumns
import android.provider.MediaStore

val baseMusicProjection = arrayOf(
    BaseColumns._ID, // 0
    MediaStore.Audio.AudioColumns.TITLE, // 1
    MediaStore.Audio.AudioColumns.ALBUM_ID, // 2
    MediaStore.Audio.AlbumColumns.ALBUM, // 3
    MediaStore.Audio.AudioColumns.ARTIST_ID, // 4
    MediaStore.Audio.ArtistColumns.ARTIST, // 5
    MediaStore.Audio.Media.DURATION // 6
)
