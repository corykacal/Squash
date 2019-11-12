package com.example.squash.api

import android.util.Log
import com.example.squash.api.posts.Post
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

    fun getComments(post_number: Long, opuuid: String): List<Post> {
        val request = postApi.getComments(post_number, opuuid)
        var results = request.execute()
        Log.d("comments are: ", "${results.body()?.results}")
        if(results.isSuccessful) {
            return results.body()!!.results
        } else {
            return mutableListOf()
        }
    }

    fun makeDescision(opuuid: String, post_number: Long, descision: Boolean): Boolean {
        var request = postApi.makeDescision(opuuid, post_number, descision)
        var results = request.execute()
        Log.d("$$$$$$$$$$$", "${results.body()?.results}")
        return results.isSuccessful
    }

    fun makePost(contents: String, imageuuid: String?,
                         reply_to: Long?, opuuid: String): Boolean {
        Log.d("PostRepository", "makeing a commmand swasg")
        var request = postApi.makePost(imageuuid, reply_to, opuuid, contents)
        var results = request.execute()
        return results.isSuccessful
    }

    fun getSinglePost(post_number: Long, opuuid: String): Post {
        val request = postApi.getSinglePost(post_number, opuuid)
        var results = request.execute()
        if(results.isSuccessful) {
            return results.body()!!.results[0]
        } else {
            return Post()
        }
    }

    fun getPosts(opuuid: String, number_of_post: Int?): List<Post>? {
        val request = postApi.getPost(opuuid, number_of_post)
        var results = request.execute()
        if(results.isSuccessful) {
            return results.body()!!.results
        } else {
            return mutableListOf()
        }
    }


}
