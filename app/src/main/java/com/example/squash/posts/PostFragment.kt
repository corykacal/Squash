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
import kotlinx.android.synthetic.main.post_fragment.*
import java.util.*

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

    private fun setMainPost(post: Post) {
        contents.text = post.contents
        comments.text = post.comment_count.toString()
        timeStamp.text = viewModel.getTime(Date(post.timestamp!!.time))
    }

    private fun observeMainPost(post_number: Long) {
        viewModel.observeSinglePost().observe(this, Observer {
            setMainPost(it)
        })
    }

    private fun initActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.posttoolbar)
        toolbar.setTitle(title)
        toolbar.setNavigationIcon(R.drawable.back_arrow)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
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

        viewModel = MainActivity.viewModel
        val intent = intent
        val post_number = intent.getLongExtra("post_number", 0)

        observeMainPost(post_number)
        viewModel.getSinglePost(post_number)

        initActionBar()


    }



}
