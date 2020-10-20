package by.akella.riotplayer.repository.albums

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import by.akella.riotplayer.util.uri

class AlbumRepositoryImpl(
    private val context: Context
) : AlbumRepository {

    override suspend fun getAlbums(): List<AlbumModel> {
        val projection = arrayOf(
            MediaStore.Audio.AlbumColumns.ALBUM_ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS,
        )

        val albums = mutableListOf<AlbumModel>()

        context.contentResolver.query(
            uri,
            projection,
            IS_ALBUM,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                do {
                    albums.add(getAlbumFromCursor(it))
                } while(it.moveToNext())
            }
        }

        return albums
    }

    private fun getAlbumFromCursor(cursor: Cursor): AlbumModel {
        val id = cursor.getString(0)
        val album = cursor.getString(1)
        val artist = cursor.getString(2)
        val songCount = cursor.getInt(3)

        return AlbumModel(id, album, artist, songCount)
    }

    companion object {
        const val IS_ALBUM = "${MediaStore.Audio.AlbumColumns.ALBUM} != ''"
    }
}
