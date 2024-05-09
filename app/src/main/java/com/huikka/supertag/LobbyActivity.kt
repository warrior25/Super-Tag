package com.huikka.supertag

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.huikka.supertag.data.AuthDao
import com.huikka.supertag.data.GameDao
import com.huikka.supertag.data.model.Game
import com.huikka.supertag.data.model.Player

class LobbyActivity : AppCompatActivity() {

    private val db = GameDao()
    private val auth = AuthDao()
    private lateinit var gameId: String

    private val database = db.getDatabase()
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

        getPlayers()

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