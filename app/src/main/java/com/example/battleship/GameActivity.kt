package com.example.battleship

import android.os.Bundle
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GameActivity : AppCompatActivity() {

    private lateinit var gridLocal: GridView
    private lateinit var gridRival: GridView
    private lateinit var localGridItems: MutableList<GridItem>
    private lateinit var rivalGridItems: MutableList<GridItem>
    private lateinit var localAdapter: GridAdapter
    private lateinit var rivalAdapter: GridAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var gameId: String
    private lateinit var playerId: String
    private lateinit var tvTurn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gridLocal = findViewById(R.id.gridLocal)
        gridRival = findViewById(R.id.gridRival)
        tvTurn = findViewById(R.id.tvTurn)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        playerId = auth.currentUser?.uid ?: ""

        gameId = intent.getStringExtra("gameId") ?: ""

        initGrids()
        setUpFirestoreListeners()
    }

    private fun initGrids() {
        localGridItems = MutableList(64) { GridItem() }
        rivalGridItems = MutableList(64) { GridItem() }

        // Place ships for player1 (host)
        placeShips(localGridItems)

        db.collection("games").document(gameId).get().addOnSuccessListener { document ->
            val game = document.toObject(Game::class.java)
            if (game != null) {
                if (playerId == game.player1) {
                    db.collection("games").document(gameId).update("player1Grid", localGridItems)
                } else {
                    // Load the existing grid for player1 and generate the grid for player2
                    localGridItems = game.player1Grid.toMutableList()
                    placeShips(rivalGridItems)
                    db.collection("games").document(gameId).update("player2Grid", rivalGridItems)
                }
            }
        }

        localAdapter = GridAdapter(this, localGridItems)
        rivalAdapter = GridAdapter(this, rivalGridItems)

        gridLocal.adapter = localAdapter
        gridRival.adapter = rivalAdapter

        gridRival.setOnItemClickListener { _, _, position, _ ->
            // Only allow to play if it's the player's turn
            if (tvTurn.text == "Your Turn") {
                if (!rivalGridItems[position].isHit) {
                    rivalGridItems[position].isHit = true
                    rivalGridItems[position].drawable = if (rivalGridItems[position].isShip) {
                        R.drawable.hit
                    } else {
                        R.drawable.miss
                    }
                    updateFirestore(position)
                    checkVictory()
                    rivalAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun placeShips(gridItems: MutableList<GridItem>) {
        val shipSizes = listOf(4, 3, 3, 2, 2) // example ship sizes

        for (size in shipSizes) {
            var placed = false
            while (!placed) {
                val orientation = (0..1).random() // 0 for horizontal, 1 for vertical
                val startPos = (0 until 64).random()
                val positions = mutableListOf<Int>()

                if (orientation == 0) { // horizontal
                    val row = startPos / 8
                    if (startPos % 8 + size <= 8) {
                        for (i in 0 until size) {
                            positions.add(startPos + i)
                        }
                    }
                } else { // vertical
                    if (startPos / 8 + size <= 8) {
                        for (i in 0 until size) {
                            positions.add(startPos + i * 8)
                        }
                    }
                }

                if (positions.all { !gridItems[it].isShip }) {
                    for (pos in positions) {
                        gridItems[pos].isShip = true
                        gridItems[pos].drawable = R.drawable.ship
                    }
                    placed = true
                }
            }
        }
    }

    private fun updateFirestore(position: Int) {
        val move = hashMapOf(
            "player" to playerId,
            "position" to position
        )
        db.collection("games").document(gameId)
            .collection("moves").add(move)
            .addOnSuccessListener {
                toggleTurn()
            }
    }

    private fun setUpFirestoreListeners() {
        db.collection("games").document(gameId).addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                return@addSnapshotListener
            }

            val game = snapshot.toObject(Game::class.java)
            if (game != null) {
                if (playerId == game.turn) {
                    tvTurn.text = "Your Turn"
                } else {
                    tvTurn.text = "Opponent's Turn"
                }
                if (playerId == game.player1) {
                    rivalGridItems = game.player2Grid.toMutableList()
                } else {
                    localGridItems = game.player1Grid.toMutableList()
                }
                localAdapter.notifyDataSetChanged()
                rivalAdapter.notifyDataSetChanged()
            }
        }

        db.collection("games").document(gameId)
            .collection("moves").addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) {
                    return@addSnapshotListener
                }

                for (doc in snapshots.documentChanges) {
                    val move = doc.document.data
                    val position = (move["position"] as Long).toInt()
                    val player = move["player"] as String
                    if (player != playerId) {
                        if (!localGridItems[position].isHit) {
                            localGridItems[position].isHit = true
                            localGridItems[position].drawable = if (localGridItems[position].isShip) {
                                R.drawable.hit
                            } else {
                                R.drawable.miss
                            }
                            localAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
    }

    private fun toggleTurn() {
        db.collection("games").document(gameId).get().addOnSuccessListener { document ->
            val game = document.toObject(Game::class.java)
            if (game != null) {
                val newTurn = if (game.turn == game.player1) game.player2 else game.player1
                db.collection("games").document(gameId).update("turn", newTurn)
            }
        }
    }

    private fun checkVictory() {
        val allShipsHit = rivalGridItems.filter { it.isShip }.all { it.isHit }
        if (allShipsHit) {
            tvTurn.text = "You Win!"
            db.collection("games").document(gameId).delete()
            db.collection("games").document(gameId).collection("moves").get().addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    snapshot.reference.delete()
                }
            }
            finish()
        }
    }
}
