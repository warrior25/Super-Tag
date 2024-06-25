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
import com.huikka.supertag.data.dao.ActiveRunnerZonesDao
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.RunnerDao
import com.huikka.supertag.data.dao.ZoneDao
import com.huikka.supertag.data.dto.Zone
import com.huikka.supertag.data.helpers.Config
import com.huikka.supertag.data.helpers.ServiceStatus
import com.huikka.supertag.data.helpers.TimeConverter
import com.huikka.supertag.data.helpers.ZoneTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationUpdateService : Service(), LocationListener {

    private lateinit var playerDao: PlayerDao
    private lateinit var runnerDao: RunnerDao
    private lateinit var gameDao: GameDao
    private lateinit var zoneDao: ZoneDao
    private lateinit var authDao: AuthDao
    private lateinit var activeRunnerZonesDao: ActiveRunnerZonesDao
    private lateinit var zoneManager: ZoneManager
    private lateinit var locationManager: LocationManager
    private lateinit var userId: String
    private lateinit var gameId: String
    private var isRunner: Boolean = false

    private var runnerZones: MutableList<Zone> = mutableListOf()
    private var chaserZones: MutableList<Zone> = mutableListOf()
    private var activeZoneIds: List<Int> = listOf()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val app = application as STApplication
        playerDao = app.playerDao
        gameDao = app.gameDao
        runnerDao = app.runnerDao
        zoneDao = app.zoneDao
        authDao = app.authDao
        activeRunnerZonesDao = app.activeRunnerZonesDao
        zoneManager = ZoneManager(app)

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

        CoroutineScope(Dispatchers.IO).launch {
            initData()

            // Recursive actions that are repeated with a timer
            if (isRunner) {
                shuffleRunnerZones()
            }
        }

        return START_STICKY
    }

    override fun onLocationChanged(loc: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            val zoneId = zoneManager.getZoneFromLocation(loc)?.id
            val error = playerDao.updatePlayerLocation(
                userId, loc.latitude, loc.longitude, loc.accuracy, loc.speed, loc.bearing, zoneId
            )
            if (error != null) {
                Log.e("LOCATION", "Failed to update location: ${error.message}")
            }
        }
    }

    private suspend fun initData() {
        authDao.awaitCurrentSession()
        userId = authDao.getUser()!!.id
        gameId = gameDao.getCurrentGameInfo(userId).gameId!!
        isRunner = gameDao.getRunnerId(gameId) == userId
        getZones()
    }

    private suspend fun getZones() {
        val zones = zoneDao.getZones()
        for (zone in zones) {
            if (zone.type == ZoneTypes.ATM || zone.type == ZoneTypes.STORE) {
                chaserZones.add(zone)
            } else if (zone.type == ZoneTypes.ATTRACTION) {
                runnerZones.add(zone)
            }
        }
    }

    private fun shuffleRunnerZones() {
        CoroutineScope(Dispatchers.IO).launch {
            var zone1: Zone
            var zone2: Zone
            do {
                zone1 = runnerZones.random()
            } while (zone1.id in activeZoneIds)
            do {
                zone2 = runnerZones.random()
            } while (zone1.id == zone2.id || zone2.id in activeZoneIds)

            activeZoneIds = listOf(zone1.id!!, zone2.id!!)

            val nextUpdateDelay = System.currentTimeMillis() + Config.RUNNER_ZONE_SHUFFLE_TIME
            val nextUpdateTimestamp = TimeConverter.longToTimestamp(nextUpdateDelay)

            activeRunnerZonesDao.setActiveRunnerZones(
                gameId = gameId,
                zones = listOf(zone1.id!!, zone2.id!!),
                nextUpdate = nextUpdateTimestamp
            )

            delay(Config.RUNNER_ZONE_SHUFFLE_TIME)
            shuffleRunnerZones()
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
        ServiceStatus.setServiceRunning(applicationContext, false)
        locationManager.removeUpdates(this)
    }
}