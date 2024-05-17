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
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dto.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LobbyActivity : AppCompatActivity() {

    private lateinit var gameDao: GameDao
    private lateinit var authDao: AuthDao
    private lateinit var playerDao: PlayerDao
    private lateinit var gameId: String
    private var isHost: Boolean = false

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

        val app = application as STApplication
        authDao = AuthDao(app)
        gameDao = GameDao(app)
        playerDao = PlayerDao(app)

        val randomButton = findViewById<Button>(R.id.pick_random)
        val startButton = findViewById<Button>(R.id.startButton)
        val leaveButton = findViewById<Button>(R.id.leaveButton)

        CoroutineScope(Dispatchers.Main).launch {
            val game = gameDao.getCurrentGameInfo(authDao.getUser()!!.id)
            gameId = game.gameId!!
            isHost = game.isHost

            val gameIdView = findViewById<TextView>(R.id.game_id)
            gameIdView.text = gameId

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
            adapter = PlayerListAdapter(app, players, isHost)
            recyclerView.adapter = adapter

            if (isHost) {
                randomButton.visibility = View.VISIBLE
                startButton.visibility = View.VISIBLE
                if (gameDao.getRunnerId(gameId) == null) {
                    gameDao.setRunnerId(gameId, authDao.getUser()!!.id)
                }
                randomButton.setOnClickListener {
                    adapter.selectRandom()
                }
                startButton.setOnClickListener {
                    val runner = adapter.getRunner()
                    Log.d("TAG", runner.toString())
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val flow = gameDao.getGameFlow(gameId)
                    flow.collect {
                        CoroutineScope(Dispatchers.Main).launch {
                            adapter.setRunner(it.runnerId!!)
                        }
                    }
                }
            }


            leaveButton.setOnClickListener {
                lifecycleScope.launch {
                    leaveGame()
                }
            }

            getPlayers()
        }
    }

    private suspend fun leaveGame() {
        if (isHost) {
            gameDao.removeGame(gameId)
        } else {
            gameDao.removePlayer(authDao.getUser()!!.id)
        }
        finish()
    }

    private suspend fun getPlayers() {
        val flow = playerDao.getPlayersByGameIdFlow(gameId)
        flow.collect {
            players.clear()
            for (player in it) {
                players.add(player)
            }
            adapter.notifyDataSetChanged()
        }
    }
}