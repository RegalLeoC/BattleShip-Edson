package com.example.battleship

data class GridItem(
    var isShip: Boolean = false,
    var isHit: Boolean = false,
    var drawable: Int = R.drawable.empty // Default to an empty grid drawable
)

