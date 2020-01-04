package com.example.squash.intro

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.squash.MainActivity
import com.example.squash.R
import kotlinx.android.synthetic.main.activity_intro.*


class IntroActivity(): AppCompatActivity() {

    companion object {
        val TAG = "$$$$$$$$$$$$"
    }


    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this@IntroActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MainActivity.REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }


    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {

        Toast.makeText(this@IntroActivity, getString(mainTextStringId), Toast.LENGTH_LONG).show()
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

            showSnackbar(R.string.com_crashlytics_android_build_id, android.R.string.ok,
                View.OnClickListener {
                    // Request permission
                    startLocationPermissionRequest()
                })

        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest()
        }
    }

    override fun onBackPressed() {
        //do nothing. can only get back when permission is granted
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == MainActivity.REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                finish()
            }
        }
    }


    private fun listenToButton() {
        locationButton.setOnClickListener {
            requestPermissions()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        //overridePendingTransition(R.anim.fade_int, R.anim.fade_out)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        listenToButton()
    }
}
