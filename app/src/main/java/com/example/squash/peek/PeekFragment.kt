package com.example.squash.peek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.squash.R
import com.example.squash.api.MainViewModel


class PeekFragment: Fragment() {

    private lateinit var viewModel: MainViewModel

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

        activity.let {
            viewModel = ViewModelProviders.of(it!!)[MainViewModel::class.java]
        }
        val root = inflater.inflate(R.layout.fragment_peek, container, false)


        return root
    }

}
