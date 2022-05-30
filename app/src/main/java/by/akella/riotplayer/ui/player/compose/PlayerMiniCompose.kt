package by.akella.riotplayer.ui.player.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.akella.riotplayer.R
import by.akella.riotplayer.ui.player.PlayerViewModel
import coil.compose.AsyncImage

@Composable
fun PlayerMiniCompose(viewModel: PlayerViewModel = viewModel()) {
    val state by viewModel.container.stateFlow.collectAsState()

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colors.primaryVariant)
    ) {
//        LinearProgressIndicator(
//            modifier = Modifier.fillMaxWidth(),
//            progress = state.
//        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .padding(start = 24.dp, top = 8.dp),
                model = state.song?.albumArtPath,
                contentDescription = null,
                error = painterResource(R.drawable.ic_album)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 4.dp, end = 8.dp)
            ) {
                Text(
                    text = "Whore",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = "In this moment",
                    color = colorResource(R.color.colorAccent),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            Image(
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 8.dp),
                painter = painterResource(R.drawable.ic_skip_previous),
                contentDescription = null
            )

            val playPauseDrawableRes =
                if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            Image(
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 8.dp),
                painter = painterResource(id = playPauseDrawableRes),
                contentDescription = null
            )

            Image(
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 48.dp)
                ,
                painter = painterResource(R.drawable.ic_skip_next),
                contentDescription = null
            )
        }
    }
}

@Preview()
@Composable
fun MiniPlayerPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            PlayerMiniCompose()
        }
    }
}