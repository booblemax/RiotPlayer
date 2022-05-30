package by.akella.riotplayer.ui.main.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.akella.riotplayer.R
import by.akella.riotplayer.ui.albums.compose.AlbumsScreen
import by.akella.riotplayer.ui.main.MainViewModel
import by.akella.riotplayer.ui.main.state.MainState
import by.akella.riotplayer.ui.main.state.MusicType
import by.akella.riotplayer.ui.player.compose.PlayerMiniCompose
import by.akella.riotplayer.ui.songs.compose.AllSongsScreen
import by.akella.riotplayer.ui.songs.compose.RecentsSongsScreen
import by.akella.riotplayer.ui.songs.compose.SongsScreen
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun MainScreenCompose(
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.stateFlow.collectAsState()

    MainScreen(state) { viewModel.rescan() }
}

@Composable
private fun MainScreen(
    state: MainState,
    onRescanClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            MainTitle(onRescanClick = onRescanClick)

            val tabNames =
                listOf(
                    stringResource(id = R.string.tab_albums),
                    stringResource(id = R.string.tab_all_songs),
                    stringResource(id = R.string.tab_recents),
                )
            MainPager(tabNames)
        }
        with(state) {
            if (playerConnected && playerDisplay) {
                PlayerMiniCompose()
            }
        }
    }
}

@Composable
private fun MainTitle(
    modifier: Modifier = Modifier,
    onRescanClick: () -> Unit = { }
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.colorPrimary)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp, top = 12.dp, bottom = 12.dp),
            text = stringResource(R.string.main_library),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Image(
            modifier = Modifier
                .padding(end = 16.dp, top = 12.dp, bottom = 12.dp)
                .size(24.dp)
                .clickable { onRescanClick() },
            painter = painterResource(id = R.drawable.ic_round_replay),
            contentDescription = null
        )
    }
}

@Composable
private fun MainPager(tabNames: List<String>) {
    var selectedTab by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        tabNames.forEachIndexed { index, item ->
            OneTab(
                modifier = Modifier.weight(1f),
                name = item,
                isSelected = index == selectedTab,
                onClick = {
                    selectedTab = index
                }
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        when (selectedTab) {
            0 -> AlbumsScreen(onAlbumClick = {})
            1 -> AllSongsScreen()
            2 -> RecentsSongsScreen()
        }
    }
}

@Composable
fun OneTab(modifier: Modifier, name: String, isSelected: Boolean, onClick: ()-> Unit) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var textColorRes = colorResource(id = R.color.colorAccent)
        if (!isSelected) textColorRes = textColorRes.copy(alpha = 0.5f)

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = name,
            color = textColorRes,
            fontSize = 16.sp
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(2.dp)
                    .fillMaxWidth()
                    .background(colorResource(R.color.colorAccent))
            )
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .size(50.dp)
                .background(MaterialTheme.colors.background))
            MainTitle()
            MainPager(
                tabNames = listOf("Albums", "All songs", "Recent")
            )
        }
    }
}

