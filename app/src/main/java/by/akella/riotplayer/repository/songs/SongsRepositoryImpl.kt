package by.akella.riotplayer.repository.songs

import android.content.Context
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SongsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SongsRepository {

    override suspend fun getSongs(): List<SongModel> {
        val baseProjection = arrayOf(
            BaseColumns._ID, // 0
            MediaStore.Audio.AudioColumns.TITLE, // 1
            MediaStore.Audio.AudioColumns.TRACK, // 2
            MediaStore.Audio.AudioColumns.YEAR, // 3
            MediaStore.Audio.AudioColumns.DATE_MODIFIED, // 4
            MediaStore.Audio.AudioColumns.ALBUM_ID, // 5
            MediaStore.Audio.AudioColumns.ALBUM, // 6
            MediaStore.Audio.AudioColumns.ARTIST_ID, // 7
            MediaStore.Audio.AudioColumns.ARTIST,// 8
            MediaStore.Audio.AudioColumns.COMPOSER// 9,
        )

        val songs = mutableListOf<SongModel>()

        val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        context.contentResolver.query(
            uri,
            baseProjection,
            IS_MUSIC,
            null,
            null
        )?.use {
            val idIndex = it.getColumnIndex(MediaStore.Audio.AudioColumns._ID)
            val titleIndex = it.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)
            val artistIdIndex = it.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST_ID)
            val artistNameIndex = it.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)
            val albumIdIndex = it.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)
            val albumNameIndex = it.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)

            if (it.moveToFirst()) {
                do {
                    val id = it.getLong(idIndex)
                    val title = it.getString(titleIndex)
                    val albumId = it.getLong(albumIdIndex)
                    val albumName = it.getStringOrNull(albumNameIndex)
                    val artistId = it.getLong(artistIdIndex)
                    val artistName = it.getStringOrNull(artistNameIndex)

                    songs.add(
                        SongModel(
                            id, title, artistId, artistName ?: "", albumId, albumName ?: ""
                        )
                    )
                } while (it.moveToNext())
            }
        } ?: throw Exception("Error when loading list of songs")

        return songs
    }

    companion object {
        const val IS_MUSIC =
            MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''"
    }
}