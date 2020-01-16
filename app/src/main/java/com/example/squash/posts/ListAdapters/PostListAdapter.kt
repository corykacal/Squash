package com.example.squash.posts.ListAdapters

import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.opengl.Visibility
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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.squash.R
import com.example.squash.api.MainViewModel
import com.example.squash.api.tables.Post
import com.example.squash.api.tables.Subject
import com.example.squash.technology.ListFragment
import com.example.squash.technology.SingleClickListener
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import java.util.*
import kotlin.coroutines.coroutineContext

/**
 * Created by witchel on 8/25/2019
 */

class PostListAdapter(private val viewModel: MainViewModel,
                      private val fragment: ListFragment?)
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

    private fun setPoints(pointsTV: TextView, item: Post) {
        var points = item.up!! - item.down!!
        if(points<0) {
            pointsTV.setTextColor(ContextCompat.getColor(pointsTV.context, R.color.badComment))
        } else {
            pointsTV.setTextColor(ContextCompat.getColor(pointsTV.context, R.color.goodComment))
        }
        pointsTV.text = points.toString()
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

        private var everything = itemView.findViewById<ConstraintLayout>(R.id.everything)
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
        private var subjectIV = itemView.findViewById<ImageView>(R.id.subjectIV)

        private var currentSubject = viewModel.observeSubject().value

        private var subjects = viewModel.observeSubjects().value


        val imageLoaded = { success: Boolean ->
            if(success) {
                loadingIV.isVisible = false
            }
        }




        fun bind(item: Post?) {
            imageIV.setImageDrawable(null)
            imageIV.isVisible = false
            loadingIV.isVisible = true
            contentsTV.minLines = 3
            contentsTV.maxLines = 5
            subjectTV.text = ""
            subjectTag.visibility = View.GONE
            subjectIV.isVisible = false
            everything.setBackgroundResource(R.color.post)
            if (item == null) return

            val postDate = Date(item.timestamp!!.time)
            timeTV.text = viewModel.getTime(postDate)

            contentsTV.text = item.contents

            commentsTV.text = item.comment_count.toString()


            if(item.subject!=null && currentSubject=="All") {
                subjectTV.text = item.subject!!.toUpperCase()
                subjectTag.visibility = View.VISIBLE
                if (item.subject=="STICKY") {
                    subjectTag.setBackgroundResource(R.color.blue)
                    everything.setBackgroundResource(R.color.lightBlue)
                    subjectIV.isVisible = true
                    GlideToVectorYou.justLoadImage(fragment?.activity, Uri.parse("https://squashsvg.s3.us-east-2.amazonaws.com/sticky.svg") , subjectIV)
                } else {
                    if(item.subject_color != null) {
                        subjectTag.setBackgroundColor(Color.parseColor("#%06x".format(item.subject_color)))
                    }
                    if(item.subject_svg != null) {
                        GlideToVectorYou.justLoadImage(fragment?.activity, Uri.parse(item.subject_svg) , subjectIV)
                        subjectIV.isVisible = true
                    }
                }
            }

            setPoints(pointsTV, item)

            imageAndText.setOnClickListener(object : SingleClickListener() {
                override fun onSingleClick(v: View) {
                    fragment?.setCurrentRecyclerState()
                    fragment?.startPostActivity(item)
                }
            })

            if(item.imageUUID!=null) {
                imageIV.isVisible = true
                contentsTV.minLines = 0
                imageIV.clipToOutline = true
                viewModel.downloadImgThumb(item.imageUUID!!, imageIV, imageLoaded)
                contentsTV.maxLines = 4
            } else {
                loadingIV.isVisible = false
            }

            setSVGcolor(downVote, R.color.voteGrey)
            setSVGcolor(upVote, R.color.voteGrey)

            if(item.decision!=null) {
                if(item.decision!!) {
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
            }


            upVote.setOnClickListener {
                if(downVote.tag=="true") {
                    setSVGcolor(downVote, R.color.voteGrey)
                    setSVGcolor(upVote, R.color.goodComment)
                    downVote.tag = "false"
                    upVote.tag = "true"
                    if(item.decision==false) {
                        item.down = item.down!!-1
                    }
                    item.decision = true
                    item.up = item.up!!+1
                    setPoints(pointsTV, item)
                    val voteLambda = { success: Boolean ->
                        if(!success) {
                            Toast.makeText(itemView.context, "vote failed", Toast.LENGTH_LONG).show()
                            setSVGcolor(downVote, R.color.voteGrey)
                            setSVGcolor(upVote, R.color.voteGrey)
                            item.decision = null
                            item.up = item.up!!-1
                            setPoints(pointsTV, item)
                        }
                    }
                    viewModel.makeDescition(item.postID!!, true, voteLambda)
                } else {
                    downVote.tag = "true"
                    upVote.tag = "true"
                    setSVGcolor(downVote, R.color.voteGrey)
                    setSVGcolor(upVote, R.color.voteGrey)
                    item.decision = null
                    item.up = item.up!!-1
                    setPoints(pointsTV, item)
                    val voteLambda = { success: Boolean ->
                        if(!success) {
                            Toast.makeText(itemView.context, "vote failed", Toast.LENGTH_LONG).show()
                            setSVGcolor(downVote, R.color.voteGrey)
                            setSVGcolor(upVote, R.color.voteGrey)
                            item.decision = true
                            item.up = item.up!!+1
                        }
                    }
                    viewModel.makeDescition(item.postID!!, null, voteLambda)
                }
            }
            downVote.setOnClickListener {
                if(upVote.tag=="true") {
                    setSVGcolor(upVote, R.color.voteGrey)
                    setSVGcolor(downVote, R.color.badComment)
                    upVote.tag = "false"
                    downVote.tag = "true"
                    if(item.decision==true) {
                        item.up = item.up!!-1
                    }
                    item.decision = false
                    item.down = item.down!!+1
                    setPoints(pointsTV, item)
                    val voteLambda = { success: Boolean ->
                        if(!success) {
                            Toast.makeText(itemView.context, "vote failed", Toast.LENGTH_LONG).show()
                            setSVGcolor(downVote, R.color.voteGrey)
                            setSVGcolor(upVote, R.color.voteGrey)
                            item.decision = null
                            item.down = item.down!!-1
                            setPoints(pointsTV, item)
                        }
                    }
                    viewModel.makeDescition(item.postID!!, false, voteLambda)
                } else {
                    downVote.tag = "true"
                    upVote.tag = "true"
                    setSVGcolor(downVote, R.color.voteGrey)
                    setSVGcolor(upVote, R.color.voteGrey)
                    item.decision = null
                    item.down = item.down!!-1
                    setPoints(pointsTV, item)
                    val voteLambda = { success: Boolean ->
                        if(!success) {
                            Toast.makeText(itemView.context, "vote failed", Toast.LENGTH_LONG).show()
                            setSVGcolor(downVote, R.color.voteGrey)
                            setSVGcolor(upVote, R.color.voteGrey)
                            item.decision = false
                            item.down = item.down!!+1
                        }
                    }
                    viewModel.makeDescition(item.postID!!, null, voteLambda)
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
