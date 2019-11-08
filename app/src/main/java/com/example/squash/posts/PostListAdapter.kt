package com.example.squash.posts

import android.app.PendingIntent.getActivity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.icu.util.LocaleData
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.squash.R
import com.example.squash.api.MainViewModel
//import com.example.squash.api.glide.Glide
import com.example.squash.api.posts.Post
import com.example.squash.technology.OnSwipeTouchListener
import com.google.type.Date
import kotlinx.coroutines.test.withTestContext
import org.w3c.dom.Text
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

/**
 * Created by witchel on 8/25/2019
 */

class PostListAdapter(private val viewModel: MainViewModel, private val fragment: HomeFragment)
    : ListAdapter<Post, PostListAdapter.VH>(RedditDiff()) {
    class RedditDiff : DiffUtil.ItemCallback<Post>() {



        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postID == newItem.postID
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.timestamp == newItem.timestamp
                    && oldItem.contents == newItem.contents
        }
    }


    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /*
        private var titleTV = itemView.findViewById<TextView>(R.id.title)
        private var selfTextTV = itemView.findViewById<TextView>(R.id.selfText)
        private var commentsTV = itemView.findViewById<TextView>(R.id.comments)
        private var leUpBoatsTV = itemView.findViewById<TextView>(R.id.score)
        private var heart = itemView.findViewById<ImageView>(R.id.rowFav)
        private var postImage = itemView.findViewById<ImageView>(R.id.image)
        //private var postIV = itemView.findViewById<ImageView>(R.id.imageTextUnion)

         */
        private var contentsTV = itemView.findViewById<TextView>(R.id.contents)
        private var image = itemView.findViewById<ImageView>(R.id.image)
        private var timeTV = itemView.findViewById<TextView>(R.id.timeStamp)
        private var commentsTV = itemView.findViewById<TextView>(R.id.comments)
        private var pointsTV = itemView.findViewById<TextView>(R.id.points)

        private var upVote = itemView.findViewById<ImageView>(R.id.upVote)
        private var downVote = itemView.findViewById<ImageView>(R.id.downVote)


        fun bind(item: Post?) {
            Log.d("post", "$item")
            if (item == null) return
            contentsTV.text = item.contents
            if(item.imageUUID!=null) {
                //viewModel.downloadJpg(item.imageUUID, image)
            }
            timeTV.text = "${2} m"
            commentsTV.text = Random.nextInt(0,32).toString()
            var points = item.points!!.toInt()
            if(points<0) {
                pointsTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.badComment))
            } else {
                pointsTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.goodComment))
            }
            pointsTV.text = points.toString()

            itemView.setOnClickListener {
                fragment.startFragment(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_post, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        //Log.d(MainActivity.TAG, "Bind pos $position")
        holder.bind(getItem(holder.adapterPosition))
    }

}
