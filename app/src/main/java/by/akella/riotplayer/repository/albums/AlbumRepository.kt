package by.akella.riotplayer.repository.albums

interface AlbumRepository {

    suspend fun getAlbums(): List<AlbumModel>

}
