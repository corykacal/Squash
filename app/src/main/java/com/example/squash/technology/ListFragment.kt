package com.example.squash.technology

import androidx.fragment.app.Fragment
import com.example.squash.api.tables.Post

abstract class ListFragment: Fragment() {

    abstract fun startPostActivity(post: Post)

    abstract fun setCurrentRecyclerState()

}
