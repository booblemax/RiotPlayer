package by.akella.riotplayer.repository.songs

import android.net.Uri
import android.os.Parcelable
import android.support.v4.media.MediaMetadataCompat
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SongModel(
    val id: String,
    val title: String,
    val artistId: Long,
    val artist: String,
    val albumId: Long,
    val album: String,
    val uri: Uri
) : Parcelable