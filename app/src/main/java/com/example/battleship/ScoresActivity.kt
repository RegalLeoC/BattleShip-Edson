package com.example.battleship

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ScoresActivity : AppCompatActivity() {

    private lateinit var rvScores: RecyclerView
    private lateinit var scoresAdapter: ScoresAdapter
    private lateinit var scores: MutableList<Score>
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scores)

        rvScores = findViewById(R.id.rvScores)
        rvScores.layoutManager = LinearLayoutManager(this)

        scores = mutableListOf()
        scoresAdapter = ScoresAdapter(scores)
        rvScores.adapter = scoresAdapter

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setUpFirestoreListener()
    }

    private fun setUpFirestoreListener() {
        db.collection("scores").orderBy("points").addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null) {
                return@addSnapshotListener
            }
            scores.clear()
            for (doc in snapshots.documents) {
                val score = doc.toObject(Score::class.java)
                if (score != null) {
                    scores.add(score)
                }
            }
            scores.sortByDescending { it.points }
            scoresAdapter.notifyDataSetChanged()
        }
    }
}
