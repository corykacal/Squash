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

    fun makePost(contents: String, imageuuid: String?,
                         reply_to: Long?, opuuid: String): Boolean {
        Log.d("PostRepository", "makeing a commmand swasg")
        var request = postApi.makePost(imageuuid, reply_to, opuuid, contents)
        var results = request.execute()
        return results.isSuccessful
    }

    fun getPosts(): List<Post>? {
        val request = postApi.getPost()
        var results = request.execute()
        if(results.isSuccessful) {
            return results.body()!!.results
        } else {
            return mutableListOf()
        }
    }


}
