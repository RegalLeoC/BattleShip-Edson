package com.example.battleship

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class JoinGameActivity : AppCompatActivity() {

    private lateinit var rvGames: RecyclerView
    private lateinit var gamesAdapter: GamesAdapter
    private lateinit var games: MutableList<Game>
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)

        rvGames = findViewById(R.id.rvGames)
        rvGames.layoutManager = LinearLayoutManager(this)
        games = mutableListOf()
        gamesAdapter = GamesAdapter(games) { gameId ->
            joinGame(gameId)
        }
        rvGames.adapter = gamesAdapter

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadAvailableGames()
    }

    private fun loadAvailableGames() {
        db.collection("games").whereEqualTo("full", false).addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null) {
                return@addSnapshotListener
            }

            games.clear()
            for (doc in snapshots.documents) {
                val game = doc.toObject(Game::class.java)
                if (game != null) {
                    games.add(game)
                }
            }
            gamesAdapter.notifyDataSetChanged()
        }
    }

    private fun joinGame(gameId: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("games").document(gameId).update("player2", userId, "full", true)
            .addOnSuccessListener {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("gameId", gameId)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to join game.", Toast.LENGTH_SHORT).show()
            }
    }
}
