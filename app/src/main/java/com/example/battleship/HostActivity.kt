package com.example.battleship

import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class HostActivity : AppCompatActivity() {

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

    private lateinit var gameRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        gameId = intent.getStringExtra("gameId") ?: ""
        gameRef = db.collection("games").document(gameId)

        turnIndicator = findViewById(R.id.turnIndicator)
        enemyGrid = findViewById(R.id.enemyGrid)
        playerGrid = findViewById(R.id.playerGrid)

        initGrids()
        listenForUpdates()
    }

    private fun initGrids() {
        // Ensure positions are correctly enumerated
        enemyGridItems = MutableList(64) { GridItem(false, position = it, false) }
        playerGridItems = MutableList(64) { GridItem(false, position =  it, false) }

        for (i in enemyGridItems.indices){
            enemyGridItems[i].position = i
        }

        for (i in playerGridItems.indices) {
            playerGridItems[i].position = i
        }


        enemyGridAdapter = GridAdapter(this, enemyGridItems, isEnemyGrid = true)
        playerGridAdapter = GridAdapter(this, playerGridItems)

        enemyGrid.adapter = enemyGridAdapter
        playerGrid.adapter = playerGridAdapter

        enemyGrid.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            handleEnemyGridClick(position)
        }

        val currentUser = auth.currentUser?.uid
        gameRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val game = document.toObject(Game::class.java)
                if (game != null) {
                    if (currentUser == game.player1) {
                        if (game.player1Ships.isEmpty()) {
                            val ships = generateShips()
                            gameRef.update("player1Ships", ships).addOnSuccessListener {
                                updatePlayerGrid(ships)
                            }
                        } else {
                            updatePlayerGrid(game.player1Ships)
                        }
                    } else {
                        if (game.player2Ships.isEmpty()) {
                            val ships = generateShips()
                            gameRef.update("player2Ships", ships).addOnSuccessListener {
                                updatePlayerGrid(ships)
                            }
                        } else {
                            updatePlayerGrid(game.player2Ships)
                        }
                    }
                }
            }
        }
    }

    private fun generateShips(): List<GridItem> {
        val shipSizes = listOf(4, 3, 3, 2, 2)
        val ships = MutableList(64) { GridItem(false, it, false) }
        val random = java.util.Random()

        for (size in shipSizes) {
            var placed = false
            var attempts = 0 // To prevent infinite loops
            while (!placed && attempts < 100) {
                attempts++
                val start = random.nextInt(64)
                val horizontal = random.nextBoolean()
                if (canPlaceShip(start, size, horizontal, ships)) {
                    Log.d("GenerateShips", "Placing ship of size $size at start $start horizontally: $horizontal")
                    for (i in 0 until size) {
                        val pos = if (horizontal) start + i else start + i * 8
                        ships[pos].isShip = true
                    }
                    placed = true
                } else {
                    Log.d("GenerateShips", "Cannot place ship of size $size at start $start horizontally: $horizontal")
                }
            }
            if (attempts == 100) {
                Log.e("GenerateShips", "Failed to place ship of size $size after 100 attempts")
            }
        }
        return ships
    }

    private fun canPlaceShip(start: Int, size: Int, horizontal: Boolean, ships: List<GridItem>): Boolean {
        for (i in 0 until size) {
            val pos = if (horizontal) start + i else start + i * 8
            if (pos >= 64 || (horizontal && start % 8 + i >= 8)) {
                return false
            }
            if (ships[pos].isShip) {
                return false
            }
        }
        return true
    }

    private fun handleEnemyGridClick(position: Int) {
        gameRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val game = document.toObject(Game::class.java)
                if (game != null && game.turn == auth.currentUser?.uid) {
                    val item = enemyGridItems[position]
                    if (!item.isHit) {
                        item.isHit = true
                        val player2Ships = game.player2Ships.toMutableList()
                        val target = player2Ships.find { it.position == position }
                        target?.isHit = true
                        gameRef.update("player2Ships", player2Ships)
                        enemyGridAdapter.notifyDataSetChanged()
                        switchTurn(game)
                    }
                } else {
                    Toast.makeText(this, "Not your turn", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun switchTurn(game: Game) {
        val nextTurn = if (game.turn == game.player1) game.player2 else game.player1
        gameRef.update("turn", nextTurn).addOnSuccessListener {
            updateTurnIndicator()
        }
    }

    private fun updateTurnIndicator() {
        val currentUser = auth.currentUser?.uid
        gameRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val game = document.toObject(Game::class.java)
                if (game != null) {
                    val currentTurn = game.turn
                    turnIndicator.text = if (currentTurn == currentUser) "Your Turn" else "Opponent's Turn"
                }
            }
        }
    }

    private fun checkForWin(game: Game) {
        val currentUser = auth.currentUser?.uid
        val enemyShips = if (currentUser == game.player1) game.player2Ships else game.player1Ships
        if (enemyShips.all { it.isHit }) {
            Toast.makeText(this, "You won!", Toast.LENGTH_LONG).show()
            gameRef.delete()
        }
    }

    private fun listenForUpdates() {
        gameRef.addSnapshotListener { document, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (document != null && document.exists()) {
                val game = document.toObject(Game::class.java)
                if (game != null) {
                    updateGrids(game)
                    updateTurnIndicator()
                }
            }
        }
    }

    private fun updateGrids(game: Game) {
        val currentUser = auth.currentUser?.uid

        val playerShips = if (currentUser == game.player1) game.player1Ships else game.player2Ships
        for (gridItem in playerShips) {
            playerGridItems[gridItem.position].apply {
                isShip = gridItem.isShip
                isHit = gridItem.isHit
            }
        }
        playerGridAdapter.notifyDataSetChanged()

        val enemyShips = if (currentUser == game.player1) game.player2Ships else game.player1Ships
        for (gridItem in enemyShips) {
            enemyGridItems[gridItem.position].apply {
                isShip = gridItem.isShip
                isHit = gridItem.isHit
            }
        }
        enemyGridAdapter.notifyDataSetChanged()

        updateTurnIndicator()
    }

    private fun updatePlayerGrid(playerShips: List<GridItem>) {
        for (gridItem in playerShips) {
            playerGridItems[gridItem.position].apply {
                isHit = gridItem.isHit
                isShip = gridItem.isShip
            }
        }
        playerGridAdapter.notifyDataSetChanged()
    }

    private fun updateEnemyGrid(enemyShips: List<GridItem>) {
        for (gridItem in enemyShips) {
            enemyGridItems[gridItem.position].apply {
                isHit = gridItem.isHit
                isShip = gridItem.isShip
            }
        }
        enemyGridAdapter.notifyDataSetChanged()
    }
}
