package com.huikka.supertag

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.RunnerDao
import com.huikka.supertag.data.helpers.TimeConverter
import com.huikka.supertag.fragments.ChaserTimers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class GameActivity : AppCompatActivity() {
    private lateinit var map: MapView
    private lateinit var playerId: String
    private lateinit var gameId: String
    private var isRunner = false
    private lateinit var runnerId: String
    private var chasers: MutableMap<String, DirectedLocationOverlay> = mutableMapOf()
    private lateinit var runner: DirectedLocationOverlay

    private var minute = 60000L

    private lateinit var authDao: AuthDao
    private lateinit var gameDao: GameDao
    private lateinit var playerDao: PlayerDao
    private lateinit var runnerDao: RunnerDao

    private lateinit var timers: ChaserTimers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        // HTTP User-Agent variable to not ban other users
        getInstance().userAgentValue = applicationContext.packageName
        setContentView(R.layout.activity_game)

        val app = application as STApplication
        authDao = AuthDao(app)
        gameDao = GameDao(app)
        playerDao = PlayerDao(app)
        runnerDao = RunnerDao(app)

        val cardsButton = findViewById<ImageButton>(R.id.cardsButton)
        cardsButton.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = LayoutInflater.from(this).inflate(R.layout.cards_sheet_layout, null)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            val buttonOne = view.findViewById<Button>(R.id.firstButton)
            buttonOne.setOnClickListener {
                Toast.makeText(this, "First button clicked", Toast.LENGTH_SHORT).show()
            }
        }

        timers = supportFragmentManager.findFragmentById(R.id.timers) as ChaserTimers

        // map initialization
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.maxZoomLevel = 18.0
        map.minZoomLevel = 12.0

        map.setMultiTouchControls(true)
        map.setTilesScaledToDpi(true)

        val mapController = map.controller
        val startPoint = GeoPoint(61.4498, 23.8595)
        mapController.setCenter(startPoint)
        mapController.setZoom(15.0)

        // Mandatory copy-right mention
        val cr = CopyrightOverlay(this)
        map.overlays.add(cr)

        lifecycleScope.launch(Dispatchers.IO) {
            playerId = authDao.getUser()!!.id
            gameId = gameDao.getCurrentGameInfo(playerId).gameId!!
            runnerId = gameDao.getRunnerId(gameId)!!
            isRunner = playerId == runnerId

            val intent = Intent(applicationContext, LocationUpdateService::class.java)
            startForegroundService(intent)

            if (isRunner) {
                // TODO: Show runner UI
                Log.d("RUNNER", "is runner")
                val nextUpdate = runnerDao.getNextUpdateTime(gameId)
                if (nextUpdate == null) {
                    applyHeadStart()
                }
                scheduleRunnerLocationUpdates()
            } else {
                Log.d("CHASER", "is chaser")
                lifecycleScope.launch(Dispatchers.IO) {
                    updateChasersOnMap()
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    updateRunnerOnMap()
                }
            }
        }

        drawUserOnMap()
        drawATMsV1()
        drawATMsV2()
    }

    override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause()  //needed for compass, my location overlays, v6.0.0 and up
    }

    private fun drawUserOnMap() {
        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), this.map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true
        map.overlays.add(myLocationOverlay)
    }

    private suspend fun updateChasersOnMap() {
        val flow = playerDao.getPlayersByGameIdFlow(gameId)
        flow.collect {
            for (player in it) {
                if (player.id == runnerId || player.id == playerId) {
                    continue
                }
                if (chasers[player.id] == null) {
                    chasers[player.id!!] = drawPlayerOnMap(
                        player.latitude!!, player.longitude!!, Color.RED, R.drawable.agent
                    )
                }
                chasers[player.id]?.location = GeoPoint(player.latitude!!, player.longitude!!)
            }
            map.invalidate()
        }
    }

    private suspend fun updateRunnerOnMap() {
        val flow = runnerDao.getRunnerFlow(gameId)
        flow.collect {
            val updateDelay = calculateDelay(it.nextUpdate!!)
            startTimer(timers.runnerLocationTimer, updateDelay)

            if (it.latitude == null || it.longitude == null) {
                // Runner location is not set on initial collect
                return@collect
            }
            if (!::runner.isInitialized) {
                runner = drawPlayerOnMap(
                    it.latitude, it.longitude, Color.GREEN, R.drawable.agent
                )
            } else {
                runner.location = GeoPoint(it.latitude, it.longitude)
                map.invalidate()
            }
        }
    }

    private fun drawPlayerOnMap(
        latitude: Double, longitude: Double, color: Int, icon: Int
    ): DirectedLocationOverlay {
        val conf = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(100, 100, conf)
        val canvas1 = Canvas(bmp)

        // paint defines the text color, stroke width and size
        val paint = Paint()
        paint.textSize = 35f
        paint.color = color

        // draw background color for bitmap
        canvas1.drawCircle(50f, 50f, 50f, paint)

        // modify canvas
        canvas1.drawBitmap(
            BitmapFactory.decodeResource(
                resources, icon
            ), 15f, 15f, paint
        )

        // Add player to map
        val playerLocation = GeoPoint(latitude, longitude)
        val playerOverlay = DirectedLocationOverlay(this)
        playerOverlay.location = playerLocation
        playerOverlay.setDirectionArrow(bmp)

        map.overlays.add(playerOverlay)
        return playerOverlay
    }

    private suspend fun applyHeadStart() {
        val headStart = System.currentTimeMillis() + gameDao.getHeadStart(gameId)!! * minute
        val timestamp = TimeConverter.longToTimestamp(headStart)
        runnerDao.setNextUpdateTime(gameId, timestamp)
    }

    private suspend fun scheduleRunnerLocationUpdates() {
        val nextUpdate = runnerDao.getNextUpdateTime(gameId)!!
        val updateDelay = calculateDelay(nextUpdate)
        delay(updateDelay)

        val timestamp: String
        if (runnerDao.getLastUpdateTime(gameId) == null) {
            // First update after head start
            val initialDelay = gameDao.getInitialTrackingInterval(gameId)!! * minute
            // Counter decrementing first interval by adding 1 minute
            timestamp = calculateNextUpdateTimestamp(initialDelay + minute)
        } else {
            timestamp = calculateNextUpdateTimestamp(updateDelay)
        }
        updateRunnerLocation(timestamp)
        scheduleRunnerLocationUpdates()
    }

    private fun calculateDelay(nextUpdate: String): Long {
        val time = TimeConverter.timestampToLong(nextUpdate)
        return time.minus(System.currentTimeMillis()).coerceAtLeast(0)
    }

    private fun calculateNextUpdateTimestamp(currentDelay: Long): String {
        val nextDelay =
            System.currentTimeMillis() + currentDelay.minus(minute).coerceAtLeast(minute)
        return TimeConverter.longToTimestamp(nextDelay)
    }

    private suspend fun updateRunnerLocation(nextUpdate: String) {
        val loc = playerDao.getPlayerLocation(runnerId)
        val lastUpdate = TimeConverter.longToTimestamp(System.currentTimeMillis())
        runnerDao.setLocation(gameId, loc.latitude!!, loc.longitude!!, lastUpdate, nextUpdate)
    }

    private fun drawATMsV1() {
        val ATMLocation = GeoPoint(61.4498, 23.8595)
        val radius = 50.0
        val circle = Polygon()
        circle.points = Polygon.pointsAsCircle(ATMLocation, radius)

        map.overlays.add(circle)

    }

    private fun drawATMsV2() {
        val ATMLocation = GeoPoint(61.4488, 23.8590)
        val ATMOverlay = DirectedLocationOverlay(this)
        ATMOverlay.location = ATMLocation
        ATMOverlay.setAccuracy(50)

        map.overlays.add(ATMOverlay)

        //TODO("Get ATMs from database, Set icon for zone")

    }

    private fun startTimer(timer: CustomTimer, time: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            timer.setTime(time)
            timer.startTimer()
        }
    }

}