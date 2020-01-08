package com.example.squash.posts

import android.Manifest
import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.squash.R
import kotlinx.android.synthetic.main.activity_new_post.*
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.content.Context.INPUT_METHOD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.os.Build
import android.renderscript.ScriptGroup
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import android.text.Spannable
import android.text.style.ImageSpan
import android.text.SpannableString
import android.view.animation.Animation
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.example.squash.api.MainViewModel
import com.example.squash.api.User
import com.example.squash.api.photoapi
import com.example.squash.technology.Constants.Companion.PAGE_SIZE
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.row_post.*
import okhttp3.internal.lockAndWaitNanos
import okhttp3.internal.waitMillis
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext


class NewPostActivity(): AppCompatActivity() {

    private var reply_to: Long? = null
    private var imageURI: Uri? = null
    private lateinit var viewModel: MainViewModel


    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    private fun openKeybaord() {
        editPostText.postDelayed(object : Runnable {
            override fun run() {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editPostText, InputMethodManager.SHOW_IMPLICIT)
            }
        }, 100)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0);
    }

    private fun pulseAnimation(textView: TextView) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 100 //You can manage the blinking time with this parameter
        anim.startOffset = 60
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 2
        textView.startAnimation(anim)
    }


    private fun initPostButton() {
        postButton.setOnClickListener {
            var contents = editPostText.text.toString()
            var subject: String? = null
            if(spinner.selectedItemId.toInt()!=0) {
                 subject = spinner.selectedItem.toString()
            }
            if(contents.isBlank()) {
                postError.text = "Error: can't send blank post"
                pulseAnimation(postError)
            } else if(contents.lines().size>10) {
                postError.text = "Error: too many lines: ${contents.lines().size}, max: 10"
                pulseAnimation(postError)
            } else {
                var imageuuid: String? = null
                if(imageURI!=null) {
                    imageuuid = UUID.randomUUID().toString()
                }
                postButton.startAnimation()
                viewModel.makePost(contents, subject,
                    imageURI, imageuuid, reply_to) { success: Boolean ->
                    if(success) {
                        postButton.stopAnimation()
                        hideKeyboard()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        postButton.revertAnimation()
                        Toast.makeText(applicationContext, "post failed", Toast.LENGTH_LONG)
                    }
                    var makeErrorGoAway = 0

                }
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }


    private fun listenToEdit() {
        editPostText.addTextChangedListener {
            postError.text = ""
            val text = it.toString()
            if(text.length!=0 && text.trim()=="") {
                editPostText.setText("")
            } else {
                val length = text.length
                val remain = 365 - length
                textLeft.text = (remain).toString()
                if (remain == 0) {
                    textLeft.setTextColor(
                        ContextCompat.getColor(
                            textLeft.context,
                            R.color.badComment
                        )
                    )
                } else {
                    textLeft.setTextColor(ContextCompat.getColor(textLeft.context, R.color.black))
                }
            }
        }
    }

    private fun initPictureButton() {

        picture.setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    //show popup to request runtime permission
                    requestPermissions(permissions, IMAGE_PICK_CODE)
                }
                else{
                    //permission already granted
                    pickImageFromGallery()
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery()
            }
        }
    }



    private fun pickImageFromGallery() {
        hideKeyboard()
        //Intent to pick image
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }


    private fun imageVisible(isVisible: Boolean) {
        selectedImage.isVisible = isVisible
        deleteImage.isVisible = isVisible
    }

    private fun initDeleteButton() {
        deleteImage.setOnClickListener {
            selectedImage.setImageURI(null)
            imageURI = null
            imageVisible(false)
        }
    }


    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        openKeybaord()
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            selectedImage.setImageURI(data?.data)
            imageURI = data?.data
            imageVisible(true)
        }
    }

    private fun initSpinner(currentSubject: String) {
        viewModel.getSubjects { success: Boolean ->
            if(success) {
                var index = 0
                var subjectIndex = 0
                val subjectsArray = viewModel.observeSubjects().value
                subjectsArray?.forEach {
                    if(it==currentSubject) {
                        subjectIndex = index
                    }
                    index+=1
                }
                val adapter = ArrayAdapter<String>(baseContext,
                    R.layout.spinner_new_post_item, subjectsArray)
                adapter.setDropDownViewResource(R.layout.spinner_new_post_dropdown)
                spinner.adapter = adapter
                spinner.setSelection(subjectIndex)
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.fade_int, R.anim.fade_out)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        val intent = intent
        val currentSubject = intent.getStringExtra("subject")

        var auth = FirebaseAuth.getInstance()
        var user = User(auth) {}
        var mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel = MainViewModel()
        viewModel.init(user, photoapi(resources), mFusedLocationClient)

        //need location before you can post
        viewModel.requestNewLocationData { success: Boolean ->
            if(success) {
                initSpinner(currentSubject)
            }
        }

        imageVisible(false)
        listenToEdit()
        initPostButton()
        initPictureButton()
        initDeleteButton()
    }
}














