package com.goose.player.adapter

import android.support.v4.media.session.MediaControllerCompat
import com.goose.player.view.PlayerFragment
import com.goose.player.view.SongListFragment

/**
 *Created by Gxxxse on 20.07.2019.
 */
class MainMenuViewPagerAdapter(fm: androidx.fragment.app.FragmentManager,
                               systemControllerCompat: MediaControllerCompat): androidx.fragment.app.FragmentPagerAdapter(fm) {
    private var playerFragment: PlayerFragment = PlayerFragment()
    private var songListFragment: SongListFragment = SongListFragment()

    init {
        playerFragment.setSystemMediaController(systemControllerCompat)
        songListFragment.setSystemMediaController(systemControllerCompat)
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        when(position){
            0 -> return playerFragment
            1 -> return songListFragment
        }
        return PlayerFragment()
    }

    override fun getCount(): Int {
        return 2
    }
}