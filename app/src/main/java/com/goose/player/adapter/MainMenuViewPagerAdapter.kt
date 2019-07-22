package com.goose.player.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.goose.player.MediaPlayerController
import com.goose.player.view.PlayerFragment
import com.goose.player.view.SongListFragment

/**
 *Created by Gxxxse on 20.07.2019.
 */
class MainMenuViewPagerAdapter(fm: FragmentManager, controller: MediaPlayerController): FragmentPagerAdapter(fm) {
    private var playerFragment: PlayerFragment = PlayerFragment()
    private var songListFragment: SongListFragment = SongListFragment()

    init {
        playerFragment.setMediaController(controller)
        songListFragment.setMediaController(controller)
    }

    override fun getItem(position: Int): Fragment {
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