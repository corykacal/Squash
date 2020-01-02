package com.example.squash.posts.subContent

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.example.squash.R
import kotlinx.android.synthetic.main.content_main.view.*
import kotlinx.android.synthetic.main.post_fragment.view.*


/*
 * This is an on going experiemnt to set different looking post in the recyclerview
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
