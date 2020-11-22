package com.example.challengetracker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_maps.*
import java.lang.Math.round


class MapsActivity : AppCompatActivity(){
    companion object{
        var totaldist = 0f
        var activityActive = false
        val TAG = "MapsActivity"
        var meter :Chronometer? = null

    }
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private val REQUEST_LOCATION = 7
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
        if(activityActive){
            c_meter.base = meter!!.base
            Log.i(TAG, "base time for new chrono ${SystemClock.elapsedRealtime() - meter!!.base}")
            btn_startStop.text = getString(R.string.stop)
            Log.i(TAG, "start chrono")
            c_meter.start()
        }
        meter = c_meter
        text_dist.text = "${round(totaldist/10f)/100f} km"
        btn_startStop.setOnClickListener {
            if(activityActive) {
                meter?.let {
                    meter ->
                    Log.i(TAG, "elapsed time ${SystemClock.elapsedRealtime() - meter.base}")
                    meter.stop()
                }
                GpsService.stopTracking(this)
                btn_startStop.text = getString(R.string.start)
                Log.i(TAG, "stopButton")
                activityActive = false
            }else{
                Log.i(TAG, "startService")
                meter?.base = SystemClock.elapsedRealtime();
                meter?.start()
                startService(GpsService.getIntent(this))
                text_dist.text = "${round(totaldist/10f)/100f} km"
                btn_startStop.text = getString(R.string.stop)
                activityActive = true
            }
        }
        val filter = IntentFilter(LocationReceiver.LOCATION_ACTION)
        registerReceiver(locationReceiver, filter)
    }

    fun updateMap(location:Location){
        Log.i(TAG, "update $totaldist")
        text_dist.text = "${round(totaldist/10f)/100f} km"
        MapsFragment.points.add(LatLng(location.latitude, location.longitude))
        fragment.updateMap(location)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
    }


}