package com.example.challengetracker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Chronometer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_maps.*
import java.lang.Math.round


class MapsActivity : AppCompatActivity(){
    companion object{
        var totaldist = 0f
        var activityActive = false
        val TAG = "MapsActivity"
        var meter :Chronometer? = null
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val REQUEST_LOCATION = 7
    }

    lateinit var fragment : MapsFragment
    private val locationReceiver : BroadcastReceiver= LocationReceiver().apply {
        setMapsActivityHandler(this@MapsActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "create")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
       // setupFragment()
        fragment = MapsFragment()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.mapsFragment, fragment).commit()
        fragmentManager.executePendingTransactions()

        setUiElements()
        //get possible activities
        //set dropdown

            var activity = mutableListOf<ChallengeActivity>(ChallengeActivity("Select an activity", 0f, ""))
            DataBaseHelper.getChallengeById("eCYl9TuShYqjQEfvfLiR"){ challenge ->
                challenge.activities.forEach(){
                    activity.add(it)
                }
            }
            spinner_activity.adapter=ArrayAdapter(this, android.R.layout.simple_spinner_item, activity)


        //register receiver for Broadcasts from GPS service
        val filter = IntentFilter(LocationReceiver.LOCATION_ACTION)
        registerReceiver(locationReceiver, filter)
    }

    private fun checkLocationDetectionPermission(permissionArray: Array<String>): Boolean {
        var permissionAllSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                permissionAllSuccess = false
        }
        return permissionAllSuccess
    }

    private fun checkGpsActivated(): Boolean {
        Log.i(TAG, "checkGPS")
        var gpsLocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun setUiElements() {
        if (activityActive) {
            c_meter.base = meter!!.base
            btn_startStop.text = getString(R.string.stop)
            c_meter.start()
        }

        meter = c_meter
        text_dist.text = "${round(totaldist / 10f) / 100f} km"
        btn_startStop.setOnClickListener {
            if (activityActive) {
                meter?.let { meter ->
                    Log.i(TAG, "elapsed time ${SystemClock.elapsedRealtime() - meter.base}")
                    meter.stop()
                }
                GpsService.stopTracking(this)
                //calculate points
                val activity = spinner_activity.selectedItem as ChallengeActivity
                val points = totaldist * activity.pointPerKm
                Log.i(TAG, "points of activity: ${activity.pointPerKm}")
                //submit activity
                 DataBaseHelper.addNewUserActivity("name", points,DataBaseHelper.getCurrentChallengeId(), activity.name, DataBaseHelper.getCurrentChallengeName()){
                     Toast.makeText(applicationContext, "Activity successfully submitted!", Toast.LENGTH_SHORT).show()
                 }
                //release dropdown
                spinner_activity.isEnabled = true
                btn_startStop.text = getString(R.string.start)
                activityActive = false
            } else {
                if (spinner_activity.selectedItemPosition != 0) {
                    if (checkGpsActivated()) {
                        Log.i(TAG, "startService, activity pos ok")
                        //reset
                        resetOldActivity()
                        startNewActivity()
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                           ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION)
                        }else{
                            startService(GpsService.getIntent(this))
                        }

                    } else {
                        Toast.makeText(applicationContext, "Please activate GPS", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.i(TAG, "no activity selected")
                    Toast.makeText(applicationContext, "Please select an activity", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_LOCATION){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startService(GpsService.getIntent(this))
            }
        }
    }
    private fun startNewActivity() {
        spinner_activity.isEnabled = false
        meter?.start()
        text_dist.text = "${round(totaldist / 10f) / 100f} km"
        btn_startStop.text = getString(R.string.stop)
        activityActive = true    }

    private fun resetOldActivity() {
        totaldist = 0f
        meter?.base = SystemClock.elapsedRealtime();
        MapsFragment.points.clear()
        fragment?.clearMap()
    }


    fun updateMap(location:Location){
        Log.i(TAG, "update $totaldist")
        text_dist.text = "${round(totaldist/10f)/100f} km"
        fragment?.updateMap(location)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
    }

}