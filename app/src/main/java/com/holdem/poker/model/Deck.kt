package com.holdem.poker.model

import kotlin.random.Random

/**
 * Колода из 52 карт
 */
class Deck {
    private val cards = mutableListOf<Card>()
    
    init {
        reset()
    }
    
    /**
     * Создает новую полную колоду и перемешивает
     */
    fun reset() {
        cards.clear()
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                cards.add(Card(suit, rank))
            }
        }
        shuffle()
    }
    
    /**
     * Перемешивает колоду
     */
    fun shuffle() {
        cards.shuffle(Random.Default)
    }
    
    /**
     * Раздает одну карту
     */
    fun dealCard(): Card {
        if (cards.isEmpty()) {
            throw IllegalStateException("Колода пуста")
        }
        return cards.removeAt(0)
    }
    
    /**
     * Раздает несколько карт
     */
    fun dealCards(count: Int): List<Card> {
        return (1..count).map { dealCard() }
    }
    
    /**
     * Количество оставшихся карт
     */
    fun remainingCards(): Int = cards.size
}

