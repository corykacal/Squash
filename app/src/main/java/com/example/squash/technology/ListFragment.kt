package com.example.squash.technology

import androidx.fragment.app.Fragment
import com.example.squash.api.posts.Post

abstract class ListFragment: Fragment() {

    abstract fun startPostFragment(post: Post)

    abstract fun setCurrentRecyclerState()

}
