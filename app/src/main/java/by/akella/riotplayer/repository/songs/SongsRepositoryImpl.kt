package by.akella.riotplayer.repository.songs

import android.content.ContentUris
import android.content.Context
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SongsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SongsRepository {

    private val cache: MutableList<SongModel> = mutableListOf()

    override suspend fun getSongs(force: Boolean): List<SongModel> {
        val songs = mutableListOf<SongModel>()

        if (cache.isEmpty() || force) {
            val baseProjection = arrayOf(
                BaseColumns._ID, // 0
                MediaStore.Audio.AudioColumns.TITLE, // 1
                MediaStore.Audio.AudioColumns.TRACK, // 2
                MediaStore.Audio.AudioColumns.ALBUM_ID, // 5
                MediaStore.Audio.AlbumColumns.ALBUM, // 6
                MediaStore.Audio.AudioColumns.ARTIST_ID, // 7
                MediaStore.Audio.ArtistColumns.ARTIST,// 8
            )


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
                val artistNameIndex = it.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST)
                val albumIdIndex = it.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)
                val albumNameIndex = it.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM)

                if (it.moveToFirst()) {
                    do {
                        val id = it.getString(idIndex)
                        val title = it.getString(titleIndex)
                        val albumId = it.getLong(albumIdIndex)
                        val albumName = it.getStringOrNull(albumNameIndex)
                        val artistId = it.getLong(artistIdIndex)
                        val artistName = it.getStringOrNull(artistNameIndex)

                        songs.add(
                            SongModel(
                                id,
                                title,
                                artistId,
                                artistName ?: "",
                                albumId,
                                albumName ?: "",
                                ContentUris.withAppendedId(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id.toLong()
                                )
                            )
                        )
                    } while (it.moveToNext())
                }

                cache.clear()
                cache.addAll(songs)
            } ?: throw Exception("Error when loading list of songs")
        } else {
            songs.addAll(cache)
        }
        return songs
    }

    override fun getSong(id: String): SongModel {
        return cache.find { it.id == id } ?: throw NoSuchElementException("No element with id $id")
    }

    companion object {
        const val IS_MUSIC =
            MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''"
    }
}