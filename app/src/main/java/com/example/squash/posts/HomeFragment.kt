package com.example.squash.posts

import android.content.Intent
import android.graphics.Canvas
import android.media.MediaRouter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.searchResults
import kotlinx.android.synthetic.main.post_fragment.*


class HomeFragment: Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var auth: FirebaseAuth

    private lateinit var postAdapter: PostListAdapter


    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private fun initDownSwipeLayout(root: View) {
        var refresher = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        refresher.setOnRefreshListener {
            val lambda = { success: Boolean ->
                refresher.isRefreshing = false
                if(!success) {
                    Toast.makeText(context, "refresh failed", Toast.LENGTH_LONG)
                }
            }
            refreshChat(lambda)
        }
    }



    private fun refreshChat(func: (Boolean) -> Unit) {
        viewModel.getChat(100, func)
    }

    fun startPostFragment(post: Post) {
        val intent = Intent(context, PostFragment::class.java)
        intent.putExtra("post_number", post.postID)
        startActivity(intent)
    }

    private fun startCreatePostActivity(root: View) {
        var intent = Intent(root.context, NewPostActivity::class.java)
        intent.putExtra("isComment", false)
        startActivity(intent)
    }

    private fun initAdapter(root: View) {
        var recycler = root.findViewById<RecyclerView>(R.id.searchResults)
        postAdapter = PostListAdapter(viewModel, this, false)
        recycler.adapter = postAdapter
        recycler.layoutManager = LinearLayoutManager(context)

        /*
        old cocde when i was going to have swipe left for favorite.
        but i am going to have swipe left for the changing between
        hot anda new.

        val itemTouchCallback = postTouchHelper(0, ItemTouchHelper.LEFT)


        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recycler)
         */

    }

    /*
    private fun initSideSwipes(root: View) {

    }

     */

    private fun initFloatingButton(root: View) {
        root.findViewById<FloatingActionButton>(R.id.newPost).setOnClickListener {
            startCreatePostActivity(root)
        }
    }

    private fun setDataObserver(root: View) {
        viewModel.observePosts().observe(this, Observer {
            var recyclerViewState = searchResults.getLayoutManager()?.onSaveInstanceState()
            initAdapter(root)
            Log.d("list has changed: ", "$it")
            postAdapter.submitList(it)
            searchResults.getLayoutManager()?.onRestoreInstanceState(recyclerViewState)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = MainActivity.viewModel
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        initDownSwipeLayout(root)
        setDataObserver(root)

        val lambda = { success: Boolean ->
            if(!success) {
                Toast.makeText(context, "refresh failed", Toast.LENGTH_LONG)
            }
        }

        refreshChat(lambda)

        initFloatingButton(root)


        //initSideSwipes(root)

        /*
        root.setOnTouchListener(object: OnSwipeTouchListener(recycle.context) {
            override fun onSwipeRight() {
                Toast.makeText(recycle.context, "right", Toast.LENGTH_SHORT).show()
            }
            override fun onSwipeLeft() {
                Toast.makeText(recycle.context, "left", Toast.LENGTH_SHORT).show()
            }
        })

         */



        return root
    }

}

