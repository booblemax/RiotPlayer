package by.akella.riotplayer.repository.songs

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import by.akella.riotplayer.util.baseMusicProjection
import by.akella.riotplayer.util.uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SongsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SongsRepository {

    override suspend fun getRecentSongs(): List<SongModel> {
        return getSongs(IS_MUSIC)
    }

    override suspend fun getAllSongs(): List<SongModel> {
        return getSongs(IS_MUSIC)
    }

    override suspend fun getSongsByAlbum(albumId: String): List<SongModel> {
        val selection = "$IS_MUSIC AND ${MediaStore.Audio.AlbumColumns.ALBUM_ID} == $albumId"
        return getSongs(selection)
    }

    override suspend fun getSongsByArtist(artist: String): List<SongModel> {
        val selection = "$IS_MUSIC AND ${MediaStore.Audio.ArtistColumns.ARTIST} == $artist"
        return getSongs(selection)
    }

    private fun getSongs(selection: String): List<SongModel> {
        val songs = mutableListOf<SongModel>()

        context.contentResolver.query(
            uri,
            baseMusicProjection,
            selection,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                do {
                    songs.add(getSongFromCursor(it))
                } while (it.moveToNext())
            }
        } ?: throw NoSuchElementException("Error when loading list of songs")

        return songs
    }

    override fun getSong(id: String): SongModel {
        val selection = "$IS_MUSIC AND ${BaseColumns._ID} == $id"

        context.contentResolver.query(
            uri,
            baseMusicProjection,
            selection,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                return getSongFromCursor(it)
            } else {
                throw NoSuchElementException("Error when loading list of songs")
            }
        } ?: throw NoSuchElementException("Error when loading list of songs")
    }

    private fun getSongFromCursor(cursor: Cursor): SongModel {
        val id = cursor.getString(0)
        val title = cursor.getString(1)
        val albumId = cursor.getLong(2)
        val albumName = cursor.getStringOrNull(3)
        val artistId = cursor.getLong(4)
        val artistName = cursor.getStringOrNull(5)
        val duration = cursor.getLong(6)

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
            ),
            duration
        )
    }

    companion object {
        const val IS_MUSIC =
            MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''"
        const val ALBUM_ART_URI = "content://media/external/audio/albumart"
    }
}
