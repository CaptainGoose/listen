package com.goose.player.adapter

import com.goose.player.controller.MediaPlayerController
import com.goose.player.view.PlayerFragment
import com.goose.player.view.SongListFragment

/**
 *Created by Gxxxse on 20.07.2019.
 */
class MainMenuViewPagerAdapter(fm: androidx.fragment.app.FragmentManager, controller: MediaPlayerController): androidx.fragment.app.FragmentPagerAdapter(fm) {
    private var playerFragment: PlayerFragment = PlayerFragment()
    private var songListFragment: SongListFragment = SongListFragment()

    init {
        playerFragment.setMediaController(controller)
        songListFragment.setMediaController(controller)
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