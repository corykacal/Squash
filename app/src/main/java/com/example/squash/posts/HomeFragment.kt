package com.example.squash.posts

import android.graphics.Canvas
import android.media.MediaRouter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.SimpleAdapter
import android.widget.TextView
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.*


class HomeFragment: Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var auth: FirebaseAuth

    private lateinit var postAdapter: PostListAdapter


    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private fun initSwipeLayout(root: View) {
        var refresher = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        refresher.setOnRefreshListener {
            refresher.isRefreshing = false
        }
    }

    private fun initAdapter(root: View) {
        var recycler = root.findViewById<RecyclerView>(R.id.searchResults)
        postAdapter = PostListAdapter(viewModel)
        recycler.adapter = postAdapter
        recycler.layoutManager = LinearLayoutManager(context)

        val itemTouchCallback = postTouchHelper(0, ItemTouchHelper.LEFT)


        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recycler)

    }

    private fun setDataObserver(root: View) {
        viewModel.observePosts().observe(this, Observer {
            Log.d("new post bro", "wew $it")
            initAdapter(root)
            postAdapter.submitList(it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = MainActivity.viewModel
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        initSwipeLayout(root)
        setDataObserver(root)
        viewModel.getChat()



        return root
    }
}

