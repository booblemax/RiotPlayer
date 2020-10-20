package by.akella.riotplayer.ui.main.state

import by.akella.riotplayer.R

data class MainState(
    val selectedTab: MusicTabs = MusicTabs.ALL_SONGS
) {

    override fun toString(): String {
        return "MainState(selected tab = ${selectedTab.name})"
    }
}

enum class MusicTabs(val tab: Int) {
    ALL_SONGS(R.string.tab_all_songs),
    RECENTS(R.string.tab_recents),
    ALBUMS(R.string.tab_albums)
}
