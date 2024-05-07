package com.huikka.supertag

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huikka.supertag.data.LoginDataSource
import com.huikka.supertag.data.LoginRepository
import com.huikka.supertag.data.model.Game
import com.huikka.supertag.data.model.Player
import com.huikka.supertag.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private var db = Firebase.firestore
    private lateinit var loginRepository: LoginRepository

    // UI elements
    private lateinit var joinGameButton: Button
    private lateinit var hostGameButton: FloatingActionButton
    private lateinit var gameIdEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup button actions
        joinGameButton = findViewById(R.id.joinGameButton)
        gameIdEditText = findViewById(R.id.gameIdEditText)
        joinGameButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                joinGame(gameIdEditText.text.toString())
            }
        }

        hostGameButton = findViewById(R.id.hostGameButton)
        hostGameButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                hostGame()
            }
        }

        // Setup firebase user authentication
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


        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            } else {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10f, locationListener
                )
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 5000, 10f, locationListener
        )
    }

    private suspend fun hostGame() {
        var randomCode: String
        while (true) {
            randomCode = List(6) { ('A'..'Z').random() }.joinToString("")
            if (isCodeUnique(randomCode)) {
                break
            }
        }

        db.collection("games").add(
            Game(
                randomCode, chasers = listOf(
                    Player(
                        loginRepository.user?.uid!!, loginRepository.user?.displayName!!
                    )
                )
            )
        )

        Toast.makeText(
            applicationContext, "Created game $randomCode", Toast.LENGTH_LONG
        ).show()

    }

    private suspend fun isCodeUnique(code: String): Boolean {
        val querySnapshot = db.collection("games").whereEqualTo("id", code).get().await()
        return querySnapshot.isEmpty
    }

    private suspend fun joinGame(gameId: String) {
        val gameRef = db.collection("games").whereEqualTo("id", gameId).get().await()
        if (!gameRef.isEmpty) {
            // Game exists, join the game
            gameRef.documents[0].reference.update(
                "chasers", FieldValue.arrayUnion(loginRepository.user?.uid!!)
            )
            Toast.makeText(
                applicationContext, "Joined game $gameId", Toast.LENGTH_LONG
            ).show()
        } else {
            // Game does not exist
            Toast.makeText(
                applicationContext, "Game $gameId does not exist", Toast.LENGTH_LONG
            ).show()
        }
    }

    suspend fun leaveGame(gameId: String) {
        val gameRef = db.collection("games").whereEqualTo("id", gameId).get().await()
        if (!gameRef.isEmpty) {
            gameRef.documents[0].reference.update(
                "players", FieldValue.arrayRemove(loginRepository.user?.uid!!)
            )
        } else {
            Log.e("MainActivity", "Game does not exist")
        }
    }
}