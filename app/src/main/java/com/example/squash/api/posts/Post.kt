package com.example.squash.api.posts

import com.google.firebase.firestore.ServerTimestamp
import java.sql.Timestamp
import java.util.*


data class Post(
    var contents: String? = null,
    var imageUUID: String? = null,
    var postID: Long? = null,
    var points: Long? = null,
    var opUUID: String? = null,
    @ServerTimestamp val timestamp: com.google.firebase.Timestamp? = null
)