package com.huikka.supertag

import android.location.Location
import android.location.LocationListener
import android.util.Log

class ChaserLocationListener : LocationListener {


    override fun onLocationChanged(loc: Location) {
        Log.d("Chaser", "Latitude: " + loc.latitude + ", Longitude: " + loc.longitude)
    }
}