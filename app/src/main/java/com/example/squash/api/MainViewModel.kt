package com.example.squash.api

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.squash.MainActivity
import com.example.squash.api.posts.Post
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MainViewModel : ViewModel() {
    private lateinit var db: FirebaseFirestore
    private var auth: User? = null
    private lateinit var storage: photoapi
    private var chat = MutableLiveData<List<Post>>()
    private var singlePost = MutableLiveData<Post>()
    private var chatListener : ListenerRegistration? = null
    private var singlePostComments = MutableLiveData<List<Post>>()
    // Ouch, this is a very poor man's cache

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

    fun uploadJpg(imageURI: Uri, uuid: String): UploadTask {
        return storage.uploadImg(imageURI, uuid)
    }

    // Very poor man's cache.  I should really use glide
    fun downloadImg(uuid: String, textView: ImageView, func: (Boolean) -> Unit) {
        storage.downloadImg(uuid, textView, func)
    }


    fun clearComments() {
        singlePostComments.postValue(null)
    }

    fun getUUID(): String? {
        return auth?.getUid()
    }

    fun observePosts(): LiveData<List<Post>> {
        return chat
    }

    fun observeSinglePost(): LiveData<Post> {
        return singlePost
    }

    fun observeComments(): LiveData<List<Post>> {
        return singlePostComments
    }

    fun getComments(post_number: Long, func: (Boolean) -> Unit) {
        var uuid = getUUID()!!
        var task = postRepository.getComments(post_number, uuid)
        task.enqueue(object : Callback<PostApi.ListingResponse> {
            override fun onResponse(call: Call<PostApi.ListingResponse>?, response: Response<PostApi.ListingResponse>?) {
                var posts = response!!.body()!!.results
                posts = posts?.sortedBy { it.timestamp }
                singlePostComments.postValue(posts)
                func(true)
            }
            override fun onFailure(call: Call<PostApi.ListingResponse>?, t: Throwable?) {
                func(false)
            }
        })

    }

    fun getSinglePost(post_number: Long, func: (Boolean) -> Unit) {
        val task = postRepository.getSinglePost(post_number, getUUID()!!)
        task.enqueue(object : Callback<PostApi.ListingResponse> {
            override fun onResponse(call: Call<PostApi.ListingResponse>?, response: Response<PostApi.ListingResponse>?) {
                var post = response!!.body()!!.results[0]
                singlePost.postValue(post)
                func(true)
            }
            override fun onFailure(call: Call<PostApi.ListingResponse>?, t: Throwable?) {
                func(false)
            }
        })
    }

    fun makeDescition(opuuid: String, post_number: Long, descision: Boolean?, func: (Boolean) -> Unit) {
        val task = postRepository.makeDescision(opuuid, post_number, descision)
        task.enqueue(object : Callback<PostApi.PostResponse> {
            override fun onResponse(call: Call<PostApi.PostResponse>?, response: Response<PostApi.PostResponse>?) {
                getChat(100,{success: Boolean ->})
                func(true)
            }
            override fun onFailure(call: Call<PostApi.PostResponse>?, t: Throwable?) {
                func(false)
            }
        })
    }

    fun makePost(contents: String, imageuri: Uri?,
                 imageuuid: String?, reply_to: Long?, func: (Boolean) -> Unit) {
        var opuuid = getUUID()!!
        var task = postRepository.makePost(contents, imageuuid, reply_to, opuuid!!)
        if(imageuuid!=null) {
            var task = uploadJpg(imageuri!!, imageuuid)
            task.addOnSuccessListener {
                getChat(100,{success: Boolean ->})
                func(true)
            }.addOnFailureListener {
                func(false)
            }
        }
        task.enqueue(object : Callback<PostApi.PostResponse> {
            override fun onResponse(call: Call<PostApi.PostResponse>?, response: Response<PostApi.PostResponse>?) {
                if(imageuuid==null) {
                    if(reply_to!=null) {
                        getComments(reply_to!!, {success: Boolean ->})
                    } else {
                        getChat(100,{success: Boolean ->})
                    }
                    func(true)
                }
            }
            override fun onFailure(call: Call<PostApi.PostResponse>?, t: Throwable?) {
            }
        })
    }

    fun getChat(number_of_post: Int?, func: (Boolean) -> Unit) {
        var uuid = getUUID()
        var task = postRepository.getPosts(uuid!!, number_of_post)
        task.enqueue(object : Callback<PostApi.ListingResponse> {
            override fun onFailure(call: Call<PostApi.ListingResponse>?, t: Throwable?) {
                func(false)
            }
            override fun onResponse(call: Call<PostApi.ListingResponse>?, response: Response<PostApi.ListingResponse>?) {
                func(true)
                var posts = response!!.body()!!.results
                if(!MainActivity.newPost) {
                    posts = posts?.sortedBy { -(it.up!! - it.down!!) }
                }
                chat.postValue(posts)
            }
        })
    }


    // Debateable how useful this is.
    override fun onCleared() {
        super.onCleared()
        chatListener?.remove()
    }
}
