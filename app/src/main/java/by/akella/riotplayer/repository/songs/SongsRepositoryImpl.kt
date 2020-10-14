package by.akella.riotplayer.repository.songs

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import by.akella.riotplayer.util.baseMusicProjection
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SongsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SongsRepository {

    private val cache: MutableList<SongModel> = mutableListOf()

    override suspend fun getSongs(force: Boolean): List<SongModel> {
        val songs = mutableListOf<SongModel>()

        if (cache.isEmpty() || force) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            context.contentResolver.query(
                uri,
                baseMusicProjection,
                IS_MUSIC,
                null,
                null
            )?.use {
                if (it.moveToFirst()) {
                    do {
                        songs.add(getSongFromCursor(it))
                    } while (it.moveToNext())
                }

                cache.clear()
                cache.addAll(songs)
            } ?: throw NoSuchElementException("Error when loading list of songs")
        } else {
            songs.addAll(cache)
        }

        return songs
    }

    private fun getSongFromCursor(cursor: Cursor): SongModel {
        val id = cursor.getString(0)
        val title = cursor.getString(1)
        val albumId = cursor.getLong(2)
        val albumName = cursor.getStringOrNull(3)
        val artistId = cursor.getLong(4)
        val artistName = cursor.getStringOrNull(5)

        return SongModel(
            id,
            title,
            artistId,
            artistName ?: "",
            albumId,
            albumName ?: "",
            "$ALBUM_ART_URI/$albumId",
            ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id.toLong()
            )
        )
    }

    override fun getSong(id: String): SongModel {
        return cache.find { it.id == id } ?: throw NoSuchElementException("No element with id $id")
    }

    companion object {
        const val IS_MUSIC =
            MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''"
        const val ALBUM_ART_URI = "content://media/external/audio/albumart"
    }
}
