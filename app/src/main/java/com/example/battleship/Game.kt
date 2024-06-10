package com.example.battleship

import com.google.firebase.firestore.PropertyName

data class Game(
    val id: String = "",
    val player1: String = "",
    val player2: String? = null,
    val turn: String = "",
    @get:PropertyName("full") @set:PropertyName("full") var isFull: Boolean = false,
    var playerShips: List<GridItem> = listOf(),
    var enemyShips: List<GridItem> = listOf(),
    var enemyHits: List<GridItem> = listOf()
)