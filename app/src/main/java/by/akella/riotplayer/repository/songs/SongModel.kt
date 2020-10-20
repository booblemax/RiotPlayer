package by.akella.riotplayer.repository.songs

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import by.akella.riotplayer.util.toUri

data class SongModel(
    val id: String,
    val title: String,
    val artistId: Long,
    val artist: String,
    val albumId: Long,
    val album: String,
    val albumArt: String,
    val uri: Uri,
    val duration: Long
) : Parcelable {

    override fun describeContents(): Int = Parcelable.CONTENTS_FILE_DESCRIPTOR

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.let {
            it.writeString(id)
            it.writeString(title)
            it.writeLong(artistId)
            it.writeString(artist)
            it.writeLong(albumId)
            it.writeString(album)
            it.writeString(albumArt)
            it.writeString(uri.toString())
            it.writeLong(duration)
        }
    }

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<SongModel> {
            override fun createFromParcel(source: Parcel?): SongModel = SongModel(
                source?.readString() ?: "",
                source?.readString() ?: "",
                source?.readLong() ?: 0L,
                source?.readString() ?: "",
                source?.readLong() ?: 0L,
                source?.readString() ?: "",
                source?.readString() ?: "",
                source?.readString().toUri(),
                source?.readLong() ?: 0L,
            )

            override fun newArray(size: Int): Array<SongModel> = arrayOf()
        }
    }
}
