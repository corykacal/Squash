package com.example.squash.posts

import android.content.Intent
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
import com.example.squash.MainActivity.Companion.viewModel
import com.example.squash.R
import com.example.squash.api.MainViewModel
import com.example.squash.api.User
import com.example.squash.api.photoapi
import com.example.squash.api.posts.Post
import com.example.squash.technology.OnSwipeTouchListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.action_bar.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.hotButton
import kotlinx.android.synthetic.main.fragment_home.newButton
import kotlinx.android.synthetic.main.fragment_home.searchResults
import kotlinx.android.synthetic.main.post_fragment.*


class HomeFragment: Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var auth: FirebaseAuth

    private lateinit var postAdapter: PostListAdapter

    private var currentRecyclerState: Parcelable? = null
    private var previousRecyclerState: Parcelable? = null

    var fragId = R.id.posts_icon


    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
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
            refreshChat(lambda)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("call$#@$#@", "destroy")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("meme", "meme")
        Log.d("$#@!$#@!$#@!", "calling save instance state")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var prev = savedInstanceState?.getString("meme")
        Log.d("i said before: ", "$prev")
    }



    fun refreshChat(func: (Boolean) -> Unit) {
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

    fun changeCurrentRecyclerState() {
        currentRecyclerState = previousRecyclerState
        previousRecyclerState = searchResults.getLayoutManager()?.onSaveInstanceState()
    }

    fun setCurrentRecyclerState() {
        currentRecyclerState = searchResults.getLayoutManager()?.onSaveInstanceState()
    }

    private fun setDataObserver(root: View) {
        viewModel.observePosts().observe(this, Observer {
            var recyclerState =  currentRecyclerState
            initAdapter(root)
            Log.d("list has changed: ", "$it")
            postAdapter.submitList(it)
            searchResults.getLayoutManager()?.onRestoreInstanceState(recyclerState)
        })
    }

    private fun initListButtons(root: View) {
        root.findViewById<Button>(R.id.hotButton).setOnClickListener {
            if(MainActivity.newPost) {
                MainActivity.newPost = false
                val sortLambda = { success: Boolean ->
                    if(success) {
                        it.setBackground(resources.getDrawable(R.drawable.selected_button))
                        (it as Button).setTextColor(ContextCompat.getColor(it.context, R.color.secondaryYellow))
                        newButton.setBackground(resources.getDrawable(R.drawable.unselected_button))
                        newButton.setTextColor(ContextCompat.getColor(it.context, R.color.selectedButton))
                    } else {
                        Toast.makeText(context, "network failed", Toast.LENGTH_LONG).show()
                        MainActivity.newPost = true
                    }
                    var MakeErrorGoAway = 0
                }
                changeCurrentRecyclerState()
                refreshChat(sortLambda)
            }
        }

        root.findViewById<Button>(R.id.newButton).setOnClickListener {
            if (!MainActivity.newPost) {
                MainActivity.newPost = true
                val sortLambda = { success: Boolean ->
                    if(success) {
                        it.setBackground(resources.getDrawable(R.drawable.selected_button))
                        (it as Button).setTextColor(ContextCompat.getColor(it.context, R.color.secondaryYellow))
                        hotButton.setBackground(resources.getDrawable(R.drawable.unselected_button))
                        hotButton.setTextColor(ContextCompat.getColor(it.context, R.color.selectedButton))
                    } else {
                        Toast.makeText(context, "network failed", Toast.LENGTH_LONG).show()
                        MainActivity.newPost = false
                    }
                    var MakeErrorGoAway = 0
                }
                changeCurrentRecyclerState()
                refreshChat(sortLambda)
            }
        }

    }

    private fun listenForScrolling(root: View) {
        //not sure how pretty or useful to the UX  this is
        /*
        val fab = root.findViewById<FloatingActionButton>(R.id.newPost)
        root.findViewById<RecyclerView>(R.id.searchResults).addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy > 0 || dy < 0 && fab.isShown) {
                    fab.alpha = 0.5f
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.alpha = 0.9f
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
         */
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

        listenForScrolling(root)


        initFloatingButton(root)

        initListButtons(root)



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

