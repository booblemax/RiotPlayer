package by.akella.riotplayer.repository.songs

data class SongModel(
    val id: Long,
    val title: String,
    val artistId: Long,
    val artist: String,
    val albumId: Long,
    val album: String
)