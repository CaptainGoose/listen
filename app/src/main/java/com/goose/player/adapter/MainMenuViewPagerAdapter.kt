package com.goose.player.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.goose.player.view.PlayerFragment
import com.goose.player.view.SongListFragment

/**
 *Created by Gxxxse on 20.07.2019.
 */
class MainMenuViewPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> return PlayerFragment()
            1 -> return SongListFragment()
        }
        return PlayerFragment()
    }

    override fun getCount(): Int {
        return 2
    }
}