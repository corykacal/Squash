package com.example.squash.posts

import android.app.PendingIntent.getActivity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.icu.util.LocaleData
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.squash.R
import com.example.squash.api.MainViewModel
//import com.example.squash.api.glide.Glide
import com.example.squash.api.posts.Post
import com.example.squash.technology.OnSwipeTouchListener
import com.google.type.Date
import kotlinx.coroutines.test.withTestContext
import okhttp3.internal.waitMillis
import org.w3c.dom.Text
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

/**
 * Created by witchel on 8/25/2019
 */

class PostListAdapter(private val viewModel: MainViewModel,
                      private val fragment: HomeFragment?)
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

    private fun setSVGcolor(view: ImageView, color: Int) {
        view.setColorFilter(ContextCompat.getColor(view.context, color), PorterDuff.Mode.SRC_IN)
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

        private var imageAndText = itemView.findViewById<ConstraintLayout>(R.id.imageTextUnion)

        private var contentsTV = itemView.findViewById<TextView>(R.id.contents)
        private var imageIV = itemView.findViewById<ImageView>(R.id.image)
        private var timeTV = itemView.findViewById<TextView>(R.id.timeStamp)
        private var commentsTV = itemView.findViewById<TextView>(R.id.comments)
        private var pointsTV = itemView.findViewById<TextView>(R.id.points)
        private var loadingIV = itemView.findViewById<RelativeLayout>(R.id.loadingPanel)

        private var upVote = itemView.findViewById<ImageView>(R.id.upVote)
        private var downVote = itemView.findViewById<ImageView>(R.id.downVote)

        private var subjectTag = itemView.findViewById<ConstraintLayout>(R.id.subject_tag)
        private var subjectTV = itemView.findViewById<TextView>(R.id.subject)


        val imageLoaded = { success: Boolean ->
            if(success) {
                loadingIV.isVisible = false
            }
        }


        val voteLambda = { success: Boolean ->
            if(!success) {
                Toast.makeText(itemView.context, "vote failed", Toast.LENGTH_LONG)
                setSVGcolor(downVote, R.color.black)
                setSVGcolor(upVote, R.color.black)
            }
        }


        fun bind(item: Post?) {
            imageIV.setImageDrawable(null)
            imageIV.isVisible = false
            contentsTV.minLines = 3
            contentsTV.maxLines = 5
            subjectTag.isVisible = false
            if (item == null) return

            val postDate = Date(item.timestamp!!.time)
            timeTV.text = viewModel.getTime(postDate)

            contentsTV.text = item.contents


            commentsTV.text = item.comment_count.toString()

            if(item.subject!=null) {
                subjectTag.isVisible = true
                subjectTV.text = item.subject!!.toUpperCase()
                if(item.subject=="Memes") {
                    subjectTag.setBackgroundResource(R.color.blue)
                }
            }

            var points = item.up!! - item.down!!
            if(points<0) {
                pointsTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.badComment))
            } else {
                pointsTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.goodComment))
            }
            pointsTV.text = points.toString()

            imageAndText.setOnClickListener {
                fragment!!.setCurrentRecyclerState()
                fragment!!.startPostFragment(item)
                imageAndText.isEnabled = false
            }

            if(item.imageUUID!=null) {
                imageIV.isVisible = true
                contentsTV.minLines = 0
                imageIV.clipToOutline = true
                viewModel.downloadImg(item.imageUUID!!, imageIV, imageLoaded)
                contentsTV.maxLines = 4
            } else {
                loadingIV.isVisible = false
            }

            setSVGcolor(downVote, R.color.black)
            setSVGcolor(upVote, R.color.black)

            if(item.decision!=null) {
                if(item.decision!!) {
                    setSVGcolor(downVote, R.color.black)
                    setSVGcolor(upVote, R.color.goodComment)
                    downVote.tag = "false"
                    upVote.tag = "true"
                } else {
                    setSVGcolor(upVote, R.color.black)
                    setSVGcolor(downVote, R.color.badComment)
                    upVote.tag = "false"
                    downVote.tag = "true"
                }
            }


            upVote.setOnClickListener {
                fragment?.setCurrentRecyclerState()
                if(downVote.tag=="true") {
                    setSVGcolor(downVote, R.color.black)
                    setSVGcolor(upVote, R.color.goodComment)
                    downVote.tag = "false"
                    upVote.tag = "true"
                    viewModel.makeDescition(viewModel.getUUID()!!, item.postID!!, true, voteLambda)
                } else {
                    downVote.tag = "true"
                    upVote.tag = "true"
                    setSVGcolor(downVote, R.color.black)
                    setSVGcolor(upVote, R.color.black)
                    viewModel.makeDescition(viewModel.getUUID()!!, item.postID!!, null, voteLambda)
                }
            }
            downVote.setOnClickListener {
                fragment?.setCurrentRecyclerState()
                if(upVote.tag=="true") {
                    setSVGcolor(upVote, R.color.black)
                    setSVGcolor(downVote, R.color.badComment)
                    upVote.tag = "false"
                    downVote.tag = "true"
                    viewModel.makeDescition(viewModel.getUUID()!!, item.postID!!, false, voteLambda)
                } else {
                    downVote.tag = "true"
                    upVote.tag = "true"
                    setSVGcolor(downVote, R.color.black)
                    setSVGcolor(upVote, R.color.black)
                    viewModel.makeDescition(viewModel.getUUID()!!, item.postID!!, null, voteLambda)
                }
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
