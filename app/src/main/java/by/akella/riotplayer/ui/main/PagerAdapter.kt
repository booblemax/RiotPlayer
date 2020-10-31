package by.akella.riotplayer.ui.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.akella.riotplayer.ui.albums.AlbumsFragment
import by.akella.riotplayer.ui.main.state.MusicType
import by.akella.riotplayer.ui.songs.SongsFragment

class PagerAdapter(parent: Fragment) : FragmentStateAdapter(parent) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AlbumsFragment.create()
            1 -> SongsFragment.create(MusicType.ALL_SONGS)
            else -> SongsFragment.create(MusicType.RECENTS)
        }
    }
}
