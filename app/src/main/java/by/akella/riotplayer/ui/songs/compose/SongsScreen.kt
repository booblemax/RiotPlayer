package by.akella.riotplayer.ui.songs.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.akella.riotplayer.R
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.main.state.MusicType
import by.akella.riotplayer.ui.songs.SongsState
import by.akella.riotplayer.ui.songs.SongsViewModel
import by.akella.riotplayer.util.TimeUtils
import coil.compose.AsyncImage

@Composable
fun AllSongsScreen() {
    SongsScreen(songType = MusicType.ALL_SONGS)
}

@Composable
fun RecentsSongsScreen() {
    SongsScreen(songType = MusicType.RECENTS)
}

@Composable
fun SongsScreen(
    songType: MusicType,
    viewModel: SongsViewModel = viewModel(),
    onItemClicked: (SongUiModel) -> Unit = { }
) {
    println(viewModel.toString())
    val songsState by viewModel.stateFlow.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            songsState.loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            songsState.songs.isEmpty() -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = if (songType == MusicType.ALL_SONGS) "No songs" else "No recent",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            else -> {
                SongsList(
                    songsState = songsState,
                    onClearHistoryClicked = { viewModel.clearHistory() },
                    onItemClicked = onItemClicked
                )
            }
        }
    }

    LaunchedEffect(key1 = songType) {
        viewModel.loadSongs(songType)
    }
}

@Composable
private fun SongsList(
    modifier: Modifier = Modifier,
    songsState: SongsState,
    onClearHistoryClicked: () -> Unit = {},
    onItemClicked: (SongUiModel) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (songsState.songType == MusicType.RECENTS) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onClearHistoryClicked() }
                ,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clear History",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Image(
                    modifier = modifier
                        .padding(start = 4.dp)
                        .size(36.dp),
                    painter = painterResource(id = R.drawable.ic_clear),
                    contentDescription = null
                )
            }
        }
        LazyColumn {
            items(
                songsState.songs,
                key = { item -> item.id },
                contentType = { songsState.songType }
            ) {
                SongItem(
                    modifier = Modifier.clickable { onItemClicked(it) },
                    song = it
                )
            }
        }

    }
}

@Composable
fun SongItem(
    modifier: Modifier,
    song: SongUiModel
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
                    .size(48.dp),
                model = song.albumArtPath,
                error = painterResource(id = R.drawable.ic_musical_note),
                contentDescription = null,
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1.0f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = song.artist,
                    fontSize = 16.sp,
                    color = colorResource(R.color.pink_light)
                )
            }
            if (song.duration != 0L) {
                Text(
                    modifier = Modifier.padding(end = 12.dp),
                    text = TimeUtils.convertMillisToShortTime(context, song.duration),
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SongsListPreview() {
    MaterialTheme {
        SongsList(
            songsState = SongsState(
                MusicType.RECENTS,
                true,
                songs = listOf(
                    SongUiModel("id1", "Whore", "In This Moment", duration = 78000),
                    SongUiModel("id2", "Whore", "In This Moment", duration = 78000),
                    SongUiModel("id3", "Whore", "In This Moment", duration = 78000),
                    SongUiModel("id4", "Whore", "In This Moment", duration = 78000),
                    SongUiModel("id5", "Whore", "In This Moment", duration = 78000),
                )
            )
        )
    }
}

@Preview
@Composable
fun ItemPreview() {
    MaterialTheme {
        SongItem(Modifier, SongUiModel("id", "Whore", "In This Moment", duration = 78000))
    }
}