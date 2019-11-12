package com.example.squash.posts

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.media.Image
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
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.post_fragment.*
import kotlinx.android.synthetic.main.post_fragment.searchResults
import okhttp3.internal.wait
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

    private fun initAdapter() {
        var recycler = findViewById<RecyclerView>(R.id.searchResults)
        postAdapter = PostListAdapter(viewModel, null, true)
        recycler.adapter = postAdapter
        recycler.layoutManager = LinearLayoutManager(this)

        /*
        old cocde when i was going to have swipe left for favorite.
        but i am going to have swipe left for the changing between
        hot anda new.

        val itemTouchCallback = postTouchHelper(0, ItemTouchHelper.LEFT)


        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recycler)
         */

    }

    private fun initDownSwipeLayout(post_number: Long) {
        var refresher = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        refresher.setOnRefreshListener {
            viewModel.getComments(post_number)
            viewModel.getSinglePost(post_number)
            refresher.isRefreshing = false
        }
    }

    private fun updatePointColors(points: Int) {
        var pointsTV = findViewById<TextView>(R.id.points)
        pointsTV.text = points.toString()
        if(points<0) {
            pointsTV.setTextColor(ContextCompat.getColor(pointsTV.context, R.color.badComment))
        } else {
            pointsTV.setTextColor(ContextCompat.getColor(pointsTV.context, R.color.goodComment))
        }
    }

    private fun setMainPost(post: Post) {
        contents.text = post.contents
        comments.text = post.comment_count.toString()
        timeStamp.text = viewModel.getTime(Date(post.timestamp!!.time))
        if(post.decision!=null) {
            if(post.decision!!) {
                setSVGcolor(downVote, R.color.black)
                setSVGcolor(upVote, R.color.goodComment)
                downVote.tag = "false"
                upVote.tag = "true"
            } else {
                setSVGcolor(upVote, R.color.black)
                setSVGcolor(downVote, R.color.badComment)
                upVote.tag = "false"
                downVote.tag = "true"
            }
        } else {
            setSVGcolor(upVote, R.color.black)
            setSVGcolor(downVote, R.color.black)
        }

        var points = post.up!! - post.down!!
        updatePointColors(points)
    }

    override fun onBackPressed() {
        viewModel.getChat(100)
        super.onBackPressed()
    }

    private fun observeMainPost(post_number: Long) {
        viewModel.observeSinglePost().observe(this, Observer {
            Log.d("jshit breaking ason", "$it")
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

    private fun startCreatePostActivity(post_number: Long) {
        var intent = Intent(this, NewPostActivity::class.java)
        intent.putExtra("isComment", true)
        intent.putExtra("reply_to", post_number)
        startActivity(intent)
    }

    private fun initFloatingButton(post_number: Long) {
        floating_action_button.setOnClickListener {
            startCreatePostActivity(post_number)
        }
    }

    private fun setDataObserver() {
        viewModel.observeComments().observe(this, Observer {
            Log.d("new post bro", "wew $it")
            initAdapter()
            postAdapter.submitList(it)
        })

        viewModel.observeSinglePost().observe(this, Observer {
            setMainPost(it)
        })
    }

    private fun setSVGcolor(view: ImageView, color: Int) {
        view.setColorFilter(ContextCompat.getColor(view.context, color), PorterDuff.Mode.SRC_IN)
    }

    private fun initVoteArrows(post_number: Long) {
        var downVote = findViewById<ImageView>(R.id.downVote)
        var upVote = findViewById<ImageView>(R.id.upVote)
        upVote.setOnClickListener {
            if(downVote.tag=="true") {
                var curPoints = points.text.toString().toInt()
                if(upVote.tag=="true") {
                    curPoints+=1
                } else {
                    curPoints+=2
                }
                setSVGcolor(downVote, R.color.black)
                setSVGcolor(upVote, R.color.goodComment)
                downVote.tag = "false"
                upVote.tag = "true"
                viewModel.makeDescition(viewModel.getUUID()!!, post_number, true)
                updatePointColors(curPoints)
            }
        }
        downVote.setOnClickListener {
            if(upVote.tag=="true") {
                var curPoints = points.text.toString().toInt()
                if(downVote.tag=="true") {
                    curPoints-=1
                } else {
                    curPoints-=2
                }
                setSVGcolor(upVote, R.color.black)
                setSVGcolor(downVote, R.color.badComment)
                upVote.tag = "false"
                downVote.tag = "true"
                viewModel.makeDescition(viewModel.getUUID()!!, post_number, false)
                updatePointColors(curPoints)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_fragment)

        viewModel = MainActivity.viewModel
        val intent = intent
        val post_number = intent.getLongExtra("post_number", 0)

        initFloatingButton(post_number)
        initDownSwipeLayout(post_number)
        initVoteArrows(post_number)

        setDataObserver()
        observeMainPost(post_number)
        viewModel.getSinglePost(post_number)
        viewModel.getComments(post_number)

        initActionBar()


    }



}
