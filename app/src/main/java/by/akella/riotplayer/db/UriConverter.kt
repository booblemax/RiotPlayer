package by.akella.riotplayer.db

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter

class UriConverter {

    @TypeConverter
    fun to(uri: Uri): String = uri.toString()

    @TypeConverter
    fun from(uri: String): Uri = uri.toUri()
}
