package com.huikka.supertag

import android.location.Location
import android.location.LocationListener
import android.util.Log
import com.huikka.supertag.data.dao.PlayerDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerLocationListener(application: STApplication, private val playerId: String) :
    LocationListener {

    private val playerDao = PlayerDao(application)

    override fun onLocationChanged(loc: Location) {
        Log.d("updateLocation", loc.toString())
        CoroutineScope(Dispatchers.IO).launch {
            playerDao.updatePlayerLocation(
                playerId, loc.latitude, loc.longitude, loc.accuracy, loc.speed, loc.bearing
            )
        }
    }
}