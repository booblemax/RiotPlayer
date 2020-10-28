package by.akella.riotplayer.db

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "songs")
@TypeConverters(UriConverter::class)
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: Long,
    val artist: String,
    val albumId: Long,
    val album: String,
    val albumArt: String,
    val uri: Uri,
    val duration: Long
)