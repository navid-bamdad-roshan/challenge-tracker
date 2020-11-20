package com.example.challengetracker

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(){
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private val REQUEST_LOCATION = 7
    var activityActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        btn_startStop.setOnClickListener {
            if(activityActive) {
                btn_startStop.text = getString(R.string.start)

                activityActive = false
            }else{
                btn_startStop.text = getString(R.string.stop)
                activityActive = true
            }
        }
    }

}