package com.example.squash.posts

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
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
import com.example.squash.technology.SingleClickListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_singlepost.*
import java.util.*
import kotlin.random.Random

class SinglePostActivity: AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var auth: FirebaseAuth

    private lateinit var postAdapter: CommentListAdapter
    private lateinit var mixedPairs: List<List<Int>>

    private lateinit var toolbar: Toolbar

    private fun initSideSwipes(root: View) {


    }

    private fun refreshSinglePost(post_number: Long, func: (Boolean) -> Unit) {
        viewModel.getComments(post_number, func)
        viewModel.getSinglePost(post_number, func)
    }

    private fun initAdapter() {
        var recycler = findViewById<RecyclerView>(R.id.commentRecycler)
        postAdapter =
            CommentListAdapter(viewModel, mixedPairs)
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
                    Toast.makeText(applicationContext, "refresh failed", Toast.LENGTH_LONG).show()
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

        if(post.subject!=null) {
            subjectTitle.text = post.subject
        }
        timeStamp.text = viewModel.getTime(Date(post.timestamp!!.time))
        if(post.imageUUID!=null) {
            image.isVisible = true
            contents.minLines = 0
            image.clipToOutline = true
            viewModel.downloadImg(post.imageUUID!!, image, imageLoaded)
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


    override fun onBackPressed() {
        /*
         * Do I want this???

        val lambda = { success: Boolean ->
            if(!success) {
                Toast.makeText(applicationContext, "refresh failed", Toast.LENGTH_LONG).show()
            }
        }
        * would refresh page_Size*current_page
        viewModel.getPosts(PAGE_SIZE, 1, lambda)
         */
        super.onBackPressed()
    }

    private fun initActionBar() {
        /*
        toolbar = findViewById(R.id.posttoolbar)
        toolbar.setTitle(title)
        toolbar.setNavigationIcon(R.drawable.back_arrow)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

         */
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
                Toast.makeText(applicationContext, "vote failed", Toast.LENGTH_LONG).show()
                setSVGcolor(downVote, R.color.voteGrey)
                setSVGcolor(upVote, R.color.voteGrey)
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

    private fun setBackButton() {
        backButton.setOnClickListener(object: SingleClickListener() {
            override fun onSingleClick(v: View) {
                onBackPressed()
            }
        })
    }

    private var veggies = listOf<Int>(R.drawable.ic_apple,
        R.drawable.ic_beetroot,
        R.drawable.ic_bell_pepper,
        R.drawable.ic_broccoli,
        R.drawable.ic_carrot,
        R.drawable.ic_cherry,
        R.drawable.ic_chili,
        R.drawable.ic_corn,
        R.drawable.ic_cucumber,
        R.drawable.ic_eggplant,
        R.drawable.ic_grape,
        R.drawable.ic_orange,
        R.drawable.ic_pineapple,
        R.drawable.ic_strawberry,
        R.drawable.ic_watermelon,
        R.drawable.ic_avocado
    )

    private var colors = listOf<Int>(
        R.color.red,
        R.color.orange,
        R.color.yellow,
        R.color.green,
        R.color.lime,
        R.color.maroon,
        R.color.blue,
        R.color.teal,
        R.color.turquoise,
        R.color.navy,
        R.color.pink,
        R.color.brown,
        R.color.beige,
        R.color.purple,
        R.color.grey,
        R.color.golden
    )




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singlepost)

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

        setBackButton()


        val pairs = Cartesian.nAryCartesianProduct(listOf<List<Int>>(colors, veggies))

        mixedPairs = pairs.shuffled(Random(post_number)) as List<List<Int>>


        commentLeft.isVisible = false


        setDataObserver(post_number)
        val lambda = { success: Boolean ->
        }

        refreshSinglePost(post_number, lambda)

        initActionBar()


    }



}
