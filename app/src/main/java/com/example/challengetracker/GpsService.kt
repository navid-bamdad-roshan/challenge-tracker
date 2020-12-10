package com.example.challengetracker

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

class GpsService : Service() {

    private var startId = 0
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.extras?.getString(ACTION_NAME)){
                ACTION_STOP_TRACKING -> stopTrackingService()
            }
        }
    }

    companion object {
        private const val GPS_ACTION = "GPS_ACTION"
        private const val ACTION_NAME = "ACTION_NAME"
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        val TAG = "GpsService"
        fun getIntent(context: Context) = Intent(context, GpsService::class.java)
        fun stopTracking(context: Context) =
            context.sendBroadcast(Intent(GPS_ACTION).apply { putExtra(ACTION_NAME, ACTION_STOP_TRACKING) })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("GpsService", "started Service")
        this.startId = startId
        startForeground(startId, getNotification())
        startLocationTracking()
        registerReceiver(actionReceiver, IntentFilter(GPS_ACTION))
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        kotlin.runCatching { unregisterReceiver(actionReceiver) }
        super.onDestroy()
    }

    private fun stopTrackingService(){
      //  stopForeground(true)
        stopLocationTracking()
        stopSelf(startId)
    }

    private fun startLocationTracking(){
        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 500
            maxWaitTime = 1000
            smallestDisplacement = 1.0f
        }

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result?.let {
                    Log.i(TAG, "got non null result")
                    MapsFragment.points.add(LatLng(it.lastLocation.latitude, it.lastLocation.longitude))
                    if(lastLocation == null){
                        lastLocation = it.lastLocation
                        sendBroadcast(Intent(LocationReceiver.LOCATION_ACTION).apply {
                            putExtra("newLoc", lastLocation)
                        })
                        return@let
                    }
                    it.lastLocation?.let { its_last ->
                        val distanceInMeters = its_last.distanceTo(lastLocation)
                        Log.i(TAG, "check accuracy: acc ${its_last.accuracy}, change ${distanceInMeters}")
                        if(valid(its_last, lastLocation!!)) {
                            MapsActivity.totaldist += distanceInMeters.toLong()
                            Log.i(TAG, "Completed: ${MapsActivity.totaldist} meters, (added $distanceInMeters)")
                            sendBroadcast(Intent(LocationReceiver.LOCATION_ACTION).apply {
                                putExtra("newLoc", its_last)
                            })
                            lastLocation = it.lastLocation
                        }
                    }
                }
                super.onLocationResult(result)
            }
        }
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopTrackingService()
        }else {
                fusedClient.requestLocationUpdates(locationRequest, locationCallback, null)

        }
    }

    private fun valid(new: Location, old: Location): Boolean {
        // not valid if new position accuracy > 10m
        if(new.accuracy<2){
            return true
        }
        if(new.accuracy>10){
            return false
        }
        if(new.distanceTo(old)<old.accuracy && new.accuracy > old.accuracy){
            return false
        }
        return true

    }

    private fun stopLocationTracking(){
        Log.i(TAG, "tracking stopped")
        fusedClient.removeLocationUpdates(locationCallback)
    }

    private fun getNotification(): Notification? {

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("gps_tracker", "GPS Tracker")
        } else {
            // If earlier version channel ID is not used
            ""
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startMaps", MapsActivity.START_MAPS)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val b = NotificationCompat.Builder(this, channelId)

        b.setOngoing(true)
            .setContentTitle("Currently tracking GPS location...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
//            .setAutoCancel(true) //remove notification when tapped
        return b.build()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}