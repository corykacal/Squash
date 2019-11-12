package com.example.squash.posts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.squash.R
import kotlinx.android.synthetic.main.activity_new_post.*
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.content.Context.INPUT_METHOD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.widget.EditText
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.squash.MainActivity


class NewPostActivity: AppCompatActivity() {

    private var isComment: Boolean? = null
    private var reply_to: Long? = null
    var viewModel = MainActivity.viewModel

    private fun openKeybaord() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun initPostButton() {
        postButton.setOnClickListener {
            var contents = editPostText.text.toString()
            if(!contents.isBlank()) {
                viewModel.makePost(contents, null, reply_to)
                hideKeyboard()
                finish()
            } else {
                postError.text = "Error: can't send blank post"
            }
        }
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

    private fun listenToEdit() {
        editPostText.addTextChangedListener {
            val text = it.toString()
            val length = text.length
            val remain = 365-length
            textLeft.text = (remain).toString()
            if(remain==0) {
                textLeft.setTextColor(ContextCompat.getColor(textLeft.context, R.color.badComment))
            } else {
                textLeft.setTextColor(ContextCompat.getColor(textLeft.context, R.color.black))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.fade_int, R.anim.fade_out)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        val intent = intent
        isComment = intent.getBooleanExtra("isComment", false)
        if(isComment!!) {
            reply_to = intent.getLongExtra("reply_to", 0)
        }


        listenToEdit()
        openKeybaord()
        initPostButton()

    }
}