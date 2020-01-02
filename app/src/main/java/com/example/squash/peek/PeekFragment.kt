package com.example.squash.peek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.squash.MainActivity
import com.example.squash.MainActivity.Companion.viewModel
import com.example.squash.R


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
