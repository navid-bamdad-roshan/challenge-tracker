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
import android.widget.ArrayAdapter
import android.widget.Chronometer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            btn_startStop.text = getString(R.string.stop)
            c_meter.start()
        }

        meter = c_meter
        text_dist.text = "${round(totaldist/10f)/100f} km"
        //get possible activities
        //set dropdown

            var activity = mutableListOf<ChallengeActivity>(ChallengeActivity("Select an activity", 0f, ""))
            DataBaseHelper.getChallengeById("eCYl9TuShYqjQEfvfLiR"){ challenge ->
                challenge.activities.forEach(){
                    activity.add(it)
                }
            }
            spinner_activity.adapter=ArrayAdapter(this, android.R.layout.simple_spinner_item, activity)

        setButton()

        //register receiver for Broadcasts from GPS service
        val filter = IntentFilter(LocationReceiver.LOCATION_ACTION)
        registerReceiver(locationReceiver, filter)
    }

    private fun setButton() {
        btn_startStop.setOnClickListener {
            if(activityActive) {
                meter?.let {
                        meter ->
                    Log.i(TAG, "elapsed time ${SystemClock.elapsedRealtime() - meter.base}")
                    meter.stop()
                }
                GpsService.stopTracking(this)
                //calculate points
                val activity = spinner_activity.selectedItem as ChallengeActivity

                val points = totaldist*activity.pointPerKm
                Log.i(TAG, "points of activity: ${activity.pointPerKm}")
                //submit activity
               // DataBaseHelper.addNewUserActivity("name", points,DataBaseHelper.getCurrentChallengeId(), activity, DataBaseHelper.getCurrentChallengeName())

                //release dropdown
                spinner_activity.isEnabled = true


                btn_startStop.text = getString(R.string.start)
                activityActive = false
            }else{
                if(spinner_activity.selectedItemPosition != 0) {
                    Log.i(TAG, "startService, activity pos ok")
                    //reset
                    totaldist = 0f
                    meter?.base = SystemClock.elapsedRealtime();
                    MapsFragment.points.clear()
                    fragment.clearMap()

                    //start activity
                    //block dropdown
                    spinner_activity.isEnabled = false

                    meter?.start()
                    startService(GpsService.getIntent(this))
                    text_dist.text = "${round(totaldist / 10f) / 100f} km"
                    btn_startStop.text = getString(R.string.stop)
                    activityActive = true
                }else{
                    Log.i(TAG, "no activity selected")
                    Toast.makeText(applicationContext, "Please select an activity", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun updateMap(location:Location){
        Log.i(TAG, "update $totaldist")
        text_dist.text = "${round(totaldist/10f)/100f} km"
        fragment.updateMap(location)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
    }

}