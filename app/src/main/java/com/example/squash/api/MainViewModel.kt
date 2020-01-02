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
import com.example.squash.technology.Constants.Companion.PAGE_SIZE
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
    private var chat = MutableLiveData<List<Post>>()
    private var myPost = MutableLiveData<List<Post>>()
    private var singlePost = MutableLiveData<Post>()
    private var chatListener : ListenerRegistration? = null
    private var singlePostComments = MutableLiveData<List<Post>>()
    private var userData = MutableLiveData<UserData>()
    private var currentSubject = MutableLiveData<String>()

    companion object {
        private lateinit var postFetch: PostApi
        private lateinit var auth: User
        private lateinit var storage: photoapi
    }

    fun init(authy: User, storagey: photoapi) {
        db = FirebaseFirestore.getInstance()
        auth = authy
        storage = storagey
        postFetch = PostApi.create()
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

    fun downloadImgThumb(uuid: String, textView: ImageView, func: (Boolean) -> Unit) {
        storage.downloadImgThumb(uuid, textView, func)
    }


    fun clearComments() {
        singlePostComments.postValue(null)
    }

    fun getUUID(): String? {
        return auth!!.getUid()
    }

    fun observeSubject(): LiveData<String> {
        return currentSubject
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

    fun observeUserData(): LiveData<UserData> {
        return userData
    }

    fun observeMyPost(): LiveData<List<Post>> {
        return myPost
    }

    fun setCurrentSubject(subject: String) {
        currentSubject.postValue(subject)
    }

    fun getUserData(func: (Boolean) -> Unit) {
        var uuid = getUUID()!!
        var task = postFetch.getUserData(uuid)
        task.enqueue(object : Callback<PostApi.UserDataReponse> {
            override fun onResponse(call: Call<PostApi.UserDataReponse>?, response: Response<PostApi.UserDataReponse>?) {
                var data = response!!.body()!!.results
                if(data.size==0) {
                    userData.postValue(UserData(0,0,0,0,0,0,0,0,0,0))
                } else {
                    userData.postValue(data[0])
                }
                func(true)
            }
            override fun onFailure(call: Call<PostApi.UserDataReponse>?, t: Throwable?) {
                func(false)
            }
        })

    }

    fun getComments(post_number: Long, func: (Boolean) -> Unit) {
        var uuid = getUUID()!!
        var task = postFetch.getComments(post_number, uuid)
        task.enqueue(object : Callback<PostApi.ListingResponse> {
            override fun onResponse(call: Call<PostApi.ListingResponse>?, response: Response<PostApi.ListingResponse>?) {
                var posts = response!!.body()!!.results
                posts = posts.sortedBy { it.timestamp }
                singlePostComments.postValue(posts)
                func(true)
            }
            override fun onFailure(call: Call<PostApi.ListingResponse>?, t: Throwable?) {
                func(false)
            }
        })

    }

    fun getSinglePost(post_number: Long, func: (Boolean) -> Unit) {
        val task = postFetch.getSinglePost(post_number, getUUID()!!)
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

    fun makeDescition(post_number: Long, descision: Boolean?, func: (Boolean) -> Unit) {
        val opuuid = getUUID()!!
        val task = postFetch.makeDescision(opuuid, post_number, descision)
        task.enqueue(object : Callback<PostApi.PostResponse> {
            override fun onResponse(call: Call<PostApi.PostResponse>?, response: Response<PostApi.PostResponse>?) {
                func(true)
            }
            override fun onFailure(call: Call<PostApi.PostResponse>?, t: Throwable?) {
                func(false)
            }
        })
    }

    fun makePost(contents: String, subject: String?, imageuri: Uri?,
                 imageuuid: String?, reply_to: Long?, func: (Boolean) -> Unit) {
        var opuuid = getUUID()!!
        var task = postFetch.makePost(imageuuid, reply_to, opuuid!!, contents, subject)
        if(imageuuid!=null) {
            var imageTask = uploadJpg(imageuri!!, imageuuid)
            imageTask.addOnSuccessListener {
                func(true)
                task.enqueue(object : Callback<PostApi.PostResponse> {
                    override fun onResponse(
                        call: Call<PostApi.PostResponse>?,
                        response: Response<PostApi.PostResponse>?
                    ) {
                        func(true)
                    }

                    override fun onFailure(call: Call<PostApi.PostResponse>?, t: Throwable?) {
                        func(false)
                    }
                })
            }.addOnFailureListener {
                func(false)
            }
        } else {
            task.enqueue(object : Callback<PostApi.PostResponse> {
                override fun onResponse(
                    call: Call<PostApi.PostResponse>?,
                    response: Response<PostApi.PostResponse>?
                ) {
                    func(true)
                }

                override fun onFailure(call: Call<PostApi.PostResponse>?, t: Throwable?) {
                    func(false)
                }
            })
        }
    }

    fun getMyPost(number_of_post: Int?, func: (Boolean) -> Unit) {
        var uuid = getUUID()
        var task = postFetch.getMyPosts(uuid!!, number_of_post, 1)
        task.enqueue(object : Callback<PostApi.ListingResponse> {
            override fun onFailure(call: Call<PostApi.ListingResponse>?, t: Throwable?) {
                func(false)
            }
            override fun onResponse(call: Call<PostApi.ListingResponse>?, response: Response<PostApi.ListingResponse>?) {
                func(true)
                var posts = response!!.body()!!.results
                myPost.postValue(posts)
            }
        })
    }

    fun getPosts(number_of_post: Int, page_number: Int, func: (Boolean) -> Unit) {
        val subject = currentSubject.value
        var uuid = getUUID()
        var task: Call<PostApi.ListingResponse>
        if(!MainActivity.newPost) {
            task = postFetch.getHotPosts(uuid!!, number_of_post, page_number, subject)
        } else {
            task = postFetch.getRecentPosts(uuid!!, number_of_post, page_number, subject)
        }
        task.enqueue(object : Callback<PostApi.ListingResponse> {
            override fun onFailure(call: Call<PostApi.ListingResponse>?, t: Throwable?) {
                func(false)
            }
            override fun onResponse(call: Call<PostApi.ListingResponse>?, response: Response<PostApi.ListingResponse>?) {
                if(response!!.isSuccessful) {
                    func(true)
                    var posts = response!!.body()!!.results
                    var currentPosts: MutableList<Post>? = null
                    if(page_number == 1) {
                        currentPosts = mutableListOf<Post>()
                    } else {
                        currentPosts = chat.value?.toMutableList()
                    }


                    if(currentPosts == null) {
                        chat.postValue(posts)
                    } else {
                        currentPosts.addAll(posts)
                        chat.postValue(currentPosts)
                    }
                } else {
                    func(false)
                }
            }
        })
    }

    // Debateable how useful this is.
    override fun onCleared() {
        super.onCleared()
        chatListener?.remove()
    }
}
