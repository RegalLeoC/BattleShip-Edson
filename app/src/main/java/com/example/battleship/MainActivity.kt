package com.example.battleship

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var btnPlay: Button
    private lateinit var btnScores: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin = findViewById(R.id.btnLogin)
        btnPlay = findViewById(R.id.btnPlay)
        btnScores = findViewById(R.id.btnScores)
        auth = FirebaseAuth.getInstance()

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnPlay.setOnClickListener {
            showGameOptionsDialog()
        }

        btnScores.setOnClickListener {
            startActivity(Intent(this, ScoresActivity::class.java))
        }

        auth.addAuthStateListener {
            btnPlay.isEnabled = it.currentUser != null
        }
    }

    private fun showGameOptionsDialog() {
        val options = arrayOf("Host a game", "Join a game")
        AlertDialog.Builder(this)
            .setTitle("Choose an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> hostGame()
                    1 -> joinGame()
                }
            }
            .show()
    }

    private fun hostGame() {
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return
        val newGame = Game(
            id = db.collection("games").document().id,
            player1 = userId,
            turn = userId,
            isFull = false
        )
        db.collection("games").document(newGame.id).set(newGame)
            .addOnSuccessListener {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("gameId", newGame.id)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to host game.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun joinGame() {
        val intent = Intent(this, JoinGameActivity::class.java)
        startActivity(intent)
    }
}
