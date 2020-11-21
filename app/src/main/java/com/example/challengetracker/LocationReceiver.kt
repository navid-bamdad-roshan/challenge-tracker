package com.example.challengetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log

class LocationReceiver : BroadcastReceiver() {
    companion object{
        val LOCATION_ACTION = "com.example.challengetracker.receivers.LocationReceiver.LOCATION_ACTION"
        val TAG = "LocationReceiver"
    }
    lateinit var activity:MapsActivity

    fun setMapsActivityHandler(mapsActivity: MapsActivity) {
        activity = mapsActivity
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
       Log.i(TAG, "received")
        if(LOCATION_ACTION == intent.action) {
            Log.i(TAG, "right intent")
           intent.getParcelableExtra<Location>("newLoc")?.let {
               activity.updateMap(it)
           }
       }
    }
}