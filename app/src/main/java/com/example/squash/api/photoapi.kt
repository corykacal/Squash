package com.example.squash.api


import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.File


class photoapi(private val resources: Resources) {
    private val photoStorage: StorageReference

    init {
        photoStorage = FirebaseStorage.getInstance().reference.child("images")
    }

    fun uploadImg(localPath: String, uuid: String) {
        val riversRef = photoStorage.child("images/$uuid")
        var file = Uri.fromFile(File(localPath))
        var uploadTask = riversRef.putFile(file)

        uploadTask.addOnFailureListener {
            Log.d("%%%%%% fail %%%%%%%", "$it")
        }.addOnSuccessListener {
            Log.d("image uploaded", "image uploaded")
        }
    }

    fun loadFileToTV(localFile: String, textView: TextView) {
        val bitmap = BitmapFactory.decodeFile(localFile)
        val bitmapResized = Bitmap.createScaledBitmap(bitmap, 500, 500, false)
        val drawable = BitmapDrawable(resources, bitmapResized)
        //val drawable = Drawable.createFromPath(currentPhotoPath)
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }

    fun downloadImg(uuid: String, textView: TextView): String {
        val imgRef = photoStorage.child("images/$uuid")
        val localFile = File.createTempFile("images", ".jpg")

        imgRef.getFile(localFile).addOnSuccessListener {
            loadFileToTV(localFile.absolutePath, textView)
        }.addOnFailureListener {
            Log.d("failed to download file", "$uuid")
        }

        return localFile.absolutePath
    }
}
