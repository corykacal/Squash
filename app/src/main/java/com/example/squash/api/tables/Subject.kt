package com.example.squash.api.tables

import com.google.gson.annotations.SerializedName

data class Subject(
    @SerializedName("subject")
    var subject: String? = null,

    @SerializedName("color")
    var color: Int? = null

)
