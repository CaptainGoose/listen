package com.goose.player.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.goose.player.R
import com.goose.player.adapter.MainMenuViewPagerAdapter
import com.goose.player.entity.Song
import com.goose.player.enums.Pages.PlayerPage
import com.goose.player.enums.Pages.SongsListPage
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainPageViewPager.adapter = MainMenuViewPagerAdapter(supportFragmentManager)
//            var songListFragment = supportFragmentManager.fragments[SongsListPage.number] as SongListFragment
    }

    fun showSongsList(){
        mainPageViewPager.currentItem = SongsListPage.number
    }

    fun showPlayer(){
        mainPageViewPager.currentItem = PlayerPage.number
    }

    fun playSong(song: Song){
        val playerFragment = supportFragmentManager.fragments[PlayerPage.number] as PlayerFragment
        playerFragment.playSong(song)
    }

    fun stopSong(){
        val playerFragment = supportFragmentManager.fragments[PlayerPage.number] as PlayerFragment
        playerFragment.stopSong()
    }

}
