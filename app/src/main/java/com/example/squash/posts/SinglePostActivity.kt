package com.example.squash.posts

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.squash.MainActivity
import com.example.squash.R
import com.example.squash.api.MainViewModel
import com.example.squash.api.tables.Post
import com.example.squash.posts.ListAdapters.CommentListAdapter
import com.example.squash.posts.subContent.ImageFragment
import com.example.squash.technology.Cartesian
import com.example.squash.technology.Constants.Companion.COLORS
import com.example.squash.technology.Constants.Companion.VEGGIES
import com.example.squash.technology.SingleClickListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_singlepost.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.util.*
import kotlin.random.Random

class SinglePostActivity: AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var postAdapter: CommentListAdapter
    private lateinit var mixedPairs: List<List<Int>>

    private var post_number: Long = 0


    /*
     * Updates the comment section and the single post (needs ot be updated for points
     * param:
     *  func: callback
     */
    private fun refreshSinglePost(func: (Boolean) -> Unit) {
        viewModel.getComments(post_number, func)
        viewModel.getSinglePost(post_number, func)
    }


    /*
     * Attach post adapter and setup recycler view
     */
    private fun initAdapter() {
        var recycler = findViewById<RecyclerView>(R.id.commentRecycler)
        postAdapter =
            CommentListAdapter(viewModel, mixedPairs)
        recycler.adapter = postAdapter
        recycler.layoutManager = LinearLayoutManager(this)
    }


    /*
     * listen to the down swipe and refresh on swipe
     */
    private fun initDownSwipeLayout() {
        var refresher = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        refresher.setOnRefreshListener {
            refreshSinglePost { success: Boolean ->
                refresher.isRefreshing = false
                if(!success) {
                    Toast.makeText(applicationContext, "refresh failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    /*
     * Called when the user votes have changed and the colors need to be updated
     * param:
     *  points: amount of points the single post has
     */
    private fun updatePointColors(points: Int) {
        var pointsTV = findViewById<TextView>(R.id.points)
        pointsTV.text = points.toString()
        if(points<0) {
            pointsTV.setTextColor(ContextCompat.getColor(pointsTV.context, R.color.badComment))
        } else {
            pointsTV.setTextColor(ContextCompat.getColor(pointsTV.context, R.color.goodComment))
        }
    }

    /*
     * Sets the main post with all the data it needs
     * param:
     *  post: the post
     */
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

        if(post.subject!=null) {
            subjectTitle.text = post.subject + " - " + post.contents
        } else {
            subjectTitle.text = post.contents
        }

        timeStamp.text = viewModel.getTime(Date(post.timestamp!!.time))
        if(post.imageUUID!=null) {
            image.isVisible = true
            contents.minLines = 0
            image.clipToOutline = true
            viewModel.downloadImg(post.imageUUID!!, image, imageLoaded)
            //start the imageview fragment when a user clicks on the image
            image.setOnClickListener(object : SingleClickListener() {
                override fun onSingleClick(v: View) {
                    val fragment = ImageFragment.newInstance()
                    val bundle = Bundle()
                    bundle.putString("imageuuid", post.imageUUID)
                    fragment.arguments = bundle
                    supportFragmentManager
                        .beginTransaction()
                        // No back stack for home
                        .add(R.id.post_fragment, fragment)
                        .addToBackStack("image_fragment")
                        // TRANSIT_FRAGMENT_FADE calls for the Fragment to fade away. causes crash
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit()
                }
            })
        } else {
            loadingPanel.isVisible = false
        }
        if(post.decision!=null) {
            if(post.decision!!) {
                setSVGcolor(downVote, R.color.voteGrey)
                setSVGcolor(upVote, R.color.goodComment)
                downVote.tag = "false"
                upVote.tag = "true"
            } else {
                setSVGcolor(upVote, R.color.voteGrey)
                setSVGcolor(downVote, R.color.badComment)
                upVote.tag = "false"
                downVote.tag = "true"
            }
        } else {
            setSVGcolor(upVote, R.color.voteGrey)
            setSVGcolor(downVote, R.color.voteGrey)
        }

        var points = post.up!! - post.down!!
        updatePointColors(points)
    }


    /*
     * Will apply a pulsing animation on the given textView
     * param:
     *  textView: the textview you would like to pulse
     */
    private fun pulseAnimation(textView: TextView) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 100 //You can manage the blinking time with this parameter
        anim.startOffset = 60
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 2
        textView.startAnimation(anim)
    }


    /*
     * Helpful function to hide keyboard if it is every out at a bad time. Such as when
     * you hit submit
     */
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


    /*
     * Listens to the submit button and will send a POST request to the server.
     */
    fun initSubmitButton() {
        commentButton.setOnClickListener(object : SingleClickListener() {
            override fun onSingleClick(v: View) {
                var contents = post.text.toString()
                if(contents.isBlank()) {
                    Toast.makeText(applicationContext, "Error: can't send blank post", Toast.LENGTH_SHORT).show()
                } else if(contents.lines().size>10) {
                    Toast.makeText(applicationContext, "Error: too many lines: ${contents.lines().size}, max: 10", Toast.LENGTH_SHORT).show()
                } else {
                    val postLambda = { success: Boolean ->
                        if(success) {
                            hideKeyboard()
                            post.setText("")
                            commentLeft.isVisible = false
                            viewModel.getComments(post_number) {}
                        } else {
                            Toast.makeText(applicationContext, "post failed", Toast.LENGTH_LONG).show()
                        }
                    }
                    viewModel.makePost(contents, null, null, null, post_number, postLambda)
                }
            }
        })
    }


    /*
     * Callback for if the keyboard is lowered or raised. The cursor and characters left box will
     * disappear when the keyboard is collapsed.
     */
    private fun listenToKeyboard() {
        KeyboardVisibilityEvent.setEventListener(this, object: KeyboardVisibilityEventListener {
            override fun onVisibilityChanged(isOpen: Boolean) {
                if(!isOpen) {
                    commentLeft.isVisible = false
                    post.isCursorVisible = false
                } else {
                    commentLeft.isVisible = true
                    post.isCursorVisible = true
                }
            }
        })
    }


    /*
     * Callback for every time the text changes. This is to let the user know how many
     * characters they have left.
     */
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


    /*
     * The data observers for this activity
     */
    private fun setDataObserver() {
        viewModel.observeComments().observe(this, Observer {
            initAdapter()
            postAdapter.submitList(it)
        })

        viewModel.observePosts().observe(this, Observer {
            refreshSinglePost {}
        })

        viewModel.observeSinglePost().observe(this, Observer {
            setMainPost(it)
        })
    }


    /*
     * Changes the color of the given image view to the given color.
     * params:
     *  view: The target image view
     *  color: the color destination of the view
     */
    private fun setSVGcolor(view: ImageView, color: Int) {
        view.setColorFilter(ContextCompat.getColor(view.context, color), PorterDuff.Mode.SRC_IN)
    }


    /*
     * Currently spaghetti code to define how arrows should behave.
     */
    private fun initVoteArrows() {
        val voteLambda = { success: Boolean ->
            if(!success) {
                Toast.makeText(applicationContext, "vote failed", Toast.LENGTH_LONG).show()
                setSVGcolor(downVote, R.color.voteGrey)
                setSVGcolor(upVote, R.color.voteGrey)
            }
        }
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
                setSVGcolor(downVote, R.color.voteGrey)
                setSVGcolor(upVote, R.color.goodComment)
                downVote.tag = "false"
                upVote.tag = "true"
                viewModel.makeDescition(post_number, true, voteLambda)
                updatePointColors(curPoints)
            } else {
                var curPoints = points.text.toString().toInt()
                curPoints-=1
                downVote.tag = "true"
                upVote.tag = "true"
                setSVGcolor(downVote, R.color.voteGrey)
                setSVGcolor(upVote, R.color.voteGrey)
                viewModel.makeDescition(post_number, null, voteLambda)
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
                setSVGcolor(upVote, R.color.voteGrey)
                setSVGcolor(downVote, R.color.badComment)
                upVote.tag = "false"
                downVote.tag = "true"
                viewModel.makeDescition(post_number, false, voteLambda)
                updatePointColors(curPoints)
            } else {
                var curPoints = points.text.toString().toInt()
                curPoints+=1
                downVote.tag = "true"
                upVote.tag = "true"
                setSVGcolor(downVote, R.color.voteGrey)
                setSVGcolor(upVote, R.color.voteGrey)
                viewModel.makeDescition(post_number, null, voteLambda)
                updatePointColors(curPoints)
            }
        }
    }


    /*
     * Enables the back button on the toolbar
     */
    private fun setBackButton() {
        backButton.setOnClickListener(object: SingleClickListener() {
            override fun onSingleClick(v: View) {
                onBackPressed()
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singlepost)


        viewModel = MainActivity.viewModel
        val intent = intent
        post_number = intent.getLongExtra("post_number", 0)


        initSubmitButton()
        initDownSwipeLayout()
        initVoteArrows()

        listenToKeyboard()
        listenToEdit()

        setBackButton()


        val pairs = Cartesian.nAryCartesianProduct(listOf<List<Int>>(COLORS, VEGGIES))
        mixedPairs = pairs.shuffled(Random(post_number)) as List<List<Int>>

        commentLeft.isVisible = false

        setDataObserver()

        refreshSinglePost { success -> Boolean
            if(!success) {
                Toast.makeText(applicationContext, "unable to fetch post", Toast.LENGTH_SHORT).show()
            }
        }

    }



}
