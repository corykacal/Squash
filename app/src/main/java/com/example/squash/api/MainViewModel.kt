package com.example.squash.api

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.squash.api.posts.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class MainViewModel : ViewModel() {
    private lateinit var db: FirebaseFirestore
    private var auth: User? = null
    private lateinit var storage: photoapi
    private var chat = MutableLiveData<List<Post>>()
    private var chatListener : ListenerRegistration? = null
    // Ouch, this is a very poor man's cache
    private var uuid2localpath = mutableMapOf<String,String>()

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
    fun sendPost(post: Post) {
        Log.d(
            "HomeViewModel",
            String.format(
                "saveChatRow ownerUid(%s) name(%s) %s",
                post.contents,
                post.postID,
                post.timestamp
            )
        )
        // XXX Write me
        // https://firebase.google.com/docs/firestore/manage-data/add-data#add_a_document
        db.collection("post").document()
            .set(
                Post(
                    post.contents,
                    post.imageUUID,
                    post.postID,
                    auth!!.getUid(),
                    com.google.firebase.Timestamp.now()
                )
            )

    }

    fun getChat() {
        // XXX Write me.  Limit total number of chat rows to 100
        db.collection("post").limit(100).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.w("####", "listen:error", firebaseFirestoreException)
                return@addSnapshotListener
            }
            Log.d("####", "fetch ${querySnapshot!!.documents.size}")
            chat.value = querySnapshot.documents.mapNotNull {
                it.toObject(Post::class.java)
            }
        }

    }

    fun uploadJpg(localPath: String, uuid: String) {
        storage.uploadImg(localPath, uuid)
    }
    // Very poor man's cache.  I should really use glide
    fun downloadJpg(uuid: String, textView: TextView) {
        if(uuid2localpath.containsKey(uuid)) {
            Log.d("####", "local load $uuid")
            storage.loadFileToTV(uuid, textView)
        } else {
            Log.d("####", "remote load $uuid")
            uuid2localpath[uuid] = storage.downloadImg(uuid, textView)
        }
    }

    // Debateable how useful this is.
    override fun onCleared() {
        super.onCleared()
        chatListener?.remove()
    }
}
