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
import android.os.CountDownTimer
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
import com.huikka.supertag.data.helpers.ServiceAction
import com.huikka.supertag.data.helpers.ServiceStatus
import com.huikka.supertag.data.helpers.Side
import com.huikka.supertag.data.helpers.ZoneType
import com.huikka.supertag.data.helpers.minute
import com.instacart.truetime.time.TrueTime
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
    private lateinit var trueTime: TrueTime
    private lateinit var zoneManager: ZoneManager
    private lateinit var locationManager: LocationManager

    private lateinit var userId: String
    private lateinit var gameId: String
    private var isRunner: Boolean = false
    private var initialTrackingInterval: Long = 0
    private var headStart: Long = 0

    private lateinit var myLocation: Location

    private var runnerZones: MutableList<Zone> = mutableListOf()
    private var chaserZones: MutableList<Zone> = mutableListOf()
    private var activeZoneIds: List<Int> = listOf()
    private var lastZone: Zone? = null

    private var isTimerRunning = false
    private var isTimerCancelled = false
    private var zonePresenceTimer: CountDownTimer? = null

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
        trueTime = app.trueTime
        zoneManager = ZoneManager(app)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ServiceAction.START_SERVICE -> {
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
                        activateRunnerLocationUpdates()
                    }
                }
                ServiceStatus.setServiceRunning(applicationContext, true)
            }

            ServiceAction.STOP_SERVICE -> {
                ServiceStatus.setServiceRunning(applicationContext, false)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelfResult(startId)
            }
        }
        return START_STICKY
    }


    override fun onLocationChanged(loc: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            myLocation = loc
            try {
                var enteredZone: Long? = null
                val zone = zoneManager.getZoneFromLocation(loc)
                lastZone = zone
                val timerDuration = when (lastZone?.type) {
                    ZoneType.ATM -> Config.ATM_TIME

                    ZoneType.STORE -> Config.STORE_TIME

                    ZoneType.ATTRACTION -> Config.ATTRACTION_TIME

                    else -> 0
                }
                if (!isValidZone()) {
                    isTimerRunning = false
                    zonePresenceTimer?.cancel()
                } else if (!isTimerRunning) {
                    startZonePresenceTimer(timerDuration)
                    enteredZone = trueTime.now().time
                }
                playerDao.updatePlayerLocation(
                    userId,
                    loc.latitude,
                    loc.longitude,
                    loc.accuracy,
                    loc.speed,
                    loc.bearing,
                    zone?.id,
                    enteredZone
                )
            } catch (e: Exception) {
                Log.e("LOCATION", "Failed to update location: $e")
            }
        }
    }

    private suspend fun initData() {
        authDao.awaitCurrentSession()
        userId = authDao.getUser()!!.id
        gameId = gameDao.getCurrentGameInfo(userId).gameId
        isRunner = gameDao.getRunnerId(gameId) == userId
        initialTrackingInterval = gameDao.getInitialTrackingInterval(gameId).toLong()
        headStart = gameDao.getHeadStart(gameId).toLong()
        getZones()
    }

    private suspend fun getZones() {
        val zones = zoneDao.getZones()
        for (zone in zones) {
            if (zone.type == ZoneType.ATM || zone.type == ZoneType.STORE) {
                chaserZones.add(zone)
            } else if (zone.type == ZoneType.ATTRACTION) {
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

            activeZoneIds = listOf(zone1.id, zone2.id)

            val nextUpdateTime = trueTime.now().time + Config.RUNNER_ZONE_SHUFFLE_TIME

            activeRunnerZonesDao.setActiveRunnerZones(
                gameId = gameId, zones = listOf(zone1.id, zone2.id), nextUpdate = nextUpdateTime
            )

            delay(Config.RUNNER_ZONE_SHUFFLE_TIME)
            shuffleRunnerZones()
        }
    }

    private fun updateRunnerLocation(delayBeforeUpdate: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(delayBeforeUpdate)
            val now = trueTime.now().time
            val nextDelay = delayBeforeUpdate.minus(
                minute
            ).coerceAtLeast(minute)
            val nextUpdate = trueTime.now().time + nextDelay

            runnerDao.setLocation(
                gameId = gameId,
                latitude = myLocation.latitude,
                longitude = myLocation.longitude,
                accuracy = myLocation.accuracy,
                lastUpdate = now,
                nextUpdate = nextUpdate
            )
            updateRunnerLocation(nextDelay)
        }
    }

    private fun activateRunnerLocationUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            var nextUpdate = trueTime.now().time + headStart * minute
            runnerDao.setNextUpdateTime(gameId, nextUpdate)

            delay(headStart * minute)

            val now = trueTime.now().time
            nextUpdate = trueTime.now().time + initialTrackingInterval * minute

            // TODO: Can crash here if runner has not moved
            runnerDao.setLocation(
                gameId = gameId,
                latitude = myLocation.latitude,
                longitude = myLocation.longitude,
                accuracy = myLocation.accuracy,
                lastUpdate = now,
                nextUpdate = nextUpdate
            )
            updateRunnerLocation(initialTrackingInterval * minute)
        }
    }

    private fun addMoney() {
        CoroutineScope(Dispatchers.IO).launch {
            val side = if (isRunner) {
                Side.Runner
            } else {
                Side.Chasers
            }

            val amount = when (lastZone?.type) {
                ZoneType.ATM -> Config.ATM_MONEY
                ZoneType.STORE -> Config.STORE_MONEY
                ZoneType.ATTRACTION -> Config.ATTRACTION_MONEY
                else -> 0
            }
            try {
                gameDao.addMoney(gameId, side, amount)
            } catch (e: Exception) {
                Log.e("Service", "Failed to add money: $e")
            }
        }
    }

    private fun isValidZone(): Boolean {
        return (!isRunner && lastZone?.type in ZoneType.CHASER_ZONE_TYPES) || (isRunner && lastZone?.id in activeZoneIds)
    }

    private fun startZonePresenceTimer(duration: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            isTimerRunning = true
            isTimerCancelled = true
            zonePresenceTimer?.cancel()
            isTimerCancelled = false
            zonePresenceTimer = object : CountDownTimer(duration, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    addMoney()
                    isTimerRunning = false
                }
            }.start()
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