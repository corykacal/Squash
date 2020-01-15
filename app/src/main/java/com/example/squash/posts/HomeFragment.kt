package com.example.squash.posts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.squash.MainActivity
import com.example.squash.R
import com.example.squash.api.MainViewModel
import com.example.squash.api.tables.Post
import com.example.squash.posts.ListAdapters.PostListAdapter
import com.example.squash.technology.Constants.Companion.CREATE_POST_ACTIVITY
import com.example.squash.technology.Constants.Companion.PAGE_SIZE
import com.example.squash.technology.Constants.Companion.VIEW_POST_ACTIVITY
import com.example.squash.technology.ListFragment
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment: ListFragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var auth: FirebaseAuth

    private lateinit var postAdapter: PostListAdapter

    private lateinit var userPoints: TextView

    private var currentRecyclerState: Parcelable? = null
    private var previousRecyclerState: Parcelable? = null

    //First page always grabbed with initial refresh
    private var currentPage: Int = 1
    private var loadingNewPages: Boolean = false

    private var subjectsArray: MutableList<String> = mutableListOf()

    private var spinnerReset = 0


    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }


    /*
     * Initialized the down swipe listener
     * param:
     *  root: root view
     */
    private fun initDownSwipeLayout(root: View) {
        var refresher = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        refresher.setOnRefreshListener {

            setCurrentRecyclerState()
            viewModel.getUserData {}

            refreshPostsAndSubjects { success: Boolean ->
                refresher.isRefreshing = false
                if(!success) {
                    Toast.makeText(context, "refresh failed", Toast.LENGTH_LONG).show()
                } else {
                    currentPage = 1
                }
            }
        }
    }


    /*
     * Refreshes the location based data.
     * Param:
     *  func: callback
     */
    fun refreshPostsAndSubjects(func: (Boolean) -> Unit) {
        spinnerReset = 0 //how many times the spinner has been clicked
        viewModel.getSubjects { subjectSuccess: Boolean ->
            if(subjectSuccess) {
                viewModel.getPosts(PAGE_SIZE, 1, func)
            } else {
                func(false)
            }
        }
    }


    /*
     * loads a new batch of posts
     * param:
     *  page_number: the pagination page number
     *  func: callback
     */
    fun loadPosts(page_number: Int, func: (Boolean) -> Unit) {
        viewModel.getPosts(PAGE_SIZE, page_number, func)
    }


    /* starts the view post activity
     * param:
     *  post: the post you want to view
     */
    override fun startPostActivity(post: Post) {
        val intent = Intent(context, SinglePostActivity::class.java)
        intent.putExtra("post_number", post.postID)
        startActivityForResult(intent, VIEW_POST_ACTIVITY)
    }


    /*
     * starts the new post creation activity
     */
    private fun startCreatePostActivity() {
        var intent = Intent(context, NewPostActivity::class.java)
        var currentSubject = viewModel.observeSubject().value
        if(currentSubject==null) {
            currentSubject = "All"
        }
        intent.putExtra("subject", currentSubject)
        startActivityForResult(intent, CREATE_POST_ACTIVITY)
    }


    /*
     * On activity return android will arrive here
     *
     * From Constants.kt
     * const val CREATE_POST_ACTIVITY = 2
     * const val VIEW_POST_ACTIVITY = 1
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Create post activity
        if(requestCode == CREATE_POST_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK) {
                refreshPostsAndSubjects { success: Boolean ->
                    if (!success) {
                        Toast.makeText(context, "refresh failed", Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("meme", "fdsfdsafdsafdsafdsafdsafs%$$#@%$#%$#@%@$#^^$#@^@#")
                        currentPage = 1
                        resetCurrentRecyclerState()
                    }
                }
            }

        }
        /*else if (requestCode == VIEW_POST_ACTIVITY) {
            //TODO check HomeFragment is ever destroyed after opening viewpost
        } */
    }


    /*
     * Initializes the list adapter and assigns recycler scroll behavior
     */
    private fun initAdapter(root: View) {
        postAdapter = PostListAdapter(viewModel, this)
        val layoutManager = LinearLayoutManager(context)
        var recycler = root.findViewById<RecyclerView>(R.id.postRecycler)

        recycler.adapter = postAdapter
        recycler.layoutManager =  layoutManager
        recycler.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy);
                var visibleItemCount = layoutManager.getChildCount()
                var totalItemCount = layoutManager.getItemCount()
                var firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!loadingNewPages
                    && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {

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

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }


    /*
     * creates the listener for the new post button
     * param:
     *  root: the root view
     */
    private fun initFloatingButton(root: View) {
        root.findViewById<FloatingActionButton>(R.id.newPost).setOnClickListener {
            startCreatePostActivity()
        }
    }


    /*
     * resets the scroll position (recycler state) to the top of the list (0)
     *
     *  TODO: fix the janky post delayed fix
     */
    fun resetCurrentRecyclerState() {
        Handler().postDelayed(Runnable {
            postRecycler.scrollToPosition(0)
            postRecycler.isVisible = true
        }, 100)
    }


    /*
     * Keeps track of two recycler states. one for Hot and one for New
     *
     * TODO: doesnt work since subject update
     */
    fun changeCurrentRecyclerState() {
        currentRecyclerState = previousRecyclerState
        previousRecyclerState = postRecycler.getLayoutManager()?.onSaveInstanceState()
    }


    /*
     * saves the current scroll position
     */
    override fun setCurrentRecyclerState() {
        currentRecyclerState = postRecycler.getLayoutManager()?.onSaveInstanceState()
    }


    /*
     * Sets up all data observers to observe data from MainViewModel
     * param:
     *  root: the root view
     */
    private fun setDataObserver(root: View) {

        //listen to post refreshing
        //TODO bugs have arrived from paging. it involves reseting states
        //TODO saving the loaded pages
        viewModel.observePosts().observe(this, Observer {
            var recyclerState =  currentRecyclerState
            initAdapter(root)
            postAdapter.submitList(it)
            postRecycler.getLayoutManager()?.onRestoreInstanceState(recyclerState)
        })


        //listen to user data change
        viewModel.observeUserData().observe(this, Observer {
            userPoints.text = (it.post_up!! + it.comment_up!!).toString()
        })


        //listen to subject changing
        viewModel.observeSubject().observe(this, Observer {
            Log.d("SUBJECT NIGGA", "()()()()()()()()()()()()()()()()()()()()()()()()()()()()")
            postRecycler.isVisible = false
            refreshPostsAndSubjects { success: Boolean ->
                if(!success) {
                    //Toast.makeText(context, "refresh failed", Toast.LENGTH_LONG).show()
                    postRecycler.isVisible = true
                } else {
                    currentPage = 1
                    resetCurrentRecyclerState()
                }
            }
        })

        val spinner = root.findViewById<Spinner>(R.id.subjectSpinner)
        val currentIndex = spinner.selectedItemPosition

        val adapter = ArrayAdapter<String>(context,
            R.layout.spinner_home_fragment_item, subjectsArray)

        adapter.setDropDownViewResource(R.layout.spinner_home_fragment_dropdown)

        spinner.setSelection(currentIndex)


        viewModel.observeSubjects().observe(this, Observer {
            val currentSubject = viewModel.observeSubject().value

            var index = 0
            var foundSubject = 0
            it.forEach {
                if(it.subject==currentSubject) {
                    foundSubject = index
                }
                index+=1
            }

            subjectsArray.removeAll {
                true
            }

            it.forEach {
                subjectsArray.add(it.subject!!)
            }

            spinner.adapter = adapter

            if(foundSubject==0 && currentSubject!=null && currentSubject!="All") {
                Toast.makeText(context, "moved out of $currentSubject zone", Toast.LENGTH_LONG).show()
                viewModel.setCurrentSubject("All")
            }
            spinner.setSelection(foundSubject)
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
                refreshPostsAndSubjects(sortLambda)
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
                refreshPostsAndSubjects(sortLambda)
            }
        }

    }


    private fun listenToSpinner(root: View) {
        root.findViewById<Spinner>(R.id.subjectSpinner).onItemSelectedListener =
            object: AdapterView.OnItemSelectedListener {

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(++spinnerReset > 1) {
                    viewModel.setCurrentSubject(p0!!.selectedItem as String)
                    spinnerReset = 0
                }
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

        activity.let {
            viewModel = ViewModelProviders.of(it!!)[MainViewModel::class.java]
        }

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        userPoints = root.findViewById(R.id.userPoints)

        initDownSwipeLayout(root)
        setDataObserver(root)

        listenToSpinner(root)

        initFloatingButton(root)

        initListButtons(root)

        viewModel.quickLocationData {
            if(it) {
                refreshPostsAndSubjects {
                    if(!it) {
                        Toast.makeText(context, "refresh failed", Toast.LENGTH_SHORT)
                    }
                }
            } else {
                Toast.makeText(context, "refresh failed", Toast.LENGTH_SHORT)
            }
        }

        return root
    }


}

