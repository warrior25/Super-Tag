package com.huikka.supertag

import android.location.Location
import android.location.LocationListener
import com.huikka.supertag.data.ZoneManager
import com.huikka.supertag.data.dao.PlayerDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerLocationListener(application: STApplication, private val playerId: String) :
    LocationListener {

    private val playerDao = PlayerDao(application)
    private val zoneManager = ZoneManager(application)

    override fun onLocationChanged(loc: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            val zoneId = zoneManager.getZoneFromLocation(loc)?.id
            playerDao.updatePlayerLocation(
                playerId, loc.latitude, loc.longitude, loc.accuracy, loc.speed, loc.bearing, zoneId
            )
        }
    }
}