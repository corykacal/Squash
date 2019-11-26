package com.example.squash

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.example.squash.api.MainViewModel
import com.example.squash.api.User
import com.example.squash.api.photoapi
import com.example.squash.posts.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.MenuInflater
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.action_bar.*


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    val TAG = "####"


    companion object {
        var newPost = true
        lateinit var viewModel: MainViewModel
    }


    private fun refreshChat(func: (Boolean) -> Unit) {
        viewModel.getChat(100, func)
    }


    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private lateinit var homeFragment: HomeFragment
    fun launchNewFragment(fragment: Fragment, tag: Int) {
        var replaceFrag = supportFragmentManager.findFragmentByTag(tag.toString())
        if(replaceFrag==null) {
            replaceFrag = fragment
            supportFragmentManager
                .beginTransaction()
                // No back stack for home
                .add(R.id.main_frame, replaceFrag, tag.toString())
                // TRANSIT_FRAGMENT_FADE calls for the Fragment to fade away. causes crash
                //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                // No back stack for home
                .replace(R.id.main_frame, replaceFrag, tag.toString())
                // TRANSIT_FRAGMENT_FADE calls for the Fragment to fade away. causes crash
                //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()

        }
    }

    private fun observePoints() {
        viewModel.observeUserData().observe(this, Observer {
            userPoints.text = (it.post_up!!+it.comment_up!!).toString()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<bottomNavBar>(R.id.bar).setInstance(this)
        auth = FirebaseAuth.getInstance()
        var user = User(auth)
        viewModel = MainViewModel()
        viewModel.init(user, photoapi(resources))
        homeFragment = HomeFragment.newInstance()
        launchNewFragment(homeFragment, R.id.posts_icon)
        observePoints()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}
