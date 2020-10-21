package by.akella.riotplayer.ui.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.akella.riotplayer.ui.albums.AlbumsFragment
import by.akella.riotplayer.ui.main.state.MusicTabs
import by.akella.riotplayer.ui.songs.SongsFragment

class PagerAdapter(parent: Fragment) : FragmentStateAdapter(parent) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SongsFragment.create(MusicTabs.ALL_SONGS)
            1 -> SongsFragment.create(MusicTabs.RECENTS)
            else -> AlbumsFragment.create()
        }
    }
}
