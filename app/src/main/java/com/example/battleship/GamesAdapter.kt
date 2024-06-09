package com.example.battleship

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GamesAdapter(
    private val games: List<Game>,
    private val onJoinClick: (String) -> Unit
) : RecyclerView.Adapter<GamesAdapter.GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(games[position], onJoinClick)
    }

    override fun getItemCount() = games.size

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGameId: TextView = itemView.findViewById(R.id.tvGameId)
        private val btnJoin: Button = itemView.findViewById(R.id.btnJoin)

        fun bind(game: Game, onJoinClick: (String) -> Unit) {
            tvGameId.text = game.toString()  // Customize this to show game details
            btnJoin.setOnClickListener {
                onJoinClick(game.id)
            }
        }
    }
}
