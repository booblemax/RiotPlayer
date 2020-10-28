package by.akella.riotplayer.ui.main.state

import by.akella.riotplayer.R

data class MainState(
    val selectedTab: MusicTabs = MusicTabs.ALBUMS
) {

    override fun toString(): String {
        return "MainState(selected tab = ${selectedTab.name})"
    }
}

enum class MusicTabs(val tabName: Int) {
    ALBUMS(R.string.tab_albums),
    ALL_SONGS(R.string.tab_all_songs),
    RECENTS(R.string.tab_recents)
}
