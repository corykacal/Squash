package com.example.squash.api.posts

import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import java.sql.Timestamp
import java.util.*


data class Post(
    @SerializedName("contents")
    var contents: String? = null,

    @SerializedName("imageuuid")
    var imageUUID: String? = null,

    @SerializedName("post_number")
    var postID: Long? = null,

    @SerializedName("opuuid")
    var opUUID: String? = null,

    @SerializedName("reply_to")
    var reply_to: Long? = null,

    @SerializedName("comment_count")
    var comment_count: Int? = null,

    @SerializedName("descision")
    var decision: Boolean? = null,

    @SerializedName("up")
    var up: Int? = null,

    @SerializedName("down")
    var down: Int? = null,

    @SerializedName("timestamp")
    @ServerTimestamp val timestamp: Timestamp? = null
)