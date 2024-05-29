package com.huikka.supertag

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.RunnerDao
import com.huikka.supertag.data.dao.ZoneDao
import com.huikka.supertag.data.dto.Zone
import com.huikka.supertag.data.helpers.TimeConverter
import com.huikka.supertag.data.helpers.ZoneTypes
import com.huikka.supertag.fragments.ChaserTimers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem
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
    private lateinit var zoneDao: ZoneDao

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
        zoneDao = ZoneDao(app)

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

            initCommonActions()
            if (isRunner) {
                initRunnerActions()
            } else {
                initChaserActions()
            }
        }
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

    private suspend fun initCommonActions() {
        val playingArea = zoneDao.getPlayingArea()!!
        drawZone(playingArea)
        drawMyLocation()
    }

    private suspend fun initRunnerActions() {
        // TODO: Show runner UI
        Log.d("RUNNER", "is runner")
        val nextUpdate = runnerDao.getNextUpdateTime(gameId)
        if (nextUpdate == null) {
            applyHeadStart()
        }
        scheduleRunnerLocationUpdates()
    }

    private suspend fun initChaserActions() {
        Log.d("CHASER", "is chaser")
        for (zone in zoneDao.getZones()) {
            if (zone.type in listOf(ZoneTypes.STORE, ZoneTypes.ATM)) {
                drawZone(zone)
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            updateChasersOnMap()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            updateRunnerOnMap()
        }
    }

    private fun drawMyLocation() {
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
                    chasers[player.id!!] = drawPlayer(
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
            if (it.nextUpdate != null) {
                val updateDelay = calculateDelay(it.nextUpdate)
                startTimer(timers.runnerLocationTimer, updateDelay)
            }

            if (it.latitude == null || it.longitude == null) {
                // Runner location is not set on initial collect
                return@collect
            }
            if (!::runner.isInitialized) {
                runner = drawPlayer(
                    it.latitude, it.longitude, Color.GREEN, R.drawable.agent
                )
            } else {
                runner.location = GeoPoint(it.latitude, it.longitude)
                map.invalidate()
            }
        }
    }

    private fun drawPlayer(
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

    private fun drawZone(zone: Zone) {
        val location = GeoPoint(zone.latitude, zone.longitude)
        val overlay: Overlay
        when (zone.type) {
            ZoneTypes.PLAYING_AREA -> {
                overlay = Polygon()
                overlay.points = Polygon.pointsAsCircle(location, zone.radius.toDouble())
            }

            in listOf(ZoneTypes.ATM, ZoneTypes.STORE) -> {
                // Create icon
                val overlayItem = OverlayItem("ATM", "ATM Location", location)
                if (zone.type == ZoneTypes.ATM) {
                    val customIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.info)
                    overlayItem.setMarker(customIcon)
                } else {
                    val customIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.cards)
                    overlayItem.setMarker(customIcon)
                }
                overlayItem.markerHotspot = OverlayItem.HotspotPlace.CENTER

                // Create radius around icon
                val circle = Polygon()
                circle.points = Polygon.pointsAsCircle(location, zone.radius.toDouble())
                map.overlays.add(circle)

                val items = ArrayList<OverlayItem>()
                items.add(overlayItem)
                overlay = ItemizedIconOverlay(this,
                    items,
                    object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                        override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                            Log.d("OVERLAY", "Item tapped")
                            // Handle single tap
                            return true
                        }

                        override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                            Log.d("OVERLAY", "Item long press")
                            // Handle long press
                            return false
                        }
                    })

            }

            else -> {
                return
            }
        }

        map.overlays.add(overlay)
    }

    private fun startTimer(timer: CustomTimer, time: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            timer.setTime(time)
            timer.startTimer()
        }
    }

}