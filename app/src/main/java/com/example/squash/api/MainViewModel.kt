package com.example.squash.api

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.squash.api.posts.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class MainViewModel : ViewModel() {
    private lateinit var db: FirebaseFirestore
    private var auth: User? = null
    private lateinit var storage: photoapi
    private var chat = MutableLiveData<List<Post>>()
    private var singlePost = MutableLiveData<Post>()
    private var chatListener : ListenerRegistration? = null
    // Ouch, this is a very poor man's cache
    private var uuid2localpath = mutableMapOf<String,String>()

    companion object {
        var postFetch = PostApi.create()
        var postRepository = PostRepository(postFetch)
    }

    fun init(auth: User, storage: photoapi) {
        db = FirebaseFirestore.getInstance()
        if (db == null) {
            Log.d("XXX", "XXX FirebaseFirestore is null!")
        }
        this.auth = auth
        this.storage = storage
    }

    fun observePosts(): LiveData<List<Post>> {
        return chat
    }

    fun observeSinglePost(): LiveData<Post> {
        return singlePost
    }

    fun getTime(postDate: Date): String {
        var res = ""
        val stamp = java.sql.Timestamp(System.currentTimeMillis())
        val date = Date(stamp.getTime())
        val dateDifference = Date(date.time-postDate.time)
        val time = dateDifference.time
        val seconds = time/1000
        val minutes = seconds/60
        val hours = minutes/60
        val days = hours/24
        val possibleValues = listOf(days, hours, minutes, seconds)
        Log.d("days ago was:", "${dateDifference.time}")
        val possibleTickers = listOf(" d", " h", " m", " s")
        for (i in possibleValues.indices) {
            val currentValue = possibleValues[i]
            if(currentValue!=0L) {
                res = currentValue.toString() + possibleTickers[i]
                break
            }
        }
        return res
    }

    fun getSinglePost(post_number: Long) {
        viewModelScope.launch(
            context = viewModelScope.coroutineContext
                    + Dispatchers.IO) {
            var post = postRepository.getSinglePost(post_number)
            Log.d("we got back this:","$post")
            singlePost.postValue(post)
        }
    }

    fun makePost(contents: String, imageuuid: String?, reply_to: Long?) {
        viewModelScope.launch(
            context = viewModelScope.coroutineContext
                    + Dispatchers.IO) {
            // Update LiveData from IO dispatcher, use postValue
            var opuuid = auth?.getUid()
            Log.d("MainViewModel", "$opuuid")
            postRepository.makePost(contents, imageuuid, reply_to, opuuid!!)
        }
    }

    fun getChat() {
        // XXX Write me.  Limit total number of chat rows to 100
        viewModelScope.launch(
            context = viewModelScope.coroutineContext
                    + Dispatchers.IO) {
            // Update LiveData from IO dispatcher, use postValue
            var posts = postRepository.getPosts()
            chat.postValue(posts)
        }
    }


    // Debateable how useful this is.
    override fun onCleared() {
        super.onCleared()
        chatListener?.remove()
    }
}
