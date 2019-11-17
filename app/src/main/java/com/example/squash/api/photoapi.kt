package com.example.squash.api


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

    fun loadFileToTV(localFile: String, textView: ImageView) {
        val bitmap = BitmapFactory.decodeFile(localFile)
        val bitmapResized = Bitmap.createBitmap(bitmap)
        val drawable = BitmapDrawable(resources, bitmapResized)
        //val drawable = Drawable.createFromPath(currentPhotoPath)
        //textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        textView.setImageDrawable(drawable)
    }

    fun downloadImg(uuid: String, textView: ImageView, func: (Boolean) -> Unit) {
        if(uuid2localpath.containsKey(uuid)) {
            loadFileToTV(uuid2localpath[uuid]!!, textView)
            func(true)
        } else {

            val imgRef = photoStorage.child("images/$uuid")
            val localFile = File.createTempFile("images", ".jpg")

            imgRef.getFile(localFile).addOnSuccessListener {
                uuid2localpath[uuid] = localFile.absolutePath
                loadFileToTV(localFile.absolutePath, textView)
                func(true)
            }.addOnFailureListener {
                Log.d("failed to download file", "$uuid")
                func(false)
            }

        }
    }
}
