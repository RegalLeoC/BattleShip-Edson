package com.example.battleship

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var btnHost: Button
    private lateinit var btnJoin: Button
    private lateinit var btnScores: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin = findViewById(R.id.btnLogin)
        btnHost = findViewById(R.id.btnHost)
        btnJoin = findViewById(R.id.btnJoin)
        btnScores = findViewById(R.id.btnScores)
        auth = FirebaseAuth.getInstance()

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnHost.setOnClickListener {
            startActivity(Intent(this, HostGameActivity::class.java))
        }

        btnJoin.setOnClickListener {
            startActivity(Intent(this, JoinGameActivity::class.java))
        }

        btnScores.setOnClickListener {
            startActivity(Intent(this, ScoresActivity::class.java))
        }

        auth.addAuthStateListener {
            val userLoggedIn = it.currentUser != null
            btnHost.isEnabled = userLoggedIn
            btnJoin.isEnabled = userLoggedIn
            btnScores.isEnabled = userLoggedIn
        }
    }
}
