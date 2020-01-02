package com.example.squash.posts.subContent

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

/*
 * This is an on going experiemnt to set different looking post in the post row.
 * I would set images or other post in a retweet style or gifs/videos
 */

class ContentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var viewToDraw: ImageView? = null
        set(value) {
            field = value
            background = value?.background
            invalidate()
        }

    override fun onDraw(canvas: Canvas?) {
        viewToDraw?.draw(canvas)
    }
}
