package com.goose.player.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.goose.player.R
import com.goose.player.adapter.RecycleTouchListener
import com.goose.player.adapter.SongsListAdapter
import com.goose.player.entity.Song
import com.goose.player.extensions.setGone
import com.goose.player.interfaces.ClickListener
import com.goose.player.utils.FileHelper.getAllAudioFromDevice
import com.goose.player.utils.StorageUtil.storeSongIndex
import com.goose.player.utils.StorageUtil.storeSongList
import kotlinx.android.synthetic.main.fragment_song_list.*
import java.io.Serializable

class SongListFragment : Fragment(), ClickListener, SwipeRefreshLayout.OnRefreshListener,
    View.OnClickListener {

    private var songsList = ArrayList<Song>()
    private var songsListAdapter = SongsListAdapter(ArrayList())
    private var systemMediaController: MediaControllerCompat? = null
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        initRecyclerView()
        showPlayerBtn.setOnClickListener { mainActivity.showPlayer() }
        refresher.setOnRefreshListener(this)
        getMusicBtn.setOnClickListener(this)
        getDataIfHasPermission()
    }

    private fun getDataIfHasPermission() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            getData()
        }
    }

    private fun getData() {
        songsList = getAllAudioFromDevice(context!!)
        songsListAdapter.dataset = songsList
        songsListAdapter.notifyDataSetChanged()
        refresher.isRefreshing = false
        getMusicBtn.setGone()
        storeSongList(context!!, songsList)
    }

    private fun initRecyclerView() {
        songsListRecyclerView.apply {
            addOnItemTouchListener(RecycleTouchListener(this@SongListFragment, context, this))
            setHasFixedSize(true)
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            this.adapter = songsListAdapter
        }
    }

    override fun onClick(view: View, position: Int) {
        showPlayerAndPlaySong(songsList[position])
        storeSongIndex(position, context!!)
    }

    private fun showPlayerAndPlaySong(song: Song) {
        mainActivity.showPlayer()
        val bundle = Bundle()
        bundle.putSerializable("song", song as Serializable)
        systemMediaController?.transportControls?.playFromUri(Uri.parse(song.path), bundle)
    }

    override fun onRefresh() {
        getData()
    }

    override fun onLongClick(view: View, position: Int) {
    }

    override fun onClick(v: View?) {
        getData()
    }

    fun setSystemMediaController(systemMediaController: MediaControllerCompat){
        this.systemMediaController = systemMediaController
    }

}
