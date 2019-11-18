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
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
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
import kotlinx.android.synthetic.main.activity_new_post.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.post_fragment.*
import kotlinx.android.synthetic.main.post_fragment.searchResults
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import okhttp3.internal.wait
import java.util.*

class PostFragment: AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var auth: FirebaseAuth

    private lateinit var postAdapter: PostListAdapter

    private fun initSideSwipes(root: View) {


    }

    private fun refreshSinglePost(post_number: Long, func: (Boolean) -> Unit) {
        viewModel.getComments(post_number, func)
        viewModel.getSinglePost(post_number, func)
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
            val lambda = { success: Boolean ->
                refresher.isRefreshing = false
                if(!success) {
                    Toast.makeText(applicationContext, "refresh failed", Toast.LENGTH_LONG)
                }
            }
            refreshSinglePost(post_number, lambda)
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
        image.setImageDrawable(null)
        image.isVisible = false
        contents.minLines = 3
        val imageLoaded = { success: Boolean ->
            if(success) {
                loadingPanel.isVisible = false
            }
        }
        contents.text = post.contents
        comments.text = post.comment_count.toString()
        timeStamp.text = viewModel.getTime(Date(post.timestamp!!.time))
        if(post.imageUUID!=null) {
            image.isVisible = true
            contents.minLines = 0
            image.clipToOutline = true
            viewModel.downloadImg(post.imageUUID!!, image, imageLoaded)
        } else {
            loadingPanel.isVisible = false
        }
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
        val lambda = { success: Boolean ->
            if(success) {
                Toast.makeText(applicationContext, "refresh failed", Toast.LENGTH_LONG)
            }
        }
        viewModel.getChat(100, lambda)
        super.onBackPressed()
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

    private fun pulseAnimation(textView: TextView) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 100 //You can manage the blinking time with this parameter
        anim.startOffset = 60
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 2
        textView.startAnimation(anim)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0);
    }


    fun initSubmitButton(post_number: Long) {
        submit.setOnClickListener {
            var contents = post.text.toString()
            if(contents.isBlank()) {
                Toast.makeText(baseContext, "Error: can't send blank post", Toast.LENGTH_SHORT).show()
            } else if(contents.lines().size>5) {
                Toast.makeText(baseContext, "Error: too many lines: ${contents.lines().size}, max: 10", Toast.LENGTH_SHORT).show()
            } else {
                val postLambda = { success: Boolean ->
                    if(success) {
                        hideKeyboard()
                        post.setText("")
                        commentLeft.isVisible = false
                    } else {
                        Toast.makeText(baseContext, "post failed", Toast.LENGTH_LONG).show()
                    }
                    var makeErrorGoAway = 0
                }
                viewModel.makePost(contents, null, null, post_number, postLambda)
            }
        }
    }

    private fun listenToEdit() {
        post.addTextChangedListener {
            val text = it.toString()
            if(text.length==0) {
                commentLeft.isVisible = false
                post.isCursorVisible = false
            } else {
                commentLeft.isVisible = true
                post.isCursorVisible = true
            }
            if(text.length!=0 && text.trim()=="") {
                post.setText("")
            } else {
                val length = text.length
                val remain = 365 - length
                commentLeft.text = (remain).toString()
                if (remain == 0) {
                    commentLeft.setTextColor(
                        ContextCompat.getColor(
                            commentLeft.context,
                            R.color.badComment
                        )
                    )
                } else {
                    commentLeft.setTextColor(ContextCompat.getColor(commentLeft.context, R.color.black))
                }
            }
        }
    }


    private fun setDataObserver(post_number: Long) {
        viewModel.observeComments().observe(this, Observer {
            initAdapter()
            postAdapter.submitList(it)
        })

        viewModel.observePosts().observe(this, Observer {
            val lambda = { success: Boolean ->

            }
            refreshSinglePost(post_number, lambda)
        })

        viewModel.observeSinglePost().observe(this, Observer {
            setMainPost(it)
        })
    }

    private fun setSVGcolor(view: ImageView, color: Int) {
        view.setColorFilter(ContextCompat.getColor(view.context, color), PorterDuff.Mode.SRC_IN)
    }

    private fun initVoteArrows(post_number: Long) {
        val voteLambda = { success: Boolean ->
            if(!success) {
                Toast.makeText(applicationContext, "vote failed", Toast.LENGTH_LONG)
                setSVGcolor(downVote, R.color.black)
                setSVGcolor(upVote, R.color.black)
            }
        }
        var downVote = findViewById<ImageView>(R.id.downVote)
        var upVote = findViewById<ImageView>(R.id.upVote)

        val lambda = { success: Boolean ->
        }

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
                viewModel.makeDescition(viewModel.getUUID()!!, post_number, true, voteLambda)
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
                viewModel.makeDescition(viewModel.getUUID()!!, post_number, false, voteLambda)
                updatePointColors(curPoints)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_fragment)

        /*
        KeyboardVisibilityEvent.setEventListener(this, object: KeyboardVisibilityEventListener {
            override fun onVisibilityChanged(isOpen: Boolean) {
                Toast.makeText(baseContext, "$isOpen", Toast.LENGTH_SHORT).show()
                if(!isOpen) {
                    commentLeft.isVisible = false
                    post.isCursorVisible = false
                } else {
                    commentLeft.isVisible = true
                    post.isCursorVisible = true
                }
            }
        })
         */

        viewModel = MainActivity.viewModel
        val intent = intent
        val post_number = intent.getLongExtra("post_number", 0)

        initSubmitButton(post_number)
        initDownSwipeLayout(post_number)
        initVoteArrows(post_number)
        listenToEdit()
        commentLeft.isVisible = false


        setDataObserver(post_number)
        val lambda = { success: Boolean ->
        }

        refreshSinglePost(post_number, lambda)

        initActionBar()


    }



}
