package com.example.squash.technology

import android.graphics.Canvas
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.squash.MainActivity.Companion.viewModel
import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE





class postTouchHelper(dragDirs: Int, swipeDirs: Int) : ItemTouchHelper.SimpleCallback(dragDirs,
    swipeDirs
) {



    var swipeBack = true


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }


    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if(swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }


    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {

        if (actionState == ACTION_STATE_SWIPE) {
            setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun setTouchListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {

        recyclerView.setOnTouchListener { v, event ->
            swipeBack =
                event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            false
        }
    }

}
