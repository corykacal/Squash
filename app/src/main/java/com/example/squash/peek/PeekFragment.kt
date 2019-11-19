package com.example.squash.peek

import com.example.squash.posts.NewPostActivity
import com.example.squash.posts.PostFragment
import com.example.squash.posts.PostListAdapter

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
import com.example.squash.posts.HomeFragment
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


class PeekFragment: Fragment() {

    companion object {
        fun newInstance(): PeekFragment {
            return PeekFragment()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = MainActivity.viewModel
        val root = inflater.inflate(R.layout.fragment_peek, container, false)


        return root
    }

}
