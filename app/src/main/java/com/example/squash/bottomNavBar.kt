package com.example.squash

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.example.squash.events.EventFragment
import com.example.squash.peek.PeekFragment
import com.example.squash.posts.HomeFragment
import com.example.squash.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class bottomNavBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private lateinit var main: MainActivity

    fun setInstance(mainActivity: MainActivity) {
        main = mainActivity
    }


    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)

        this.setOnNavigationItemSelectedListener {
            val newSelectId = it.itemId
            val prevSelectId = this.selectedItemId
            if(newSelectId!=prevSelectId) {
                val new_frag = when (newSelectId) {
                    R.id.profile_icon -> ProfileFragment.newInstance()
                    R.id.posts_icon -> HomeFragment.newInstance()
                    R.id.peek_icon -> PeekFragment.newInstance()
                    R.id.events_icon -> EventFragment.newInstance()
                    else -> HomeFragment.newInstance()
                }
                main.launchNewFragment(new_frag, newSelectId)
            }

            true
        }
    }







}