package com.example.squash.posts

import android.graphics.Canvas
import android.media.MediaRouter
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.squash.MainActivity
import com.example.squash.R
import com.example.squash.api.MainViewModel
import com.example.squash.api.User
import com.example.squash.api.photoapi
import com.example.squash.api.posts.Post
import com.example.squash.technology.OnSwipeTouchListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.action_bar.*

class PostFragment: AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var auth: FirebaseAuth

    private lateinit var postAdapter: PostListAdapter


    private fun initDownSwipeLayout(root: View) {
        var refresher = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        refresher.setOnRefreshListener {
            refresher.isRefreshing = false
        }
    }

    private fun initAdapter(root: View) {
        /*
        var recycler = root.findViewById<RecyclerView>(R.id.searchResults)
        postAdapter = PostListAdapter(viewModel, this)
        recycler.adapter = postAdapter
        recycler.layoutManager = LinearLayoutManager(context)

         */


        /*
        old cocde when i was going to have swipe left for favorite.
        but i am going to have swipe left for the changing between
        hot anda new.

        val itemTouchCallback = postTouchHelper(0, ItemTouchHelper.LEFT)


        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recycler)
         */

    }

    private fun initSideSwipes(root: View) {



    }
    private fun initActionBar(actionBar: ActionBar) {
        // Disable the default and enable the custom
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowCustomEnabled(true)
        val customView: View =
            layoutInflater.inflate(R.layout.post_bar, null)
        // Apply the custom view
        actionBar.customView = customView
    }

    private fun setDataObserver(root: View) {
        viewModel.observePosts().observe(this, Observer {
            Log.d("new post bro", "wew $it")
            initAdapter(root)
            postAdapter.submitList(it)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_fragment)
        val toolbar: Toolbar = findViewById(R.id.posttoolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let{
            initActionBar(it)
        }

    }



}
