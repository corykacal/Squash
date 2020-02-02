package com.example.squash.posts.ListAdapters

import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.squash.R
import com.example.squash.api.MainViewModel
//import com.example.squash.api.glide.Glide
import com.example.squash.api.tables.Post
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.coroutines.coroutineContext

/**
 * Created by witchel on 8/25/2019
 */

class CommentListAdapter(private val viewModel: MainViewModel)
    : ListAdapter<Post, CommentListAdapter.VH>(RedditDiff()) {
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


        private var contentsTV = itemView.findViewById<TextView>(R.id.contents)
        private var timeTV = itemView.findViewById<TextView>(R.id.timeStamp)
        private var pointsTV = itemView.findViewById<TextView>(R.id.points)

        private var upVote = itemView.findViewById<ImageView>(R.id.upVote)
        private var downVote = itemView.findViewById<ImageView>(R.id.downVote)
        private var comment_tag = itemView.findViewById<ConstraintLayout>(R.id.comment_tag)
        private var veggieIV = itemView.findViewById<CircleImageView>(R.id.veggie)
        private var opTag = itemView.findViewById<TextView>(R.id.opTag)



        val voteLambda = { success: Boolean ->
            if(!success) {
                Toast.makeText(itemView.context, "vote failed", Toast.LENGTH_LONG)
                setSVGcolor(downVote, R.color.voteGrey)
                setSVGcolor(upVote, R.color.voteGrey)
            }
        }


        fun bind(item: Post?) {
            if (item == null) return

            val postDate = Date(item.timestamp!!.time)
            timeTV.text = viewModel.getTime(postDate)

            contentsTV.text = item.contents


            val uniqueCommenter = item.uniqueCommenter!!
            val veggie_color = item.veggie_color
            val veggie_url = item.veggie_url
            //setSVGcolor(comment_tag, color!!)
            opTag.text = ""
            if(uniqueCommenter==0) {
                opTag.text="OP"
            }
            Log.d("XXX", "$veggie_color")
            Log.d("XXX", "$veggie_url")
            veggieIV.setCircleBackgroundColor(Color.parseColor("#%06x".format(veggie_color)))
            Glide.with(veggieIV.context)
                .load(veggie_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(veggieIV)

            var points = item.up!! - item.down!!
            if(points<0) {
                pointsTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.badComment))
            } else {
                pointsTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.goodComment))
            }
            pointsTV.text = points.toString()

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
            .inflate(R.layout.row_comment, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        //Log.d(MainActivity.TAG, "Bind pos $position")
        holder.bind(getItem(holder.adapterPosition))
    }

}
