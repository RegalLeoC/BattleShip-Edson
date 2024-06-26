package com.example.battleship

/*

import android.os.Bundle
import android.widget.AdapterView
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var gameId: String
    private lateinit var turnIndicator: TextView
    private lateinit var enemyGrid: GridView
    private lateinit var playerGrid: GridView

    private lateinit var enemyGridAdapter: GridAdapter
    private lateinit var playerGridAdapter: GridAdapter

    private lateinit var enemyGridItems: MutableList<GridItem>
    private lateinit var playerGridItems: MutableList<GridItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        gameId = intent.getStringExtra("gameId") ?: ""

        turnIndicator = findViewById(R.id.turnIndicator)
        enemyGrid = findViewById(R.id.enemyGrid)
        playerGrid = findViewById(R.id.playerGrid)

        initGrids()
        //updateTurnIndicator(game)
    }

    private fun initGrids() {
        enemyGridItems = MutableList(64) { GridItem(false, false, it) }
        playerGridItems = MutableList(64) { GridItem(false, false, it) }

        enemyGridAdapter = GridAdapter(this, enemyGridItems)
        playerGridAdapter = GridAdapter(this, playerGridItems)

        enemyGrid.adapter = enemyGridAdapter
        playerGrid.adapter = playerGridAdapter

        enemyGrid.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val currentUser = auth.currentUser?.uid
            val gameRef = db.collection("games").document(gameId)
            gameRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val game = document.toObject(Game::class.java)
                    if (game != null && game.turn == currentUser) {
                        handleEnemyGridClick(game, position)
                    } else {
                        Toast.makeText(this, "Not your turn", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Generate and display ships if not already present
        generateAndSaveShips()
    }

    private fun generateAndSaveShips() {
        val gameRef = db.collection("games").document(gameId)
        val currentUser = auth.currentUser?.uid

        gameRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val game = document.toObject(Game::class.java)
                if (game != null && currentUser != null) {
                    if ((currentUser == game.player1 && game.playerShips.isEmpty()) ||
                        (currentUser == game.player2 && game.enemyShips.isEmpty())) {
                        val ships = generateShips()
                        val field = if (currentUser == game.player1) "playerShips" else "enemyShips"
                        gameRef.update(field, ships).addOnSuccessListener {
                            loadGridData(game)
                        }
                    } else {
                        loadGridData(game)
                    }
                }
            }
        }
    }

    private fun generateShips(): List<GridItem> {
        val shipSizes = listOf(4, 3, 3, 2, 2)
        val ships = mutableListOf<GridItem>()
        val random = Random.Default

        for (size in shipSizes) {
            var placed = false
            while (!placed) {
                val start = random.nextInt(64)
                val horizontal = random.nextBoolean()
                if (canPlaceShip(start, size, horizontal)) {
                    for (i in 0 until size) {
                        val pos = if (horizontal) start + i else start + i * 8
                        ships.add(GridItem(isShip = true, isHit = false, position = pos))
                    }
                    placed = true
                }
            }
        }
        return ships
    }

    private fun canPlaceShip(start: Int, size: Int, horizontal: Boolean): Boolean {
        for (i in 0 until size) {
            val pos = if (horizontal) start + i else start + i * 8
            if (pos >= 64 || (horizontal && start % 8 + i >= 8)) {
                return false
            }
            if (playerGridItems[pos].isShip) {
                return false
            }
        }
        return true
    }

    private fun loadGridData(game: Game) {
        val currentUser = auth.currentUser?.uid

        // Update the player's grid based on the stored data
        val playerShips = if (currentUser == game.player1) game.playerShips else game.enemyShips
        for (gridItem in playerShips) {
            playerGridItems[gridItem.position].apply {
                isShip = true
                isHit = gridItem.isHit
            }
        }
        playerGridAdapter.notifyDataSetChanged()

        // Update the enemy's grid based on the stored data
        val enemyHits = if (currentUser == game.player1) game.enemyHits else game.playerShips
        for (gridItem in enemyHits) {
            enemyGridItems[gridItem.position].apply {
                isHit = true
                isShip = gridItem.isShip
            }
        }
        enemyGridAdapter.notifyDataSetChanged()
    }


    private fun handleEnemyGridClick(game: Game, position: Int) {
        val gameRef = db.collection("games").document(gameId)
        val item = enemyGridItems[position]
        if (!item.isHit) {
            item.isHit = true
            if (isShipAtPosition(game.enemyShips, position)) {
                item.isShip = true
                updatePlayerView(game, position, true)
                checkForWin(game)
            } else {
                updatePlayerView(game, position, false)
            }
            enemyGridAdapter.notifyDataSetChanged()
            switchTurn(game)
        }
    }

    private fun isShipAtPosition(ships: List<GridItem>, position: Int): Boolean {
        return ships.any { it.position == position && it.isShip }
    }

    private fun updatePlayerView(game: Game, position: Int, isHit: Boolean) {
        val currentUser = auth.currentUser?.uid
        val field = if (currentUser == game.player1) "playerHits" else "enemyHits"
        val updatedGrid = if (isHit) game.enemyShips else game.enemyHits

        val gameRef = db.collection("games").document(gameId)
        gameRef.update(field, updatedGrid)
    }

    private fun switchTurn(game: Game) {
        val nextTurn = if (game.turn == game.player1) game.player2 else game.player1
        val gameRef = db.collection("games").document(gameId)
        gameRef.update("turn", nextTurn).addOnSuccessListener {
            //updateTurnIndicator()
            enableDisableGrids(nextTurn)
        }
    }

    private fun updateTurnIndicator(game: Game) {
        val currentUser = auth.currentUser?.uid
        val currentTurn = game.turn
        if (currentTurn == currentUser) {
            turnIndicator.text = "Your Turn"
        } else {
            turnIndicator.text = "Opponent's Turn"
        }
    }


    private fun enableDisableGrids(nextTurn: String?) {
        val currentUser = auth.currentUser?.uid
        enemyGrid.isEnabled = nextTurn == currentUser
    }

    private fun checkForWin(game: Game) {
        val gameRef = db.collection("games").document(gameId)
        val currentUser = auth.currentUser?.uid
        val enemyShips = if (currentUser == game.player1) game.enemyShips else game.playerShips
        if (enemyShips.all { it.isHit }) {
            Toast.makeText(this, "You won!", Toast.LENGTH_LONG).show()
            gameRef.delete()
        }
    }
}

       */