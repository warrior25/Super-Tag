package com.huikka.supertag

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.huikka.supertag.data.AuthDao
import com.huikka.supertag.data.GameDao
import com.huikka.supertag.data.model.Player

class LobbyActivity : AppCompatActivity() {

    private val db = GameDao()
    private val auth = AuthDao()
    private lateinit var gameId: String

    private var players: ArrayList<Player> = ArrayList()
    private val adapter = PlayerListAdapter(players, this)

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
        val gameIdView = findViewById<TextView>(R.id.game_id)
        gameIdView.text = gameId

        // Just for testing until players are fetched from DB
        players.add(Player(id = "1", name = "Player 1"))
        players.add(Player(id = "2", name = "Player 2"))
        players.add(Player(id = "3", name = "Player 3"))

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter

        val randomButton = findViewById<Button>(R.id.pick_random)
        randomButton.setOnClickListener {
            adapter.selectRandom();
        }
    }

    private suspend fun leaveGame() {
        db.removeChaser(auth.user?.uid!!, gameId)
    }
}