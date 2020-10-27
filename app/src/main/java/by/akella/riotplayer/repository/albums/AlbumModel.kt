package by.akella.riotplayer.repository.albums

import android.os.Parcel
import android.os.Parcelable

data class AlbumModel(
    val id: String,
    val artUrl: String,
    val name: String,
    val artist: String
) : Parcelable {

    override fun describeContents(): Int = Parcelable.CONTENTS_FILE_DESCRIPTOR

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.let {
            it.writeString(id)
            it.writeString(artUrl)
            it.writeString(name)
            it.writeString(artist)
        }
    }

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<AlbumModel> {

            override fun createFromParcel(source: Parcel?): AlbumModel {
                return AlbumModel(
                    source?.readString() ?: "",
                    source?.readString() ?: "",
                    source?.readString() ?: "",
                    source?.readString() ?: ""
                )
            }

            override fun newArray(size: Int): Array<AlbumModel> = arrayOf()
        }
    }
}
