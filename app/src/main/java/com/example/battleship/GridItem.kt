package com.example.battleship

import com.google.firebase.firestore.PropertyName

data class GridItem(
    @get:PropertyName("ship") @set:PropertyName("ship") var isShip: Boolean = false,
    @get:PropertyName("hit") @set:PropertyName("hit") var isHit: Boolean = false,
    var position: Int = 0
)


