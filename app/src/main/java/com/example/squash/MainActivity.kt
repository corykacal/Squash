package com.example.squash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import androidx.fragment.app.Fragment
import com.example.squash.api.MainViewModel
import com.example.squash.api.User
import com.example.squash.api.photoapi
import com.example.squash.posts.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.action_bar.*
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.squash.intro.IntroActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    val TAG = "####"

    private var mFusedLocationClient: FusedLocationProviderClient? = null


    companion object {
        var newPost = true
        lateinit var viewModel: MainViewModel
        val REQUEST_PERMISSIONS_REQUEST_CODE = 12
    }



    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private lateinit var homeFragment: HomeFragment
    fun launchNewFragment(fragment: Fragment, tag: Int) {
        var replaceFrag = supportFragmentManager.findFragmentByTag(tag.toString())
        if(replaceFrag==null) {
            replaceFrag = fragment
            supportFragmentManager
                .beginTransaction()
                // No back stack for home
                .add(R.id.main_frame, replaceFrag, tag.toString())
                // TRANSIT_FRAGMENT_FADE calls for the Fragment to fade away. causes crash
                //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                // No back stack for home
                .replace(R.id.main_frame, replaceFrag, tag.toString())
                // TRANSIT_FRAGMENT_FADE calls for the Fragment to fade away. causes crash
                //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()

        }
    }

    private fun observePoints() {
        viewModel.observeUserData().observe(this, Observer {
            userPoints.text = (it.post_up!!+it.comment_up!!).toString()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        everything.isVisible = false
        findViewById<bottomNavBar>(R.id.bar).setInstance(this)
        auth = FirebaseAuth.getInstance()
        var user = User(auth) {}
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel = MainViewModel()
        viewModel.init(user, photoapi(resources), mFusedLocationClient)
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        startApplication()
    }

    public override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            var intent = Intent(applicationContext, IntroActivity::class.java)
            intent.putExtra("isComment", false)
            startActivityForResult(intent, 2)
        } else {
            startApplication()
        }
    }

    //TODO make this not horrible. make a check. maybe a fragment. for sure a callback somehwere
    private fun startApplication() {
        viewModel.requestNewLocationData {  }
        everything.isVisible = true
        homeFragment = HomeFragment.newInstance()
        launchNewFragment(homeFragment, R.id.posts_icon)
        observePoints()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}
