package com.example.squash.profile

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.squash.R
import com.example.squash.api.MainViewModel
import com.example.squash.api.posts.Post
import com.example.squash.posts.PostFragment
import com.example.squash.posts.PostListAdapter
import com.example.squash.technology.ListFragment
import kotlinx.android.synthetic.main.fragment_home.*


class MyPostFragment: ListFragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var postAdapter: PostListAdapter
    private var currentRecyclerState: Parcelable? = null
    private lateinit var recycler: RecyclerView
    private lateinit var userPoints: TextView

    companion object {
        fun newInstance(): MyPostFragment {
            return MyPostFragment()
        }
    }

    private fun initAdapter(root: View) {
        recycler = root.findViewById(R.id.myPost)
        postAdapter = PostListAdapter(viewModel, this)
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

    fun refreshPosts(func: (Boolean) -> Unit) {
        viewModel.getMyPost(100, func)
    }

    private fun initDownSwipeLayout(root: View) {
        var refresher = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        refresher.setOnRefreshListener {
            setCurrentRecyclerState()
            val lambda = { success: Boolean ->
                refresher.isRefreshing = false
                if(!success) {
                    Toast.makeText(context, "refresh failed", Toast.LENGTH_LONG)
                }
            }
            viewModel.getUserData {}
            refreshPosts(lambda)
        }
    }

    private fun setDataObserver(root: View) {
        viewModel.observeMyPost().observe(this, Observer {
            var recyclerState =  currentRecyclerState
            initAdapter(root)
            postAdapter.submitList(it)
            recycler.layoutManager?.onRestoreInstanceState(recyclerState)
        })
        viewModel.observeUserData().observe(this, Observer {
            userPoints.text = (it.post_up!! + it.comment_up!!).toString()
        })
    }

    override fun startPostFragment(post: Post) {
        val intent = Intent(context, PostFragment::class.java)
        intent.putExtra("post_number", post.postID)
        startActivityForResult(intent, 1)
    }

    override fun setCurrentRecyclerState() {
        currentRecyclerState = recycler.layoutManager?.onSaveInstanceState()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProviders.of(activity!!)[MainViewModel::class.java]

        val root = inflater.inflate(R.layout.my_post_fragment, container, false)

        userPoints = root.findViewById(R.id.userPoints)

        setDataObserver(root)
        initAdapter(root)
        initDownSwipeLayout(root)

        refreshPosts {  }

        return root
    }

}
