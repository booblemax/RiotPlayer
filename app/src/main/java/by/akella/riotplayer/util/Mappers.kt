package by.akella.riotplayer.util

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import by.akella.riotplayer.db.SongEntity
import by.akella.riotplayer.repository.songs.SongModel
import by.akella.riotplayer.ui.base.model.SongUiModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource

fun SongModel.toEntity(): SongEntity {
    return SongEntity(
        id,
        title,
        artistId,
        artist,
        albumId,
        album,
        albumArt,
        uri,
        duration,
        System.currentTimeMillis()
    )
}

fun SongEntity.toModel(): SongModel {
    return SongModel(
        id, title, artistId, artist, albumId, album, albumArt, uri, duration
    )
}

fun SongModel.toMediaMetadata(): MediaMetadataCompat {
    val builder = MediaMetadataCompat.Builder()
    return builder.apply {
        id = this@toMediaMetadata.id
        title = this@toMediaMetadata.title
        artist = this@toMediaMetadata.artist
        album = this@toMediaMetadata.album
        mediaUri = this@toMediaMetadata.uri.toString()
        albumArtUri = this@toMediaMetadata.albumArt
        duration = this@toMediaMetadata.duration
    }.build()
}

fun List<SongModel>.toMediaMetadata(): List<MediaMetadataCompat> {
    val builder = MediaMetadataCompat.Builder()
    return map { song ->
        builder.apply {
            id = song.id
            title = song.title
            artist = song.artist
            mediaUri = song.uri.toString()
            album = song.album
            albumArtUri = song.albumArt
            duration = song.duration
        }.build()
    }
}

fun MediaMetadataCompat.toSongUiModel(): SongUiModel {
    return SongUiModel(
        id ?: "",
        title ?: "",
        artist ?: "",
        albumArtUri.toString(),
        duration
    )
}

/**
 * Extension method for building an [ExtractorMediaSource] from a [MediaMetadataCompat] object.
 *
 * For convenience, place the [MediaDescriptionCompat] into the tag so it can be retrieved later.
 */
fun MediaMetadataCompat.toMediaSource(dataSourceFactory: DataSource.Factory) =
    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mediaUri))

/**
 * Extension method for building a [ConcatenatingMediaSource] given a [List]
 * of [MediaMetadataCompat] objects.
 */
fun List<MediaMetadataCompat>.toMediaSource(
    dataSourceFactory: DataSource.Factory
): ConcatenatingMediaSource {

    val concatenatingMediaSource = ConcatenatingMediaSource()
    forEach {
        concatenatingMediaSource.addMediaSource(it.toMediaSource(dataSourceFactory))
    }
    return concatenatingMediaSource
}
