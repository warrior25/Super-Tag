package com.huikka.supertag

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huikka.supertag.data.LoginDataSource
import com.huikka.supertag.data.LoginRepository
import com.huikka.supertag.data.model.Game
import com.huikka.supertag.data.model.Player
import com.huikka.supertag.databinding.ActivityMainBinding
import com.huikka.supertag.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private var db = Firebase.firestore

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var loginRepository: LoginRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", { CoroutineScope(Dispatchers.Main).launch { hostGame() } }).show()
        }

        val loginDataSource = LoginDataSource()
        loginRepository = LoginRepository(loginDataSource)

        if (!loginRepository.isLoggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            Log.d("LOGIN", loginRepository.user?.uid!!)
        }

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val locationListener = ChaserLocationListener()

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, locationListener)
                }
            }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private suspend fun hostGame() {
        var randomString = ""
        while (true) {
            randomString = List(6) { ('A'..'Z').random() }.joinToString("")
            if (isCodeUnique(randomString)) {
                break
            }
        }

        db.collection("games").add(Game(randomString, chasers = listOf(Player(loginRepository.user?.uid!!, loginRepository.user?.displayName!!))))

    }

    suspend fun isCodeUnique(code: String): Boolean {
        val querySnapshot = db
            .collection("games")
            .whereEqualTo("id", code)
            .get().await()
        return querySnapshot.isEmpty
    }

    suspend fun joinGame(gameId: String) {
        val gameRef = db.collection("games").whereEqualTo("id", gameId).get().await()
        if (!gameRef.isEmpty) {
            // Game exists, join the game
            gameRef.documents[0].reference.update("players", FieldValue.arrayUnion(loginRepository.user?.uid!!))
        } else {
            // Game does not exist, handle the error
            Log.e("MainActivity", "Game does not exist")
        }
    }

    suspend fun leaveGame(gameId: String) {
        val gameRef = db.collection("games").whereEqualTo("id", gameId).get().await()
        if (!gameRef.isEmpty) {
            gameRef.documents[0].reference.update("players", FieldValue.arrayRemove(loginRepository.user?.uid!!))
        } else {
            Log.e("MainActivity", "Game does not exist")
        }
    }
}