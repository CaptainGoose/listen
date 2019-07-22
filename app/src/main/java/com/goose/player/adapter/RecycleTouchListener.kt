package com.goose.player.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import com.goose.player.interfaces.ClickListener

/**
 *Created by Gxxxse on 20.07.2019.
 */
class RecycleTouchListener(private val clicklistener: ClickListener,
                           context: Context,
                           private val rv: RecyclerView): RecyclerView.OnItemTouchListener {

    private var gestureDetector: GestureDetector

    init {
        val gestureListener = object : GestureDetector.SimpleOnGestureListener(){
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = rv.findChildViewUnder(e.x, e.y)
                if (child != null && clicklistener != null) {
                    clicklistener.onLongClick(child, rv.getChildAdapterPosition(child))
                }
            }
        }
        gestureDetector = GestureDetector(context, gestureListener)

    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val child = rv.findChildViewUnder(e.x, e.y)
        if (child != null && gestureDetector.onTouchEvent(e)) {
            clicklistener.onClick(child, rv.getChildAdapterPosition(child))
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }

    override fun onRequestDisallowInterceptTouchEvent(p0: Boolean) {
    }

}