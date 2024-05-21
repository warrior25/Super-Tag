package com.huikka.supertag

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        // HTTP User-Agent variable to not ban other users
        getInstance().userAgentValue = applicationContext.packageName
        setContentView(R.layout.activity_game)

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
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // map initialization
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.maxZoomLevel = 18.0
        map.minZoomLevel = 12.0

        map.setMultiTouchControls(true)
        map.setTilesScaledToDpi(true)

        val mapController = map.controller
        val startPoint = GeoPoint(61.4498, 23.8595)
        mapController.setCenter(startPoint);
        mapController.setZoom(15.0)

        // draw items on map
        drawUserOnMap()
        drawPlayersOnMap()
        drawATMsV1()
        drawATMsV2()


        // Mandatory copy-right mention
        val cr = CopyrightOverlay(this)
        map.overlays.add(cr)
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

    private fun drawUserOnMap() {
        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), this.map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true
        map.overlays.add(myLocationOverlay)
    }

    private fun drawPlayersOnMap() {
        val conf = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(100, 100, conf)
        val canvas1 = Canvas(bmp)

        // paint defines the text color, stroke width and size
        val color = Paint()
        color.textSize = 35f
        color.color = Color.WHITE

        // draw background color for bitmap
        canvas1.drawCircle(50f, 50f, 50f, color)

        // modify canvas
        canvas1.drawBitmap(
            BitmapFactory.decodeResource(
                resources, R.drawable.agent
            ), 15f, 15f, color
        )


        // Add player to map
        val playerLocation = GeoPoint(61.4478, 23.8620)
        val playerOverlay = DirectedLocationOverlay(this)
        playerOverlay.location = playerLocation
        playerOverlay.setDirectionArrow(bmp)

        map.overlays.add(playerOverlay)

        //TODO("Get players from database")
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