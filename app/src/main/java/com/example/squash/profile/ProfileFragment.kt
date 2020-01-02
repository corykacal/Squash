package com.example.squash.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.squash.R
import com.example.squash.api.MainViewModel
import com.example.squash.api.UserData
import com.example.squash.technology.SingleClickListener


class ProfileFragment: Fragment() {

    private lateinit var viewModel: MainViewModel

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    private lateinit var userPoints: TextView
    private lateinit var upVotes: TextView
    private lateinit var downVotes: TextView
    private lateinit var upVotesGiven: TextView
    private lateinit var downVotesGiven: TextView
    private lateinit var postWithImages: TextView
    private lateinit var postWithoutImages: TextView
    private lateinit var totalPost: TextView
    private lateinit var totalComments: TextView
    private lateinit var totalGiven: TextView
    private lateinit var totalRecieved: TextView
    private lateinit var upVotesRecieved: TextView
    private lateinit var downVotesRecieved: TextView




    private fun initProfile(root: View) {
        upVotes = root.findViewById(R.id.upVotes)
        downVotes = root.findViewById(R.id.downVotes)
        totalPost= root.findViewById(R.id.totalPost)
        totalComments = root.findViewById(R.id.totalComments)
        totalRecieved = root.findViewById(R.id.totalRecieved)
        userPoints = root.findViewById(R.id.userPoints)

        /*
        upVotesGiven = root.findViewById(R.id.up_votes_given)
        downVotesGiven = root.findViewById(R.id.down_votes_given)
        postWithImages = root.findViewById(R.id.post_made_with_image)
        postWithoutImages = root.findViewById(R.id.post_made_without_image)
        totalGiven = root.findViewById(R.id.total_points_given)
        upVotesRecieved = root.findViewById(R.id.up_votes)
        downVotesRecieved = root.findViewById(R.id.down_votes)
         */
    }

    private fun updateProfile(data: UserData) {
        userPoints.text = (data.post_up!! + data.comment_up!!).toString()
        val up = data.post_up!! + data.comment_up!!
        val down = data.post_down!! + data.comment_down!!
        upVotes.text = (up).toString()
        downVotes.text = (down).toString()
        totalRecieved.text = (up-down).toString()
        totalPost.text = (data.post_without_image!! + data.post_with_image!!).toString()
        totalComments.text = data.total_comments.toString()
        /*
        upVotesRecieved.text = (data.post_up!! + data.comment_up!!).toString()
        downVotesRecieved.text = (data.post_down!! + data.comment_down!!).toString()
        upVotesGiven.text = data.points_given.toString()
        downVotesGiven.text = data.points_taken.toString()
        postWithImages.text = data.post_with_image.toString()
        postWithoutImages.text = data.post_without_image.toString()
        totalGiven.text = (data.points_given!! - data.points_taken!!).toString()

         */
    }

    private fun listenToUserData(root: View) {
        viewModel.observeUserData().observe(this, Observer {
            updateProfile(it)
        })
    }

    private fun listenToPostsButton(root: View) {
        root.findViewById<CardView>(R.id.getMyPost).setOnClickListener(object: SingleClickListener() {
            override fun onSingleClick(v: View) {
                val fragment = MyPostsFragment.newInstance()
                fragmentManager!!
                    .beginTransaction()
                    // No back stack for home
                    .add(R.id.main_frame, fragment)
                    .addToBackStack("my_post")
                    // TRANSIT_FRAGMENT_FADE calls for the Fragment to fade away. causes crash
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProviders.of(activity!!)[MainViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        initProfile(root)
        listenToUserData(root)
        listenToPostsButton(root)

        viewModel.getUserData {}

        return root
    }

}

