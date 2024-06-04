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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.RunnerDao
import com.huikka.supertag.data.dao.ZoneDao
import com.huikka.supertag.data.dto.Card
import com.huikka.supertag.data.dto.Zone
import com.huikka.supertag.data.helpers.Config
import com.huikka.supertag.data.helpers.Sides
import com.huikka.supertag.data.helpers.TimeConverter
import com.huikka.supertag.data.helpers.ZoneTypes
import com.huikka.supertag.fragments.ChaserTimers
import com.huikka.supertag.fragments.RunnerTimers
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

    private lateinit var myLocationOverlay: MyLocationNewOverlay

    private var cards: ArrayList<Card> = ArrayList()
    private lateinit var adapter: CardListAdapter
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

    private lateinit var chaserTimers: ChaserTimers
    private lateinit var runnerTimers: RunnerTimers

    private var runnerZoneOverlays = mutableListOf<Overlay>()

    private lateinit var zones: List<Zone>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        // HTTP User-Agent variable to not ban other users
        getInstance().userAgentValue = applicationContext.packageName
        setContentView(R.layout.activity_game)

        val view = LayoutInflater.from(this).inflate(R.layout.cards_sheet_layout, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        adapter = CardListAdapter(cards)
        recyclerView.adapter = adapter

        val locationButton = findViewById<ImageButton>(R.id.locationButton)
        locationButton.setOnClickListener {
            myLocationOverlay.enableFollowLocation()

        }

        val app = application as STApplication
        authDao = AuthDao(app)
        gameDao = GameDao(app)
        playerDao = PlayerDao(app)
        runnerDao = RunnerDao(app)
        zoneDao = ZoneDao(app)

        val cardsButton = findViewById<ImageButton>(R.id.cardsButton)
        cardsButton.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            cards.add(Card("Card 1", "Test Card", 100, R.drawable.cards))
            cards.add(Card("Card 2", "Second Card", 200, R.drawable.cards))

        }
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        chaserTimers = supportFragmentManager.findFragmentById(R.id.chaserTimers) as ChaserTimers
        runnerTimers = supportFragmentManager.findFragmentById(R.id.runnerTimers) as RunnerTimers

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
            zones = zoneDao.getZones()

            val intent = Intent(applicationContext, LocationUpdateService::class.java)
            startForegroundService(intent)

            lifecycleScope.launch(Dispatchers.Main) {
                initCommonActions()
            }
            if (isRunner) {
                initRunnerActions()
            } else {
                initChaserActions()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.game_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.info) {
            Toast.makeText(this, "Info", Toast.LENGTH_SHORT).show()
        }
        if (id == R.id.leave_game) {
            Toast.makeText(this, "Leave", Toast.LENGTH_SHORT).show()
        }
        return true
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
        drawZoneRadius(playingArea)
        drawMyLocation()
    }

    private suspend fun initRunnerActions() {
        Log.d("RUNNER", "is runner")
        lifecycleScope.launch {
            findViewById<View>(R.id.chaserTimers).visibility = View.GONE
        }
        val nextUpdate = runnerDao.getNextUpdateTime(gameId)
        if (nextUpdate == null) {
            applyHeadStart()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            scheduleRunnerLocationUpdates()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            selectActiveRunnerZones()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            checkRunnerZonePresence()
        }
    }

    private suspend fun initChaserActions() {
        Log.d("CHASER", "is chaser")
        lifecycleScope.launch {
            findViewById<View>(R.id.runnerTimers).visibility = View.GONE
        }

        val chaserZones = mutableListOf<Zone>()
        for (zone in zones) {
            if (zone.type in listOf(ZoneTypes.STORE, ZoneTypes.ATM)) {
                chaserZones.add(zone)
            }
        }
        drawZones(chaserZones)

        lifecycleScope.launch(Dispatchers.IO) {
            updateChasersOnMap()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            updateRunnerOnMap()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            checkChaserZonePresence()
        }
    }

    private suspend fun checkChaserZonePresence() {
        val flow = playerDao.getPlayerByIdFlow(playerId)
        var lastZoneId: Int? = null

        flow.collect { player ->
            val currentZoneId = player.zoneId
            if (currentZoneId == null) {
                chaserTimers.moneyTimer.stopTimer()
            } else if (currentZoneId != lastZoneId) {
                val currentZone = zoneDao.getZoneById(currentZoneId)
                when (currentZone!!.type) {
                    ZoneTypes.ATM -> {
                        startTimer(chaserTimers.moneyTimer, Config.ATM_TIME) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                gameDao.addMoney(gameId, Sides.Chasers, Config.ATM_MONEY)
                            }
                        }
                    }

                    ZoneTypes.STORE -> {
                        startTimer(chaserTimers.moneyTimer, Config.STORE_TIME) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                gameDao.addMoney(gameId, Sides.Chasers, Config.STORE_MONEY)
                            }
                        }
                    }
                }
            }
            lastZoneId = currentZoneId
        }
    }

    private suspend fun checkRunnerZonePresence() {
        val flow = playerDao.getPlayerByIdFlow(playerId)
        var lastZoneId: Int? = null

        flow.collect { player ->
            val currentZoneId = player.zoneId
            if (currentZoneId == null) {
                chaserTimers.moneyTimer.stopTimer()
            } else if (currentZoneId != lastZoneId) {
                val currentZone = zoneDao.getZoneById(currentZoneId)!!
                val activeZones = gameDao.getActiveRunnerZones(gameId)!!
                if (currentZone.type == ZoneTypes.ATTRACTION && currentZone.id!! in activeZones) {
                    startTimer(runnerTimers.moneyTimer, Config.ATTRACTION_TIME) {
                        lifecycleScope.launch {
                            gameDao.addMoney(gameId, Sides.Runner, Config.ATTRACTION_MONEY)
                            runnerTimers.zoneTimer.setTime(0)
                        }
                    }
                }
            }
            lastZoneId = currentZoneId
        }
    }

    private suspend fun selectActiveRunnerZones() {
        val zone1 = zones.random()
        var zone2: Zone
        do {
            zone2 = zones.random()
        } while (zone1.id == zone2.id)

        val activeZoneIds = listOf(zone1.id!!, zone2.id!!)
        val activeZones = listOf(zone1, zone2)
        gameDao.setActiveRunnerZones(gameId, activeZoneIds)

        for (overlay in runnerZoneOverlays) {
            map.overlays.remove(overlay)
        }
        runnerZoneOverlays = drawZones(activeZones)

        startTimer(runnerTimers.zoneTimer, Config.RUNNER_ZONE_SHUFFLE_TIME) {
            lifecycleScope.launch(Dispatchers.IO) {
                selectActiveRunnerZones()
            }
        }
    }

    private fun drawMyLocation() {
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), this.map)
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
                startTimer(chaserTimers.runnerLocationTimer, updateDelay)
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

    private fun drawZoneRadius(zone: Zone): Overlay {
        val location = GeoPoint(zone.latitude!!, zone.longitude!!)
        val overlay = Polygon()
        overlay.points = Polygon.pointsAsCircle(location, zone.radius!!.toDouble())

        map.overlays.add(overlay)
        return overlay
    }

    private fun drawZones(zones: List<Zone>): MutableList<Overlay> {
        val overlays = mutableListOf<Overlay>()
        val items = ArrayList<OverlayItem>()

        for (zone in zones) {
            val location = GeoPoint(zone.latitude!!, zone.longitude!!)
            val overlayItem = OverlayItem("ATM", "ATM Location", location)
            val drawableId: Int = resources.getIdentifier(
                zone.drawable, "drawable", applicationContext.packageName
            )
            val customIcon = ContextCompat.getDrawable(this, drawableId)
            overlayItem.setMarker(customIcon)
            overlayItem.markerHotspot = OverlayItem.HotspotPlace.CENTER

            // Create radius around icon
            val radiusOverlay = drawZoneRadius(zone)
            overlays.add(radiusOverlay)

            items.add(overlayItem)
        }

        val overlay = ItemizedIconOverlay(this,
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

        map.overlays.add(overlay)
        overlays.add(overlay)

        return overlays
    }

    private fun startTimer(timer: CustomTimer, time: Long, onTimeout: () -> Unit = {}) {
        lifecycleScope.launch(Dispatchers.Main) {
            timer.setTime(time)
            timer.startTimer(onTimeout)
        }
    }

}