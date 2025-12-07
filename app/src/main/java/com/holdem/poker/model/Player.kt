package com.holdem.poker.model

/**
 * Игрок за столом
 */
data class Player(
    val id: String,
    val name: String,
    var chips: Int,
    var hand: List<Card> = emptyList(),
    var currentBet: Int = 0,
    var isFolded: Boolean = false,
    var isAllIn: Boolean = false,
    var isDealer: Boolean = false,
    var isSmallBlind: Boolean = false,
    var isBigBlind: Boolean = false,
    var isActive: Boolean = true
) {
    fun canBet(amount: Int): Boolean {
        return chips >= amount && !isFolded && !isAllIn
    }
    
    fun bet(amount: Int) {
        val betAmount = minOf(amount, chips)
        chips -= betAmount
        currentBet += betAmount
        if (chips == 0) {
            isAllIn = true
        }
    }
    
    fun fold() {
        isFolded = true
    }
    
    fun resetForNewHand() {
        hand = emptyList()
        currentBet = 0
        isFolded = false
        isAllIn = false
        isDealer = false
        isSmallBlind = false
        isBigBlind = false
    }
}

