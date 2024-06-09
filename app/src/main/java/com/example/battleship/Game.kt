package com.example.battleship


data class Game(
    val id: String = "",
    val player1: String? = null,
    val player2: String? = null,
    val turn: String? = null,
    val isFull: Boolean = false,
    val player1Grid: List<GridItem> = listOf(),
    val player2Grid: List<GridItem> = listOf()
)
