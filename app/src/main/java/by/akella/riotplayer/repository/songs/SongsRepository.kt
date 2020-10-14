package by.akella.riotplayer.repository.songs

interface SongsRepository {

    suspend fun getSongs(force: Boolean = false): List<SongModel>

    fun getSong(id: String): SongModel
}
