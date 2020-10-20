package by.akella.riotplayer.repository.artists

interface ArtistRepository {

    suspend fun getArtists(): List<ArtistModel>

}
