package com.example.challengetracker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_maps.*
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class MapsActivity : AppCompatActivity(){
    companion object{
        var totaldist = 0f
        var activityActive = false
        const val TAG = "MapsActivity"
        var meter :Chronometer? = null
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        const val REQUEST_LOCATION = 7
        const val REQUEST_LOCATION_SERVICE = 9
        const val START_MAPS = 13
        var askingPermission = false
        var spinner_pos = 0
    }
    lateinit var fragment : MapsFragment
    lateinit var viewModel : MapsActivityViewModel


    private val locationReceiver : BroadcastReceiver= LocationReceiver().apply {
        setMapsActivityHandler(this@MapsActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "Create")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        DataBaseHelper.setAppContext(this.applicationContext)
        viewModel = ViewModelProvider(this).get(MapsActivityViewModel::class.java)
        setupFragment()
        setSpinner()
        setUiElements()
        //register receiver for Broadcasts from GPS service
        val filter = IntentFilter(LocationReceiver.LOCATION_ACTION)
        registerReceiver(locationReceiver, filter)
    }

    private fun setSpinner() {
        spinner_activity.adapter = viewModel.adapter
        if (viewModel.adapter.count > 1){
            onSpinnerReady()
        }else {
            viewModel.setSpinnerDefaultValue.observe(this, androidx.lifecycle.Observer {
                onSpinnerReady()
            })
        }
    }

    private fun onSpinnerReady() {
        val dateString  = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time)
        spinner_activity.setSelection(spinner_pos)
        text_challenge.text = viewModel.currentChallengeName
        if (dateString > viewModel.currenChallengeDate) {
            finishActivityDialog("The chosen challenge is expired")
        }
    }

//    private fun setChallengeInfo() {
//        adapter = ArrayAdapter(this,
//                android.R.layout.simple_spinner_item,
//                arrayListOf<ChallengeActivity>(ChallengeActivity(
//                        "Select an activity", 0f, "")))
//        val dateString  = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time)
//        var challengeId = DataBaseHelper.getCurrentChallengeId()
//        Log.i(TAG, "challengeId: $challengeId")
//        DataBaseHelper.getChallengeById(challengeId) { challenge ->
//            if (dateString > challenge.deadline) {
//                Toast.makeText(applicationContext, "The chosen challenge is expired", Toast.LENGTH_SHORT).show()
//            } else {
//                challenge.activities.forEach() {
//                    Log.i(TAG, "new activity added")
//                    adapter.add(it)
//                }
//            }
//            spinner_activity.adapter = adapter
//            Log.i(TAG, "pos spinner from savedInstance ${spinner_pos}")
//            if (adapter.count > spinner_pos) {
//                spinner_activity.setSelection(spinner_pos)
//            }
//            text_challenge.text = challenge.name
//        }
//    }

//    private fun setupSettings() {
//        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment())
//        val darkMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_mode", false)
//        if (!darkMode)
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        else
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//    }
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
        spinner_activity.isEnabled = activityActive.not()
        if (activityActive) {
            c_meter.base = meter!!.base
            c_meter.start()
            btn_startStop.text = getString(R.string.stop)
        }
        meter = c_meter
        text_dist.text = getString(R.string.kilometers, totaldist/1000f)
        btn_startStop.setOnClickListener {
            if (activityActive) {
                meter?.let { meter ->
                    Log.i(TAG, "elapsed time ${SystemClock.elapsedRealtime() - meter.base}")
                    meter.stop()
                }
                GpsService.stopTracking(this)
                submitActivity()
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

    private fun submitActivity() {
    //calculate points
            val activity = spinner_activity.selectedItem as ChallengeActivity
            var points = totaldist/1000f * activity.pointPerKm
            Log.i(TAG, "points of activity: ${activity.pointPerKm}")
        if(points>0) {
            var name = DataBaseHelper.getNickname()
            Log.i(TAG, "name: $name, points $points")
            DataBaseHelper.addNewUserActivity(name, points, DataBaseHelper.getCurrentChallengeId(), activity.id, activity.name) {
                Toast.makeText(applicationContext, getString(R.string.activitySubmitted, points), Toast.LENGTH_SHORT).show()
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
        if (spinner_activity.selectedItemPosition == 0) {
            Log.i(TAG, "no activity selected")
            Toast.makeText(applicationContext, getString(R.string.selectActivity), Toast.LENGTH_SHORT).show()
            return
        }
        if (checkGpsActivated().not()) {
            Toast.makeText(applicationContext, getString(R.string.activateGPS), Toast.LENGTH_SHORT).show()
            return
        }

        Log.i(TAG, "startService, activity pos and gps ok")
        //reset
        resetOldActivity()
        spinner_activity.isEnabled = false
        totaldist = 1111f
        meter?.start()
        text_dist.text = getString(R.string.kilometers, totaldist/1000f)
        btn_startStop.text = getString(R.string.stop)
        activityActive = true
        startService(GpsService.getIntent(this))


    }

    private fun resetOldActivity() {
        totaldist = 0f
        meter?.base = SystemClock.elapsedRealtime();
        MapsFragment.points.clear()
        fragment.clearMap()
    }


    fun updateMap(location: Location){
        Log.i(TAG, "update $totaldist")
        text_dist.text = getString(R.string.kilometers, totaldist/1000f)
        fragment.updateMap(location)
    }

    private fun finishActivityDialog(str:String) {
        // new game or back to menu when game is over
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tracking not possible")
        builder.setMessage(str)
        builder.setPositiveButton(
                "Back") { _, _ ->
            finish()
        }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
        spinner_pos = spinner_activity.selectedItemPosition
        if(activityActive.not()) {
            resetOldActivity()
        }

    }
}
