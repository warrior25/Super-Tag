package com.huikka.supertag

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.huikka.supertag.data.AuthDao
import com.huikka.supertag.data.GameDao
import com.huikka.supertag.data.model.Game
import com.huikka.supertag.data.model.Player
import kotlinx.coroutines.launch

class LobbyActivity : AppCompatActivity() {

    private val db = GameDao()
    private val auth = AuthDao()
    private lateinit var gameId: String
    private var isHost: Boolean = false

    private val database = db.getDatabase()
    private var players: ArrayList<Player> = ArrayList()
    private lateinit var adapter: PlayerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lobby)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gameId = intent.getStringExtra("GAME_ID")!!
        isHost = intent.getBooleanExtra("HOST", false)

        val gameIdView = findViewById<TextView>(R.id.game_id)
        gameIdView.text = gameId

        getPlayers()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        adapter = PlayerListAdapter(players, isHost)
        recyclerView.adapter = adapter

        val randomButton = findViewById<Button>(R.id.pick_random)
        val startButton = findViewById<Button>(R.id.startButton)
        val leaveButton = findViewById<Button>(R.id.leaveButton)

        leaveButton.setOnClickListener {
            lifecycleScope.launch {
                leaveGame()
            }
        }

        if (isHost) {
            randomButton.setOnClickListener {
                adapter.selectRandom()
            }
            startButton.setOnClickListener {
                val runner = adapter.getRunner()
                Log.d("TAG", runner.toString())
            }
        } else {
            randomButton.visibility = View.GONE
            startButton.visibility = View.GONE
        }
    }

    private suspend fun leaveGame() {
        if (isHost) {
            db.removeGame(gameId)
        } else {
            db.removeChaser(auth.user?.uid!!, gameId)
        }
        finish()
    }

    private fun getPlayers() {
        database.collection("games").whereEqualTo(/* field = */ "id", /* value = */ gameId)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("Error", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val game = value!!.documents[0].toObject(Game::class.java)
                for (player in game!!.chasers) {
                    players.add(player)
                }
                adapter.notifyDataSetChanged()
            }
    }
}