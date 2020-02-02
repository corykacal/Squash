package com.example.squash.api


import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import okhttp3.internal.wait
import java.io.File
import android.media.ThumbnailUtils
import android.provider.MediaStore
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.example.squash.MainActivity
import com.example.squash.posts.NewPostActivity
import io.grpc.Compressor
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt


class photoapi(private val resources: Resources) {
    private val photoStorage: StorageReference

    companion object {
        private var uuid2localpath = mutableMapOf<String,String>()
    }

    init {
        photoStorage = FirebaseStorage.getInstance().reference.child("images")
    }

    fun uploadImg(file: Uri, uuid: String): UploadTask {
        val riversRef = photoStorage.child("images/$uuid")
        var uploadTask = riversRef.putFile(file)

        return uploadTask
    }


    fun loadFileToTV(localFile: String, textView: ImageView, isThumbnail: Boolean) {
        var bitmap = BitmapFactory.decodeFile(localFile)
        if(isThumbnail) {
            val width = 225.0
            val height = (width/bitmap.width) * bitmap.height
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width.toInt(), height.toInt())
        }
        val drawable = BitmapDrawable(resources, bitmap)
        //val drawable = Drawable.createFromPath(currentPhotoPath)
        //textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        textView.setImageDrawable(drawable)
    }

    fun downloadImgThumb(uuid: String, textView: ImageView, func: (Boolean) -> Unit) {
        if(uuid2localpath.containsKey(uuid)) {
            loadFileToTV(uuid2localpath[uuid]!!, textView, true)
            func(true)
        } else {

            val imgRef = photoStorage.child("images/$uuid")
            val localFile = File.createTempFile("images", ".jpg")

            imgRef.getFile(localFile).addOnSuccessListener {
                uuid2localpath[uuid] = localFile.absolutePath
                loadFileToTV(localFile.absolutePath, textView, true)
                func(true)
            }.addOnFailureListener {
                Log.d("failed to download file", "$uuid")
                func(false)
            }

        }
    }

    fun downloadImg(uuid: String, textView: ImageView, func: (Boolean) -> Unit) {
        if(uuid2localpath.containsKey(uuid)) {
            loadFileToTV(uuid2localpath[uuid]!!, textView, false)
            func(true)
        } else {

            val imgRef = photoStorage.child("images/$uuid")
            val localFile = File.createTempFile("images", ".jpg")

            imgRef.getFile(localFile).addOnSuccessListener {
                uuid2localpath[uuid] = localFile.absolutePath
                loadFileToTV(localFile.absolutePath, textView, false)
                func(true)
            }.addOnFailureListener {
                Log.d("failed to download file", "$uuid")
                func(false)
            }

        }
    }
}
