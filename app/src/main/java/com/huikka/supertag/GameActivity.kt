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
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    // TODO: Set dynamically (e.g based on difficulty)
    private var locationUpdateInterval = 10 * minute
    private var minLocationUpdateInterval = minute

    private lateinit var authDao: AuthDao
    private lateinit var gameDao: GameDao
    private lateinit var playerDao: PlayerDao

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

        val intent = Intent(this, LocationUpdateService::class.java)
        startForegroundService(intent)

        CoroutineScope(Dispatchers.Main).launch {
            playerId = authDao.getUser()!!.id
            gameId = gameDao.getCurrentGameInfo(playerId).gameId!!
            runnerId = gameDao.getRunnerId(gameId)!!
            isRunner = playerId == runnerId

            if (isRunner) {
                // TODO: Show runner UI
                Log.d("RUNNER", "is runner")
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    updateChasersOnMap()
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

            // Force refresh map
            // https://stackoverflow.com/a/72153233
            map.controller.setCenter(map.mapCenter)
        }
    }

    private suspend fun updateRunnerOnMap() {
        val loc = playerDao.getPlayerLocation(runnerId)
        if (!::runner.isInitialized) {
            runner = drawPlayerOnMap(loc.latitude!!, loc.longitude!!, Color.GREEN, R.drawable.agent)
            return
        }
        runner.location = GeoPoint(loc.latitude!!, loc.longitude!!)
        map.controller.setCenter(map.mapCenter)
        Log.d("RUNNER", "runner location updated")
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


}