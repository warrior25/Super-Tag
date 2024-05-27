package com.huikka.supertag

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.huikka.supertag.data.ZoneManager
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.PlayerDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationUpdateService : Service(), LocationListener {

    private lateinit var playerDao: PlayerDao
    private lateinit var zoneManager: ZoneManager
    private lateinit var locationManager: LocationManager
    private lateinit var playerId: String

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val app = application as STApplication
        playerDao = PlayerDao(app)
        zoneManager = ZoneManager(app)
        val authDao = AuthDao(app)
        playerId = authDao.getUser()!!.id

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10f, this
            )
        }
        return START_STICKY
    }

    override fun onLocationChanged(loc: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            val zoneId = zoneManager.getZoneFromLocation(loc)?.id
            val error = playerDao.updatePlayerLocation(
                playerId, loc.latitude, loc.longitude, loc.accuracy, loc.speed, loc.bearing, zoneId
            )
            if (error != null) {
                Log.e("LOCATION", "Failed to update location: ${error.message}")
            }
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "LOCATION_UPDATE_CHANNEL"

        val channel = NotificationChannel(
            notificationChannelId, "Location Updates", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for location updates"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Location Service")
            .setContentText("Updating location in the background").setSmallIcon(R.drawable.runner)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
    }
}