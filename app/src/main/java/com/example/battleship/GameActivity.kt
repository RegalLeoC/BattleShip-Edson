package com.example.battleship

import android.os.Bundle
import android.widget.AdapterView
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        updateTurnIndicator()
    }

    private fun initGrids() {
        // Initialize the grid items with default values
        enemyGridItems = MutableList(64) { GridItem(false, false, it) }
        playerGridItems = MutableList(64) { GridItem(false, false, it) }

        // Set up the adapters
        enemyGridAdapter = GridAdapter(this, enemyGridItems)
        playerGridAdapter = GridAdapter(this, playerGridItems)

        enemyGrid.adapter = enemyGridAdapter
        playerGrid.adapter = playerGridAdapter

        // Set item click listener for the enemy grid
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

        // Load initial grid data from Firestore
        loadGridData()
    }

    private fun loadGridData() {
        val gameRef = db.collection("games").document(gameId)
        gameRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val game = document.toObject(Game::class.java)
                if (game != null) {
                    // Update the player's grid based on the stored data
                    for (gridItem in game.playerShips) {
                        playerGridItems[gridItem.position].isShip = true
                    }
                    playerGridAdapter.notifyDataSetChanged()

                    // Update the enemy's grid based on the stored data
                    for (gridItem in game.enemyHits) {
                        val position = gridItem.position
                        enemyGridItems[position].isHit = true
                        if (isShipAtPosition(game.enemyShips, position)) {
                            enemyGridItems[position].isShip = true
                        }
                    }
                    enemyGridAdapter.notifyDataSetChanged()

                    updateTurnIndicator()
                }
            }
        }
    }

    private fun isShipAtPosition(shipPositions: List<GridItem>, position: Int): Boolean {
        return shipPositions.any { it.position == position }
    }

    private fun handleEnemyGridClick(game: Game, position: Int) {
        val gameRef = db.collection("games").document(gameId)
        val item = enemyGridItems[position]
        if (!item.isHit) {
            item.isHit = true
            if (isShipAtPosition(game.enemyShips, position)) {
                item.isShip = true
                enemyGridAdapter.notifyDataSetChanged()
                checkForWin(game)
            } else {
                enemyGridAdapter.notifyDataSetChanged()
            }
            switchTurn(game)
        }
    }

    private fun switchTurn(game: Game) {
        val gameRef = db.collection("games").document(gameId)
        val nextTurn = if (game.turn == game.player1) game.player2 else game.player1

        gameRef.update("turn", nextTurn)
            .addOnSuccessListener {
                updateTurnIndicator()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to switch turn.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTurnIndicator() {
        val gameRef = db.collection("games").document(gameId)
        gameRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val game = document.toObject(Game::class.java)
                val currentTurn = game?.turn
                val currentUser = auth.currentUser?.uid

                if (currentTurn == currentUser) {
                    turnIndicator.text = "Your Turn"
                    enemyGrid.isEnabled = true
                } else {
                    turnIndicator.text = "Enemy's Turn"
                    enemyGrid.isEnabled = false
                }
            }
        }
    }

    private fun checkForWin(game: Game) {
        val enemyShipsHit = enemyGridItems.count { it.isHit && it.isShip }
        if (enemyShipsHit == game.enemyShips.size) {
            Toast.makeText(this, "You Win!", Toast.LENGTH_SHORT).show()
            // Handle win logic here
        }
    }
}
