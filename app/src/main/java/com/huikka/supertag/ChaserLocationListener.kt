package com.huikka.supertag

import android.location.Location
import android.location.LocationListener
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChaserLocationListener : LocationListener {

    private var database: DatabaseReference = Firebase.database.reference


    override fun onLocationChanged(loc: Location) {
        Log.d("Chaser", "Latitude: " + loc.latitude + ", Longitude: " + loc.longitude)
        database.child("Chasers").child("Chaser1").child("Location").child("Latitude").setValue(loc.latitude)
        database.child("Chasers").child("Chaser1").child("Location").child("Longitude").setValue(loc.longitude)
    }
}