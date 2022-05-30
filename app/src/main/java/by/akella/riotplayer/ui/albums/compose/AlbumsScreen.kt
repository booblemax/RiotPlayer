package by.akella.riotplayer.ui.albums.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.akella.riotplayer.R
import by.akella.riotplayer.repository.albums.AlbumModel
import by.akella.riotplayer.ui.albums.AlbumsViewModel
import by.akella.riotplayer.ui.main.state.MusicType
import coil.compose.AsyncImage

@Composable
fun AlbumsScreen(
    viewModel: AlbumsViewModel = viewModel(),
    onAlbumClick: (AlbumModel) -> Unit
) {
    val albumsState by viewModel.stateFlow.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            albumsState.loading ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            albumsState.albums.isEmpty() ->
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "No albums",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            else -> AlbumsList(albums = albumsState.albums, onAlbumClick = onAlbumClick)
        }
    }

    LaunchedEffect(key1 = MusicType.ALBUMS) {
        viewModel.load()
    }
}

@Composable
fun AlbumsList(
    modifier: Modifier = Modifier,
    albums: List<AlbumModel> = emptyList(),
    onAlbumClick: (AlbumModel) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(columns = GridCells.Adaptive(156.dp)) {
            items(
                albums,
                key = { item -> item.id },
                contentType = { item -> item }
            ) {
                AlbumItem(Modifier.clickable { onAlbumClick(it) }, it)
            }
        }
    }
}

@Composable
fun AlbumItem(modifier: Modifier, model: AlbumModel) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model.artUrl,
            contentDescription = null,
            modifier = Modifier.size(140.dp),
            error = painterResource(R.drawable.ic_album)
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = model.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = model.artist,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Preview
@Composable
fun ItemPreview() {
    MaterialTheme {
        AlbumItem(
            Modifier,
            AlbumModel("id", "", "name", "artist")
        )
    }
}