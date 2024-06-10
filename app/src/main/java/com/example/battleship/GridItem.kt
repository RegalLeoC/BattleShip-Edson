package com.example.battleship

import com.google.firebase.firestore.PropertyName

data class GridItem(
    @get:PropertyName("hit") @set:PropertyName("hit") var isHit: Boolean = false,
    @get:PropertyName("position") @set:PropertyName("position") var position: Int = 0,
    @get:PropertyName("ship") @set:PropertyName("ship") var isShip: Boolean = false
)

