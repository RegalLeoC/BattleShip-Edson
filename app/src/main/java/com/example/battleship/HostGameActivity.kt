package com.example.battleship

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.ListenerRegistration


class HostGameActivity : AppCompatActivity() {

    private lateinit var tvWaiting: TextView
    private lateinit var btnCancel: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var gameId: String

    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_game)

        tvWaiting = findViewById(R.id.tvWaiting)
        btnCancel = findViewById(R.id.btnCancel)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnCancel.setOnClickListener {
            cancelHosting()
        }

        hostGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure listener is unregistered to prevent memory leaks
        listenerRegistration?.remove()
    }

    private fun hostGame() {
        val userId = auth.currentUser?.uid ?: return
        gameId = db.collection("games").document().id
        val newGame = Game(
            id = gameId,
            player1 = userId,
            turn = userId,
            isFull = false,
            player1Ships = List(64) { GridItem() },
            player2Ships = List(64) { GridItem() }
        )
        db.collection("games").document(gameId).set(newGame)
            .addOnSuccessListener {
                waitForPlayerToJoin()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to host game.", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun waitForPlayerToJoin() {
        listenerRegistration = db.collection("games").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val game = snapshot.toObject(Game::class.java)
                if (game?.isFull == true) {
                    launchHostActivity()
                }
            }
    }

    private fun launchHostActivity() {
        val intent = Intent(this, HostActivity::class.java)
        intent.putExtra("gameId", gameId)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun cancelHosting() {
        db.collection("games").document(gameId).delete()
            .addOnSuccessListener {
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to cancel hosting.", Toast.LENGTH_SHORT).show()
            }
    }
}
