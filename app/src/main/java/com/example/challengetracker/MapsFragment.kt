package com.example.challengetracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapsFragment : Fragment() {
    companion object{
        var zoomLoc = LatLng(58.385254, 26.725064)
        val zoomFactor = 16f
        var points = mutableListOf<LatLng>()
    }
    var lastLocation: Location?=null
    val TAG = "MapsFragment"
    var mMap:GoogleMap? = null

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomLoc, zoomFactor))
        Log.i(TAG, "draw again")
        mMap?.addPolyline(PolylineOptions().addAll(points))
        enableMyLocation()
    }
    fun enableMyLocation(){
        if (context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "enable location")
            mMap?.isMyLocationEnabled = true
            //annoying if one wants to look something up in the map
//            mMap?.setOnMyLocationChangeListener {
//                if(MapsActivity.activityActive.not()){
//                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), zoomFactor))
//                }
//            }
//        }else{
//            if(MapsActivity.askingPermission.not()) {
//                activity?.let { ActivityCompat.requestPermissions(it, MapsActivity.permissions, MapsActivity.REQUEST_LOCATION) }
//            }
        }
    }
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }
    fun updateMap(location: Location){
        zoomLoc = LatLng(location.latitude, location.longitude)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomLoc, zoomFactor))
        if(lastLocation != null) {
            Log.i(TAG, "drawLine")
            mMap?.addPolyline(
                PolylineOptions().add(
                    LatLng(
                        lastLocation!!.latitude,
                        lastLocation!!.longitude
                    ), LatLng(location.latitude, location.longitude)
                )
            )
        }
        lastLocation = location
    }

    fun clearMap(){
        mMap?.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}