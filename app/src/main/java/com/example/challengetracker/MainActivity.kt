package com.example.challengetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.*


class MainActivity : AppCompatActivity(){
    private lateinit var mMap: GoogleMap
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private val REQUEST_LOCATION = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

}