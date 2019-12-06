package com.example.squash.posts.subContent

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler
import com.example.squash.R
import com.example.squash.api.MainViewModel
import com.example.squash.posts.NewPostActivity
import com.example.squash.technology.SingleClickListener
import kotlinx.android.synthetic.main.activity_new_post.*
import java.util.*


class ImageFragment: Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var toolbar: Toolbar
    private lateinit var root: View


    companion object {
        fun newInstance(): ImageFragment {
            return ImageFragment()
        }

        //image download code
        private val IMAGE_DOWNLOAD_CODE = 1002;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    private fun setBackButton(root: View) {
        val backButton = root.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener(object: SingleClickListener() {
            override fun onSingleClick(v: View) {
                activity?.onBackPressed()
            }
        })
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    saveImage()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(root.context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setDownloadButton(root: View) {
        val downloadButton = root.findViewById<ImageView>(R.id.downloadButton)
        downloadButton.setOnClickListener(object: SingleClickListener() {
            override fun onSingleClick(v: View) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if (activity?.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED){
                        //permission denied
                        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        //show popup to request runtime permission
                        requestPermissions(permissions, IMAGE_DOWNLOAD_CODE)
                    }
                    else{
                        saveImage()
                    }
                }
                else{
                    saveImage()
                }
            }
        })
    }

    private fun saveImage() {

        // Save image to gallery

        val imageView = root.findViewById<ImageView>(R.id.image)
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val title = UUID.randomUUID().toString()

        val savedImageURL = MediaStore.Images.Media.insertImage(
            activity?.contentResolver,
            bitmap,
            title,
            "Image of $title"
        )

        Toast.makeText(root.context, "saved to gallery", Toast.LENGTH_LONG).show()
    }

    private fun setImage(root: View, uuid: String?) {
        val imageView = root.findViewById<ImageView>(R.id.image)
        imageView.setOnTouchListener(object: ImageMatrixTouchHandler(context){})
        val lambda = { success: Boolean ->
            if(!success) {
                Toast.makeText(root.context, "failed to get image", Toast.LENGTH_LONG).show()
                activity?.onBackPressed()
            }
        }
        if(uuid!=null) {
            viewModel.downloadImg(uuid, imageView, lambda)
        } else {
            Toast.makeText(root.context, "how did I even get here?", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProviders.of(activity!!)[MainViewModel::class.java]

        root = inflater.inflate(R.layout.image_fragment, container, false)

        val bundle = arguments
        val imageUUID = bundle?.getString("imageuuid")

        setBackButton(root)
        setDownloadButton(root)
        setImage(root, imageUUID)


        return root
    }
}
