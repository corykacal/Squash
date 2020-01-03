package com.example.squash.posts

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.squash.MainActivity
import com.example.squash.R
import com.example.squash.api.MainViewModel
import com.example.squash.api.posts.Post
import com.example.squash.posts.ListAdapters.PostListAdapter
import com.example.squash.technology.Constants.Companion.PAGE_SIZE
import com.example.squash.technology.ListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.hotButton
import kotlinx.android.synthetic.main.fragment_home.newButton
import kotlinx.android.synthetic.main.fragment_home.searchResults


class HomeFragment: ListFragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var auth: FirebaseAuth

    private lateinit var postAdapter: PostListAdapter

    private lateinit var userPoints: TextView

    private var currentRecyclerState: Parcelable? = null
    private var previousRecyclerState: Parcelable? = null

    //first page always grabbed with initial refresh
    private var currentPage: Int = 1
    private var loadingNewPages: Boolean = false

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
            viewModel.getUserData {}
            refreshPosts { success: Boolean ->
                refresher.isRefreshing = false
                if(!success) {
                    Toast.makeText(context, "refresh failed", Toast.LENGTH_LONG).show()
                } else {
                    currentPage = 1
                }
            }
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

    fun refreshPosts(func: (Boolean) -> Unit) {
        viewModel.getPosts(PAGE_SIZE, 1, func)
    }

    fun loadPosts(page_number: Int, func: (Boolean) -> Unit) {
        viewModel.getPosts(PAGE_SIZE, page_number, func)
    }

    override fun startPostActivity(post: Post) {
        val intent = Intent(context, SinglePostActivity::class.java)
        intent.putExtra("post_number", post.postID)
        startActivityForResult(intent, 1)
    }

    private fun startCreatePostActivity(root: View) {
        var intent = Intent(root.context, NewPostActivity::class.java)
        intent.putExtra("isComment", false)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 2) {
            refreshPosts { success: Boolean ->
                if (!success) {
                    Toast.makeText(context, "refresh failed", Toast.LENGTH_LONG).show()
                } else {
                    currentPage = 1
                    searchResults.scrollToPosition(0)
                }
            }
        }
    }

    private fun initAdapter(root: View) {
        var recycler = root.findViewById<RecyclerView>(R.id.searchResults)
        postAdapter = PostListAdapter(viewModel, this)
        recycler.adapter = postAdapter
        val layoutManager = LinearLayoutManager(context)
        recycler.layoutManager =  layoutManager
        recycler.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy);
                var visibleItemCount = layoutManager.getChildCount()
                var totalItemCount = layoutManager.getItemCount()
                var firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if(!loadingNewPages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                    ) {
                        loadingNewPages = true
                        loadPosts(currentPage) { success: Boolean ->
                            loadingNewPages = false
                            if (success) {
                                setCurrentRecyclerState()
                                currentPage += 1
                            }
                        }
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

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

    fun resetCurrentRecyclerState() {
        searchResults.scrollToPosition(0)
    }

    fun changeCurrentRecyclerState() {
        currentRecyclerState = previousRecyclerState
        previousRecyclerState = searchResults.getLayoutManager()?.onSaveInstanceState()
    }

    override fun setCurrentRecyclerState() {
        currentRecyclerState = searchResults.getLayoutManager()?.onSaveInstanceState()
    }

    private fun setDataObserver(root: View) {

        //listen to post refreshing
        //TODO bugs have arrived from paging. it involves reseting states
        //TODO saving the loaded pages
        viewModel.observePosts().observe(this, Observer {
            var recyclerState =  currentRecyclerState
            initAdapter(root)
            postAdapter.submitList(it)
            searchResults.getLayoutManager()?.onRestoreInstanceState(recyclerState)
        })


        //listen to user data change
        viewModel.observeUserData().observe(this, Observer {
            userPoints.text = (it.post_up!! + it.comment_up!!).toString()
        })


        //listen to subject changing
        viewModel.observeSubject().observe(this, Observer {
            refreshPosts { success: Boolean ->
                if(!success) {
                    Toast.makeText(context, "refresh failed", Toast.LENGTH_LONG).show()
                } else {
                    currentPage = 1
                    searchResults.scrollToPosition(0)
                }
            }
            viewModel.getUserData {  }
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
                    //makes error go away
                    var MakeErrorGoAway = 0
                }
                changeCurrentRecyclerState()
                refreshPosts(sortLambda)
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
                }
                changeCurrentRecyclerState()
                refreshPosts(sortLambda)
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

    private fun listenToSpinner(root: View) {
        root.findViewById<Spinner>(R.id.subjectSpinner).onItemSelectedListener =
            object: AdapterView.OnItemSelectedListener {

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.setCurrentSubject(p0!!.selectedItem as String)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProviders.of(activity!!)[MainViewModel::class.java]

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        userPoints = root.findViewById(R.id.userPoints)

        initDownSwipeLayout(root)
        setDataObserver(root)

        listenForScrolling(root)

        listenToSpinner(root)

        initFloatingButton(root)

        initListButtons(root)


        //refreshPosts {  }



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

