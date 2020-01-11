package com.example.squash.api.tables

import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import java.sql.Timestamp


data class Post(
    @SerializedName("contents")
    var contents: String? = null,

    @SerializedName("subject")
    var subject: String? = null,

    @SerializedName("imageuuid")
    var imageUUID: String? = null,

    @SerializedName("post_number")
    var postID: Long? = null,

    @SerializedName("unique_commenter")
    var uniqueCommenter: Int? = null,

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
    @ServerTimestamp val timestamp: Timestamp? = null,

    @SerializedName("color")
    var subject_color: Int? = null,

    @SerializedName("svg")
    var subject_svg: String? = null,

    @SerializedName("image")
    var subject_image: String? = null
)