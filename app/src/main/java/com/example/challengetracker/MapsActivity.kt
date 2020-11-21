package com.example.challengetracker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(){
    companion object{
        var totaldist = 0f
    }
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private val REQUEST_LOCATION = 7
    var activityActive = false
    lateinit var fragment : MapsFragment
    private val locationReceiver : BroadcastReceiver= LocationReceiver().apply {
        setMapsActivityHandler(this@MapsActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragment = MapsFragment()
        fragmentTransaction.add(R.id.mapsFragment, fragment)
        fragmentTransaction.commit()


        btn_startStop.setOnClickListener {
            if(activityActive) {
                btn_startStop.text = getString(R.string.start)
                Log.i("MapsActivity", "startButton")


                activityActive = false
            }else{
                btn_startStop.text = getString(R.string.stop)
                activityActive = true
            }
        }
        val filter = IntentFilter(LocationReceiver.LOCATION_ACTION)
        registerReceiver(locationReceiver, filter)
        Log.i("MapsActivity", "startService")
        startService(GpsService.getIntent(this))
    }

    fun updateMap(location:Location){
        fragment.updateMap(location)
    }

    override fun onDestroy() {
        super.onDestroy()
        GpsService.stopTracking(this)
        unregisterReceiver(locationReceiver)
    }


}