package com.example.battleship

import com.google.firebase.firestore.PropertyName

data class Game(
    val id: String = "",
    val player1: String = "",
    val player2: String? = null,
    var turn: String = "",
    @get:PropertyName("full") @set:PropertyName("full") var isFull: Boolean = false,
    var player1Ships: List<GridItem> = emptyList(),
    var player2Ships: List<GridItem> = emptyList(),
    var gameOver: Boolean = false,
    var winner: String? = null
)