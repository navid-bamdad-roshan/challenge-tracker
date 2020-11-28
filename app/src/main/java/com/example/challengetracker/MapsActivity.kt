package com.example.challengetracker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.PersistableBundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Chronometer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_maps.*
import java.lang.Error
import java.lang.Math.round


class MapsActivity : AppCompatActivity(){
    companion object{
        var totaldist = 0f
        var activityActive = false
        val TAG = "MapsActivity"
        var meter :Chronometer? = null
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val REQUEST_LOCATION = 7
        val REQUEST_LOCATION_SERVICE = 9
        var askingPermission = false
        var spinner_pos = 0
    }
    lateinit var fragment : MapsFragment
    private val locationReceiver : BroadcastReceiver= LocationReceiver().apply {
        setMapsActivityHandler(this@MapsActivity)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.i(TAG, "set ${spinner_activity.selectedItemPosition}")
        outState.putInt("activitySpinner", spinner_activity.selectedItemPosition)


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        DataBaseHelper.setAppContext(this.applicationContext)
        setupSettings()
        setupFragment()
        setUiElements()
        var activity = mutableListOf<ChallengeActivity>(ChallengeActivity("Select an activity", 0f, ""))
        DataBaseHelper.getChallengeById("eCYl9TuShYqjQEfvfLiR"){ challenge ->
            challenge.activities.forEach(){
                activity.add(it)
            }
          }
            spinner_activity.adapter=ArrayAdapter(this, android.R.layout.simple_spinner_item, activity)
//        savedInstanceState?.let {
//            val pos = savedInstanceState.getInt("activitySpinner", 0)
//            Log.i(TAG, "spinner from saved instance ${pos}")
            spinner_activity.setSelection(spinner_pos)
//        }

        //register receiver for Broadcasts from GPS service
        val filter = IntentFilter(LocationReceiver.LOCATION_ACTION)
        registerReceiver(locationReceiver, filter)
    }
    private fun setupSettings() {
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment())
        val darkMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_mode", false)
        if (!darkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    private fun setupFragment() {
        fragment = MapsFragment()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.mapsFragment, fragment).commit()
        fragmentManager.executePendingTransactions()
        if (locationGranted()) {
            Log.i(TAG, "enable fragment location")
            fragment.enableMyLocation()
        }else{
            if(askingPermission.not()) {
                askingPermission = true
                ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION)
            }
        }
    }

    private fun locationGranted():Boolean{
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
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
                if(locationGranted()){
                    Log.i(TAG, "location Granted")
                    startNewActivity()
                } else{
                    if(askingPermission.not()) {
                        askingPermission = true
                        ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_SERVICE)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        askingPermission = false
        if(requestCode == REQUEST_LOCATION){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                fragment.enableMyLocation()
            }
        }
        if(requestCode == REQUEST_LOCATION_SERVICE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                fragment.enableMyLocation()
                Log.i(TAG, "newActivity after granted")
                startNewActivity()
            }
        }
    }
    private fun startNewActivity() {
        if (spinner_activity.selectedItemPosition != 0) {
            if (checkGpsActivated()) {
                Log.i(TAG, "startService, activity pos ok")
                //reset
                resetOldActivity()
                spinner_activity.isEnabled = false
                meter?.start()
                text_dist.text = "${round(totaldist / 10f) / 100f} km"
                btn_startStop.text = getString(R.string.stop)
                activityActive = true
                startService(GpsService.getIntent(this))
            } else {
                Toast.makeText(applicationContext, "Please activate GPS", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.i(TAG, "no activity selected")
            Toast.makeText(applicationContext, "Please select an activity", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetOldActivity() {
        totaldist = 0f
        meter?.base = SystemClock.elapsedRealtime();
        MapsFragment.points.clear()
        fragment.clearMap()
    }


    fun updateMap(location:Location){
        Log.i(TAG, "update $totaldist")
        text_dist.text = "${round(totaldist/10f)/100f} km"
        fragment.updateMap(location)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
        spinner_pos = spinner_activity.selectedItemPosition
    }

}