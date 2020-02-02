package com.example.squash.posts

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.squash.R
import com.example.squash.api.MainViewModel
import com.example.squash.api.User
import com.example.squash.api.photoapi
import com.example.squash.technology.Constants.Companion.IMAGE_PICK_CODE
import com.example.squash.technology.Constants.Companion.PERMISSION_CODE
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_new_post.*
import java.io.File
import java.util.*


class NewPostActivity(): AppCompatActivity() {

    private var reply_to: Long? = null
    private var imageURI: Uri? = null
    private lateinit var viewModel: MainViewModel


    /*
     * Function to open the on-screen keyboard
     */
    private fun openKeybaord() {
        editPostText.postDelayed(object : Runnable {
            override fun run() {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editPostText, InputMethodManager.SHOW_IMPLICIT)
            }
        }, 100)
    }


    /*
     * Function to hide the on-screen keyboard
     */
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


    /*
     * Displays the error in the text box right below the edit text box.
     * param:
     *  error: the string to be displayed
     */
    private fun displayError(error: String) {
        postError.text = error
        pulseAnimation(postError)
    }


    /*
     * Will apply a pulsing animation on the given textView.
     * param:
     *  textView: the textview you would like to pulse
     */
    private fun pulseAnimation(textView: TextView) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 110 //You can manage the blinking time with this parameter
        anim.startOffset = 60
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 3
        textView.startAnimation(anim)
    }


    /*
     * Sets up the listener for the post button.
     */
    private fun initPostButton() {
        postButton.setOnClickListener {
            var contents = editPostText.text.toString()
            var subject: String? = null
            if(spinner.selectedItemId.toInt()!=0) {
                 subject = spinner.selectedItem.toString()
            }
            if(contents.isBlank()) {
                displayError("Error: can't send blank post")
            } else if(contents.lines().size>10) {
                displayError("Error: too many lines: ${contents.lines().size}, max: 10")
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


    /*
     * Override android back button
     */
    override fun onBackPressed() {
        //no post made, result is canceled, don't refresh
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }


    /*
     * Callback for every time the text changes. This is to let the user know how many
     * characters they have left.
     */
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
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            openKeybaord()
            imageURI = data?.data
            val imagePath = getRealPathFromURI(imageURI.toString())
            val file = File(imagePath)
            val size = file.length()
            Log.d("size is:", "$size")
            if(size>625000)  {
                displayError("5MB filesize limit")
            } else {
                selectedImage.setImageURI(imageURI)
                imageVisible(true)
            }
        }
    }

    fun getRealPathFromURI(contentURI: String?): String? {
        val contentUri = Uri.parse(contentURI)
        val projection =
            arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null
        try {
            cursor = if (Build.VERSION.SDK_INT > 19) { // Will return "image:x*"
                val wholeID = DocumentsContract.getDocumentId(contentUri)
                // Split at colon, use second item in the array
                val id = wholeID.split(":").toTypedArray()[1]
                // where id is equal to
                val sel = MediaStore.Images.Media._ID + "=?"
                applicationContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, sel, arrayOf(id), null
                )
            } else {
                applicationContext.getContentResolver().query(
                    contentUri,
                    projection, null, null, null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var path: String? = null
        try {
            val column_index: Int = cursor!!
                .getColumnIndex(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            path = cursor.getString(column_index).toString()
            cursor.close()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return path
    }

    private fun initSpinner(currentSubject: String) {
        viewModel.getSubjects { success: Boolean ->
            if(success) {
                var index = 0
                var subjectIndex = 0
                val subjectsAllArray = viewModel.observeSubjects().value
                val subjectsArray = mutableListOf<String>()
                subjectsAllArray?.forEach {
                    subjectsArray.add(it.subject!!)
                }
                subjectsArray.forEach {
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
        var user = User(auth) { success, user ->
            if(!success) {
                Toast.makeText(applicationContext, "please check internet connection", Toast.LENGTH_LONG)
                finish()
            }
        }
        var mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel = MainViewModel()
        viewModel.init(user, photoapi(resources), mFusedLocationClient)

        //need location before you can post
        viewModel.startLocationServices()

        initSpinner(currentSubject)
        imageVisible(false)
        listenToEdit()
        initPostButton()
        initPictureButton()
        initDeleteButton()
    }
}














