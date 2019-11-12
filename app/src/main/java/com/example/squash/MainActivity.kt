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
import android.widget.Button
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.action_bar.*


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var homeFragment: HomeFragment

    val TAG = "####"

    companion object {
        lateinit var viewModel: MainViewModel
        var newPost = true
    }

    private fun initActionBar(actionBar: ActionBar) {
        // Disable the default and enable the custom
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowCustomEnabled(true)
        val customView: View =
            layoutInflater.inflate(R.layout.action_bar, null)
        // Apply the custom view
        actionBar.customView = customView
        hotButton.setOnClickListener {
            if(newPost) {
                it.setBackgroundColor(ContextCompat.getColor(it.context, R.color.selectedButton))
                (it as Button).setTextColor(ContextCompat.getColor(it.context, R.color.secondaryYellow))
                newButton.setBackgroundColor(ContextCompat.getColor(it.context, R.color.secondaryYellow))
                newButton.setTextColor(ContextCompat.getColor(it.context, R.color.selectedButton))
                newPost = false
                viewModel.getChat(100)
            }
        }

        newButton.setOnClickListener {
            if (!newPost) {
                it.setBackgroundColor(ContextCompat.getColor(it.context, R.color.selectedButton))
                (it as Button).setTextColor(ContextCompat.getColor(it.context, R.color.secondaryYellow))
                hotButton.setBackgroundColor(ContextCompat.getColor(it.context, R.color.secondaryYellow))
                hotButton.setTextColor(ContextCompat.getColor(it.context, R.color.selectedButton))
                newPost = true
                viewModel.getChat(100)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun initHomeFragment() {
        supportFragmentManager
            .beginTransaction()
            // No back stack for home
            .add(R.id.main_frame, homeFragment)
            // TRANSIT_FRAGMENT_FADE calls for the Fragment to fade away
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let{
            initActionBar(it)
        }
        homeFragment = HomeFragment.newInstance()
        initHomeFragment()
        auth = FirebaseAuth.getInstance()
        var user = User(auth)
        viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]
        viewModel.init(user, photoapi(resources))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}
