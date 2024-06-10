package com.example.battleship

data class GridItem(
    var isShip: Boolean = false,
    var isHit: Boolean = false,
    var position: Int = 0
)


