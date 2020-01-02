package com.example.squash.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.squash.R
import com.example.squash.api.MainViewModel


class EventFragment: Fragment() {

    private lateinit var viewModel: MainViewModel

    companion object {
        fun newInstance(): EventFragment {
            return EventFragment()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProviders.of(activity!!)[MainViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_events, container, false)

        return root
    }

}
