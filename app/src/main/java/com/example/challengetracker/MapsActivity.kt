package com.example.challengetracker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.DataSetObserver
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.PersistableBundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_maps.*
import java.lang.Math.round
import java.text.DateFormat
import java.util.*


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
    lateinit var spinnerAdapter : SpinnerAdapter
    private val locationReceiver : BroadcastReceiver= LocationReceiver().apply {
        setMapsActivityHandler(this@MapsActivity)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.i(TAG, "set ${spinner_activity.selectedItemPosition}")
        outState.putInt("activitySpinner", spinner_activity.selectedItemPosition)


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "Create")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        DataBaseHelper.setAppContext(this.applicationContext)
        setupSettings()
        setupFragment()
        var activity = mutableListOf<ChallengeActivity>(ChallengeActivity("Select an activity", 0f, ""))

        //todo get challenge -> set in mainactivity check for finished
        var challengeId = DataBaseHelper.getCurrentChallengeId()
        Log.i(TAG, "challengeId: $challengeId")
        if(challengeId == ""){
            Log.i(TAG, "invalid challenge id")
            Toast.makeText(applicationContext, "Please choose a challenge", Toast.LENGTH_SHORT).show()
            this.finish()
        }else {
            DataBaseHelper.getChallengeById(challengeId) { challenge ->
                if (Calendar.getInstance().after(challenge.deadline)) {
                    Toast.makeText(applicationContext, "The chosen challenge is expired", Toast.LENGTH_SHORT).show()
                    this.finish()
                } else {
                    challenge.activities.forEach() {
                        Log.i(TAG, "new activity added")
                        activity.add(it)
                    }
                }
                spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activity)
                spinner_activity.adapter = spinnerAdapter
                Log.i(TAG, "pos spinner from savedInstance ${spinner_pos}")
                if (spinnerAdapter.count > spinner_pos) {
                    spinner_activity.setSelection(spinner_pos)
                }
            }
        }
        setUiElements()
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

            spinner_activity.isEnabled = false
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
                //todo get name
                var name = DataBaseHelper.getNickname()
                DataBaseHelper.addNewUserActivity(name, points, DataBaseHelper.getCurrentChallengeId(), activity.name, DataBaseHelper.getCurrentChallengeName()){
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


    fun updateMap(location: Location){
        Log.i(TAG, "update $totaldist")
        text_dist.text = "${round(totaldist / 10f)/100f} km"
        fragment.updateMap(location)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
        spinner_pos = spinner_activity.selectedItemPosition
    }

}