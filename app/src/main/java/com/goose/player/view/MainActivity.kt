package com.goose.player.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.goose.player.MediaPlayerController
import com.goose.player.R
import com.goose.player.adapter.MainMenuViewPagerAdapter
import com.goose.player.enums.Pages.PlayerPage
import com.goose.player.enums.Pages.SongsListPage
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val controller = MediaPlayerController(this)
        mainPageViewPager.adapter = MainMenuViewPagerAdapter(supportFragmentManager, controller)
    }

    fun showSongsList(){
        mainPageViewPager.currentItem = SongsListPage.number
    }

    fun showPlayer(){
        mainPageViewPager.currentItem = PlayerPage.number
    }

}
