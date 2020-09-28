package by.akella.riotplayer.repository.songs

interface SongsRepository {

    suspend fun getSongs(): List<SongModel>
}