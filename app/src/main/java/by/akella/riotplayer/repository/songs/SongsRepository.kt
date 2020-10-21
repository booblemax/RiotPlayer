package by.akella.riotplayer.repository.songs

interface SongsRepository {

    suspend fun getAllSongs(): List<SongModel>

    suspend fun getRecentSongs(): List<SongModel>

    suspend fun getSongsByAlbum(albumId: String): List<SongModel>

    suspend fun getSongsByArtist(artist: String): List<SongModel>

    fun getSong(id: String): SongModel
}
