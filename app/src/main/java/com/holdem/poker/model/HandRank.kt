package com.holdem.poker.model

/**
 * Ранги покерных комбинаций от старших к младшим
 */
enum class HandRank(val value: Int, val name: String) {
    HIGH_CARD(1, "High Card"),
    PAIR(2, "Pair"),
    TWO_PAIR(3, "Two Pair"),
    THREE_OF_A_KIND(4, "Three of a Kind"),
    STRAIGHT(5, "Straight"),
    FLUSH(6, "Flush"),
    FULL_HOUSE(7, "Full House"),
    FOUR_OF_A_KIND(8, "Four of a Kind"),
    STRAIGHT_FLUSH(9, "Straight Flush"),
    ROYAL_FLUSH(10, "Royal Flush")
}

/**
 * Результат оценки руки
 */
data class HandEvaluation(
    val rank: HandRank,
    val cards: List<Card>,
    val kickers: List<Int> = emptyList()
) : Comparable<HandEvaluation> {
    override fun compareTo(other: HandEvaluation): Int {
        if (rank.value != other.rank.value) {
            return rank.value.compareTo(other.rank.value)
        }
        // Если ранги одинаковые, сравниваем кикеры
        for (i in kickers.indices) {
            if (i >= other.kickers.size) return 1
            val comparison = kickers[i].compareTo(other.kickers[i])
            if (comparison != 0) return comparison
        }
        return 0
    }
}

