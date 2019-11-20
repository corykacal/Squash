package com.example.squash.api

import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class UserData(
    @SerializedName("post_up")
    var post_up: Long? = null,

    @SerializedName("post_down")
    var post_down: Long? = null,

    @SerializedName("comment_up")
    var comment_up: Long? = null,

    @SerializedName("comment_down")
    var comment_down: Long? = null,

    @SerializedName("post_without_image")
    var post_without_image: Long? = null,

    @SerializedName("post_with_image")
    var post_with_image: Long? = null,

    @SerializedName("total_comments")
    var total_comments: Long? = null,

    @SerializedName("total_posts")
    var total_posts: Long? = null,

    @SerializedName("points_given")
    var points_given: Long? = null,

    @SerializedName("points_taken")
    var points_taken: Long? = null
)
