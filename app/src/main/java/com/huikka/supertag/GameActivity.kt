package com.huikka.supertag

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.huikka.supertag.data.dto.Card
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

    private lateinit var myLocationOverlay: MyLocationNewOverlay

    private var cards: ArrayList<Card> = ArrayList()
    private lateinit var adapter: CardListAdapter
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

        val cardsButton = findViewById<ImageButton>(R.id.cardsButton)
        cardsButton.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            cards.add(Card("Card 1", "Test Card", 100, R.drawable.cards))
            cards.add(Card("Card 2", "Second Card", 200, R.drawable.cards))

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
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), this.map)
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