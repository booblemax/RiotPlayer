package by.akella.riotplayer.repository.artists

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import by.akella.riotplayer.util.uri

class ArtistRepositoryImpl(
    private val context: Context
) : ArtistRepository {

    override suspend fun getArtists(): List<ArtistModel> {
        val projection = arrayOf(
            MediaStore.Audio.ArtistColumns.ARTIST,
        )

        val artists = mutableListOf<ArtistModel>()

        context.contentResolver.query(
            uri,
            projection,
            IS_ARTIST,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                do {
                    artists.add(getArtistFromCursor(it))
                } while(it.moveToNext())
            }
        }

        return artists
    }

    private fun getArtistFromCursor(cursor: Cursor): ArtistModel {
        val artist = cursor.getString(0)

        return ArtistModel(artist)
    }

    companion object {
        const val IS_ARTIST = "${MediaStore.Audio.ArtistColumns.ARTIST} != ''"
    }
}
