package com.example.squash.api

import android.util.Log
import com.example.squash.api.posts.Post
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import kotlinx.coroutines.yield
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostRepository(private val postApi: PostApi) {
    val gson = Gson()

    companion object {
        //private val favorites: MutableSet<Post> = HashSet()
    }

    fun getRecentPosts(opuuid: String, number_of_post: Int?): Call<PostApi.ListingResponse> {
        Log.d("nigga we in here", "4jk432jl4k;32jl4k32kl")
        val request = postApi.getRecentPosts(opuuid, number_of_post)
        return request
    }

    fun getHotPosts(opuuid: String, number_of_post: Int?): Call<PostApi.ListingResponse> {
        val request = postApi.getHotPosts(opuuid, number_of_post)
        return request
    }

    fun makePost(contents: String, imageuuid: String?,
                 reply_to: Long?, opuuid: String): Call<PostApi.PostResponse> {
        var request = postApi.makePost(imageuuid, reply_to, opuuid, contents)
        return request
    }

    fun makeDescision(opuuid: String, post_number: Long, descision: Boolean?): Call<PostApi.PostResponse> {
        var request = postApi.makeDescision(opuuid, post_number, descision)
        return request
    }

    fun getSinglePost(post_number: Long, opuuid: String): Call<PostApi.ListingResponse> {
        val request = postApi.getSinglePost(post_number, opuuid)
        return request
    }

    fun getComments(post_number: Long, opuuid: String): Call<PostApi.ListingResponse> {
        val request = postApi.getComments(post_number, opuuid)
        return request
    }





}
