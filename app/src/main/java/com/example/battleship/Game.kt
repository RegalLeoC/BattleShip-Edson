package com.example.battleship

import com.google.firebase.firestore.PropertyName

data class Game(
    val id: String = "",
    val player1: String = "",
    val player2: String? = null,
    val turn: String = "",
    @get:PropertyName("full") @set:PropertyName("full") var isFull: Boolean = false,
    val player1Ships: List<GridItem> = emptyList(),
    val player2Ships: List<GridItem> = emptyList(),
    val player1Hits: List<GridItem> = emptyList(),
    val player2Hits: List<GridItem> = emptyList(),
)