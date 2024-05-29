package com.huikka.supertag

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import com.huikka.supertag.data.helpers.GameStatuses
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LobbyActivity : AppCompatActivity() {

    private lateinit var gameDao: GameDao
    private lateinit var authDao: AuthDao
    private lateinit var playerDao: PlayerDao
    private lateinit var gameId: String
    private var isHost: Boolean = false

    private lateinit var playerId: String

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

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val startButton = findViewById<Button>(R.id.startButton)
        playerId = authDao.getUser()!!.id

        CoroutineScope(Dispatchers.Main).launch {
            val game = gameDao.getCurrentGameInfo(playerId)
            gameId = game.gameId!!
            isHost = game.isHost

            val gameIdView = findViewById<TextView>(R.id.game_id)
            gameIdView.text = gameId

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
            adapter = PlayerListAdapter(app, players, isHost)
            recyclerView.adapter = adapter

            if (isHost) {
                startButton.visibility = View.VISIBLE
                if (gameDao.getRunnerId(gameId) == null) {
                    gameDao.setRunnerId(gameId, playerId)
                }
                startButton.setOnClickListener {
                    val runner = adapter.getRunner()
                    Log.d("TAG", runner.toString())
                }
                toolbar.menu.findItem(R.id.pick_random).isVisible = true
                toolbar.menu.findItem(R.id.settings).isVisible = true
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val flow = gameDao.getGameFlow(gameId)
                    flow.collect { game ->
                        CoroutineScope(Dispatchers.Main).launch {
                            adapter.setRunner(game.runnerId!!)
                            if (game.status == GameStatuses.PLAYING) {
                                startGameActivity()
                            }
                        }
                    }
                }
            }

            startButton.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    gameDao.setGameStatus(gameId, GameStatuses.PLAYING)
                    startGameActivity()
                }
            }
            getPlayers()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.lobby_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
            val intent = Intent(baseContext, SettingsActivity::class.java)
            startActivity(intent)
        }
        if (id == R.id.pick_random) {
            Toast.makeText(this, "Pick Random", Toast.LENGTH_SHORT).show()
            adapter.selectRandom()
        }
        if (id == R.id.leave_game) {
            Toast.makeText(this, "Leave", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                leaveGame()
            }
        }
        return true
    }

    private suspend fun leaveGame() {
        if (isHost) {
            gameDao.removeGame(gameId)
        } else {
            playerDao.removeFromGame(playerId)
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

    private fun startGameActivity() {
        val intent = Intent(baseContext, GameActivity::class.java)
        startActivity(intent)
    }
}