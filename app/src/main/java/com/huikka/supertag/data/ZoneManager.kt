package com.huikka.supertag.data

import android.util.Log
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.ZoneDao
import com.huikka.supertag.data.dto.Player
import com.huikka.supertag.data.dto.Zone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ZoneManager(application: STApplication) {

    private val zoneDao = ZoneDao(application)


    private val earthRadius = 6378137.0 // Earth radius in meters

    // Function to calculate the distance between two latitude-longitude points in meters
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(
                dLon / 2
            ) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    // Function to check if the player's location overlaps with the zone
    private fun isPlayerInZone(
        zone: Zone, player: Player
    ): Boolean {
        val distance =
            haversine(zone.latitude, zone.longitude, player.latitude!!, player.longitude!!)
        Log.d("HARVESINE", distance.toString())
        return distance <= zone.radius + player.locationAccuracy!!
    }

    suspend fun getPlayerZone(player: Player): Zone? {
        Log.d("PLAYER", player.toString())
        val zones = zoneDao.getZones()
        for (zone in zones) {
            if (isPlayerInZone(zone, player)) {
                return zone
            }
        }
        return null
    }
}