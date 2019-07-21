package com.goose.player.interfaces

import android.view.View

/**
 *Created by Gxxxse on 20.07.2019.
 */
interface ClickListener {
    fun onClick(view: View, position: Int)
    fun onLongClick(view: View, position: Int)
}